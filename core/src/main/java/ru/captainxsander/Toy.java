package ru.captainxsander;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

import static ru.captainxsander.GameTuning.CLAW_VELOCITY_TRANSFER;

/**
 * Игрушка — это физический объект (Box2D), который:
 * * лежит в куче
 * * может быть захвачен клешнёй
 * * может сорваться
 * * может быть сброшен в лоток
 *
 * Важно:
 * основной принцип — мы задаём импульс один раз,
 * дальше вся траектория рассчитывается физикой.
 */
public class Toy {

    // Тип игрушки нужен для зверинца и открытия карточек.
    private final ToyType toyType;
    private final Body body;
    private final Texture texture;

    // =========================
    // Состояния после отпускания
    // =========================

    /**
     * Задержка перед падением, когда игрушка ещё визуально "висит" в клешне.
     */
    private float releaseDelay = 0f;

    /**
     * Флаг: игрушку только что отпустили.
     */
    private boolean justReleased = false;

    /**
     * Время визуального "соскальзывания" с пальцев.
     */
    private float slidePhase = 0f;

    // =========================
    // Состояния игрушки
    // =========================
    private boolean captured = false;
    private boolean won = false;
    private boolean inTray = false;
    private boolean releasedToPhysicsTray = false;

    private final float width = GameTuning.TOY_DRAW_W;
    private final float height = GameTuning.TOY_DRAW_H;

    /**
     * Сложность захвата (0..1).
     */
    private final float catchDifficulty;

    /**
     * Насколько игрушка "пружинит" в лотке.
     */
    private final float trayRestitution;

    /**
     * Таймер "успокоения" в лотке.
     */
    private float settleTimer = 0f;

    public Toy(World world, float x, float y, ToyType toyType,
               float catchDifficulty, float trayRestitution) {

        this.toyType = toyType;
        texture = new Texture(Gdx.files.internal(toyType.getTexturePath()));

        this.catchDifficulty = catchDifficulty;
        this.trayRestitution = trayRestitution;

        // =========================
        // Создание физического тела
        // =========================

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);
        body.setUserData(this);

        CircleShape shape = new CircleShape();
        shape.setRadius(GameTuning.TOY_RADIUS);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = GameTuning.TOY_DENSITY;
        fix.friction = GameTuning.TOY_FRICTION;
        fix.restitution = GameTuning.TOY_RESTITUTION;

        body.createFixture(fix);

        // Демпфирование нужно, чтобы игрушки не катались бесконечно.
        body.setLinearDamping(GameTuning.TOY_LINEAR_DAMPING);
        body.setAngularDamping(GameTuning.TOY_ANGULAR_DAMPING);

        shape.dispose();
    }

    /**
     * Обновление логики игрушки.
     */
    public void update(float delta, WinZone winZone) {

        // =========================
        // Фаза: только что отпустили
        // =========================
        if (justReleased) {

            releaseDelay -= delta;

            // Некоторое время игрушка ещё "висит" в клешне.
            if (releaseDelay > 0) {
                // На Android не "замораживаем" вертикаль после разжима клешни:
                // иначе игрушка визуально падает слишком медленно.
                if (isAndroidRuntime()) {
                    body.setLinearVelocity(body.getLinearVelocity().x * 0.92f, body.getLinearVelocity().y);
                } else {
                    body.setLinearVelocity(
                        body.getLinearVelocity().x * 0.9f,
                        0
                    );
                }
                return;
            }

            // Фаза визуального соскальзывания без вмешательства в скорость.
            if (slidePhase > 0) {
                slidePhase -= delta;
                return;
            }

            // Включаем нормальную физику лотка.
            justReleased = false;
            // Для Android поднимаем gravityScale, чтобы падение/оседание в лотке
            // не выглядело "замедленным" по сравнению с desktop.
            float trayGravityScale = isAndroidRuntime() ? 0.55f : GameTuning.TOY_TRAY_GRAVITY_SCALE;
            body.setGravityScale(trayGravityScale);
        }

        // Если игрушка уже в клешне, замораживаем её скорость.
        if (captured) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        // Если игрушка уже зафиксирована как выигранная, больше не двигаем её.
        if (inTray) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        // =========================
        // Физика в лотке
        // =========================
        if (releasedToPhysicsTray) {

            boolean inside =
                body.getPosition().x > winZone.getInnerLeft() &&
                    body.getPosition().x < winZone.getInnerRight() &&
                    body.getPosition().y > winZone.getInnerBottom() &&
                    body.getPosition().y < winZone.getInnerTop();

            float speed = body.getLinearVelocity().len();

            // Проверяем, успокоилась ли игрушка внутри зоны выигрыша.
            if (inside
                && speed < GameTuning.TOY_SETTLE_SPEED
                && Math.abs(body.getAngularVelocity()) < GameTuning.TOY_SETTLE_ANGULAR_SPEED) {

                settleTimer += delta;

                if (settleTimer > GameTuning.TOY_SETTLE_TIME) {
                    lockInTray();
                }
            } else {
                settleTimer = 0f;
            }

            // Если игрушка вылетела обратно, возвращаем ей обычную физику кучи.
            boolean backOnFloor =
                body.getPosition().y < GameTuning.TOY_BACK_ON_FLOOR_Y &&
                    Math.abs(body.getLinearVelocity().y) < GameTuning.TOY_BACK_ON_FLOOR_MAX_VY &&
                    !inside;

            if (backOnFloor) {
                releasedToPhysicsTray = false;
                body.setGravityScale(1f);

                for (Fixture fixture : body.getFixtureList()) {
                    fixture.setRestitution(GameTuning.TOY_RESTITUTION);
                    fixture.setFriction(GameTuning.TOY_FRICTION);
                }
            }

            return;
        }

        // Слабая "жизнь" кучи, чтобы игрушки не выглядели совсем мёртвыми.
        if (Math.random() < 0.002f) {
            float impulseX = (float) (Math.random() * 0.02f - 0.01f);
            float impulseY = (float) (Math.random() * 0.02f);

            body.applyLinearImpulse(
                impulseX,
                impulseY,
                body.getWorldCenter().x,
                body.getWorldCenter().y,
                true
            );
        }
    }

    /**
     * Фиксирует игрушку как выигранную.
     */
    private void lockInTray() {
        releasedToPhysicsTray = false;
        inTray = true;
        won = true;

        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0f);
        body.setType(BodyDef.BodyType.StaticBody);
    }

    /**
     * Прикрепление к клешне.
     */
    public void attachTo(float x, float y, float swing) {
        captured = true;
        releasedToPhysicsTray = false;

        body.setType(BodyDef.BodyType.KinematicBody);
        body.setGravityScale(0f);

        // Игрушка следует за клешнёй напрямую.
        body.setTransform(x, y, swing * 0.12f);

        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    /**
     * Срыв с клешни.
     */
    public void releaseFailedGrab(float impulseX, float impulseY) {
        captured = false;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);

        body.setLinearVelocity(impulseX, impulseY);
        body.setAngularVelocity(impulseX * 0.25f);
    }

    /**
     * Сброс игрушки в сторону лотка.
     */
    public void releaseToPhysicalTray(WinZone winZone,
                                      boolean earlyRelease,
                                      float clawVelocityX) {

        captured = false;
        releasedToPhysicsTray = true;
        inTray = false;
        won = false;
        settleTimer = 0f;

        justReleased = true;

        // На Android делаем падение в лоток быстрее:
        // меньше стартовая задержка и короче фаза соскальзывания.
        if (isAndroidRuntime()) {
            // Момент после разжатия на Android должен быть сразу "живым":
            // убираем искусственную паузу/слайд, чтобы игрушка сразу падала.
            releaseDelay = 0f;
            slidePhase = 0f;
        } else {
            // Desktop оставляем без изменений.
            releaseDelay = 0.08f + (float) Math.random() * 0.12f;
            slidePhase = 0.12f + (float) Math.random() * 0.1f;
        }

        body.setType(BodyDef.BodyType.DynamicBody);

        // На Android ускоряем начальное падение в лоток,
        // чтобы игрушка визуально не "зависала".
        body.setGravityScale(isAndroidRuntime() ? 1.15f : 0.6f);

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.6f);
        }

        float vx;
        float vy;

        // =========================
        // Импульс
        // =========================

        // Передаём горизонтальную скорость от клешни.
        vx = clawVelocityX * CLAW_VELOCITY_TRANSFER;

        // Добавляем небольшой случайный шум.
        vx += (float) ((Math.random() - 0.5f) * GameTuning.RELEASE_RANDOM_X);

        if (earlyRelease) {
            vx += (float) ((Math.random() - 0.5f) * GameTuning.RELEASE_RANDOM_X_EARLY);
        }

        // Более тяжёлые или сложные игрушки немного хуже долетают.
        vx *= 0.9f + (1f - catchDifficulty) * 0.2f;

        // Ограничиваем скорость разумным диапазоном.
        vx = Math.max(-GameTuning.RELEASE_MAX_VX,
            Math.min(GameTuning.RELEASE_MAX_VX, vx));

        // Задаём падение вниз.
        if (isAndroidRuntime()) {
            // Более уверенное стартовое падение на Android.
            vy = -1.20f - (float) Math.random() * 0.30f;
        } else {
            vy = GameTuning.RELEASE_BASE_VY
                - (float) Math.random() * GameTuning.RELEASE_RANDOM_VY;
        }

        // Скорость задаётся один раз, дальше работает физика.
        body.setLinearVelocity(vx, vy);

        body.setAngularVelocity(
            ((float) Math.random() - 0.5f) * 1.2f
        );
    }

    private boolean isAndroidRuntime() {
        return Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.Android;
    }

    public void render(SpriteBatch batch) {
        float rotationDeg = (float) Math.toDegrees(body.getAngle());

        batch.draw(
            texture,
            body.getPosition().x - width / 2f,
            body.getPosition().y - height / 2f,
            width / 2f, height / 2f,
            width, height,
            1f, 1f,
            rotationDeg,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public Body getBody() {
        return body;
    }

    public ToyType getToyType() {
        return toyType;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isCaptured() {
        return captured;
    }

    public boolean isInTray() {
        return inTray;
    }

    /**
     * Проверка, что центр игрушки находится внутри внутреннего контура лотка.
     * Используется режимом FIND_ANIMAL, чтобы зафиксировать результат
     * сразу при попадании игрушки в лоток, не дожидаясь полного "успокоения".
     */
    public boolean isInsideWinZone(WinZone winZone) {
        return body.getPosition().x > winZone.getInnerLeft()
            && body.getPosition().x < winZone.getInnerRight()
            && body.getPosition().y > winZone.getInnerBottom()
            && body.getPosition().y < winZone.getInnerTop();
    }

    /**
     * Более мягкая проверка попадания в сам лоток (не только в узкую inner-зону).
     * Нужна для FIND_ANIMAL, чтобы результат фиксировался стабильно даже когда
     * игрушка застревает у стенки лотка и центр не попадает в inner-прямоугольник.
     */
    public boolean isInsideTrayBounds(WinZone winZone) {
        float left = winZone.getX() - winZone.getWidth() * 0.5f;
        float right = winZone.getX() + winZone.getWidth() * 0.5f;
        float bottom = winZone.getY();
        float top = winZone.getY() + winZone.getHeight();

        float radius = GameTuning.TOY_RADIUS * 0.55f;
        float x = body.getPosition().x;
        float y = body.getPosition().y;

        return x + radius > left
            && x - radius < right
            && y + radius > bottom
            && y - radius < top;
    }

    public float getCatchDifficulty() {
        return catchDifficulty;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void dispose() {
        texture.dispose();
    }
}
