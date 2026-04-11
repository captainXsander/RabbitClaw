package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Игрушка — это физический объект (Box2D), который:
 * * лежит в куче
 * * может быть захвачен клешнёй
 * * может сорваться
 * * может быть сброшен в лоток
 * <p>
 * 🔥 ВАЖНО:
 * Основной принцип — мы задаём импульс ОДИН раз,
 * дальше вся траектория рассчитывается физикой (Box2D)
 */
public class Toy {

    private final Body body;
    private final Texture texture;

    // =========================
    // Состояния после отпускания
    // =========================

    /**
     * Задержка перед падением (игрушка "висит" в клешне)
     */
    private float releaseDelay = 0f;

    /**
     * Флаг: только что отпущена
     */
    private boolean justReleased = false;

    /**
     * Время "соскальзывания" с пальцев
     * (чисто визуальная задержка — НЕ влияет на velocity)
     */
    private float slidePhase = 0f;

    /**
     * Направление соскальзывания (-1 или 1)
     */
    private float slideDir = 0f;

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
     * Сложность захвата (0..1)
     * влияет на:
     * * шанс поймать
     * * шанс сорваться
     * * поведение в полёте
     */
    private final float catchDifficulty;

    /**
     * Насколько "пружинит" в лотке
     */
    private final float trayRestitution;

    /**
     * Таймер "успокоения" в лотке
     */
    private float settleTimer = 0f;

    public Toy(World world, float x, float y, String texturePath,
               float catchDifficulty, float trayRestitution) {

        texture = new Texture(Gdx.files.internal(texturePath));

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

        // демпфирование (чтобы не катались бесконечно)
        body.setLinearDamping(GameTuning.TOY_LINEAR_DAMPING);
        body.setAngularDamping(GameTuning.TOY_ANGULAR_DAMPING);

        shape.dispose();

    }

    /**
     * Обновление логики игрушки
     */
    public void update(float delta, WinZone winZone) {

        // =========================
        // ФАЗА: только что отпустили
        // =========================
        if (justReleased) {

            releaseDelay -= delta;

            // 🔥 "висит" в клешне
            if (releaseDelay > 0) {
                body.setLinearVelocity(
                    body.getLinearVelocity().x * 0.9f,
                    0
                );
                return;
            }

            // 🔥 фаза "соскальзывания"
            if (slidePhase > 0) {
                slidePhase -= delta;

                // ⚠️ ВАЖНО:
                // НЕ трогаем velocity!
                // иначе ломаем физику

                return;
            }

            // 🔥 включаем нормальную физику
            justReleased = false;
            body.setGravityScale(GameTuning.TOY_TRAY_GRAVITY_SCALE);

        }

        // =========================
        // Если в клешне
        // =========================
        if (captured) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        // =========================
        // Если уже выиграна
        // =========================
        if (inTray) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        // =========================
        // ФИЗИКА В ЛОТКЕ
        // =========================
        if (releasedToPhysicsTray) {

            boolean inside =
                body.getPosition().x > winZone.getInnerLeft() &&
                    body.getPosition().x < winZone.getInnerRight() &&
                    body.getPosition().y > winZone.getInnerBottom() &&
                    body.getPosition().y < winZone.getInnerTop();

            float speed = body.getLinearVelocity().len();

            // 🔥 проверка: "успокоилась ли игрушка"
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

            // 🔥 если вылетела обратно
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

        if (Math.abs(body.getLinearVelocity().x) < 0.008f) {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        if (Math.abs(body.getAngularVelocity()) < 0.008f) {
            body.setAngularVelocity(0);
        }
        // 🔥 микро-жизнь кучи (очень слабая)
        if (!captured && !inTray && !releasedToPhysicsTray) {

            if (Math.random() < 0.002f) { // редко!
                float impulseX = (float)(Math.random() * 0.02f - 0.01f);
                float impulseY = (float)(Math.random() * 0.02f);

                body.applyLinearImpulse(
                    impulseX,
                    impulseY,
                    body.getWorldCenter().x,
                    body.getWorldCenter().y,
                    true
                );
            }
        }
    }

    /**
     * Зафиксировать игрушку как выигранную
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
     * Прикрепление к клешне
     */
    public void attachTo(float x, float y, float swing) {
        captured = true;
        releasedToPhysicsTray = false;

        body.setType(BodyDef.BodyType.KinematicBody);
        body.setGravityScale(0f);

        // 🔥 игрушка просто "телепортируется" за клешнёй
        body.setTransform(x, y, swing * 0.12f);

        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    /**
     * Срыв с клешни
     */
    public void releaseFailedGrab(float impulseX, float impulseY) {
        captured = false;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);

        body.setLinearVelocity(impulseX, impulseY);
        body.setAngularVelocity(impulseX * 0.25f);
    }

    /**
     * 🔥 ГЛАВНЫЙ МЕТОД
     * Сброс игрушки в сторону лотка
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

        // задержки для визуального эффекта
        releaseDelay = 0.08f + (float) Math.random() * 0.12f;
        slidePhase = 0.12f + (float) Math.random() * 0.1f;
        slideDir = Math.random() < 0.5 ? -1f : 1f;

        body.setType(BodyDef.BodyType.DynamicBody);

        // 🔥 слабая гравитация в начале
        body.setGravityScale(0.6f);

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.6f);
        }

        float vx;
        float vy;

        // =========================
        // 🎯 ИМПУЛЬС (САМОЕ ВАЖНОЕ)
        // =========================

        // скорость от клешни
        vx = clawVelocityX * GameTuning.RELEASE_VX_FROM_CLAW_MULT;

        // случайный шум
        vx += (Math.random() - 0.5f) * GameTuning.RELEASE_RANDOM_X;

        if (earlyRelease) {
            vx += (Math.random() - 0.5f) * GameTuning.RELEASE_RANDOM_X_EARLY;
        }

        // эффект "соскальзывания"
        vx += slideDir * GameTuning.RELEASE_SLIDE_IMPULSE;

        // влияние "веса"
        vx *= 0.9f + (1f - catchDifficulty) * 0.2f;

        // ограничение скорости
        vx = Math.max(-GameTuning.RELEASE_MAX_VX,
            Math.min(GameTuning.RELEASE_MAX_VX, vx));

        // падение вниз
        vy = GameTuning.RELEASE_BASE_VY
            - (float) Math.random() * GameTuning.RELEASE_RANDOM_VY;

        // =========================
        // 🎯 ЗОНЫ ПОПАДАНИЯ
        // =========================

        float trayCenter = winZone.getCenterX();
        float dx = trayCenter - body.getPosition().x;
        float distance = Math.abs(dx);

        if (distance < GameTuning.TRAY_PERFECT_ZONE) {
            vx += dx * GameTuning.TRAY_PERFECT_ASSIST_X;
            vy += GameTuning.TRAY_PERFECT_ASSIST_Y;
        } else if (distance < GameTuning.TRAY_ASSIST_ZONE) {
            float t = 1f - (distance - GameTuning.TRAY_PERFECT_ZONE)
                / (GameTuning.TRAY_ASSIST_ZONE - GameTuning.TRAY_PERFECT_ZONE);

            vx += dx * GameTuning.TRAY_ASSIST_X * t;
            vy += GameTuning.TRAY_ASSIST_Y * t;

        }

        // 🔥 задаём скорость ОДИН РАЗ
        body.setLinearVelocity(vx, vy);

        body.setAngularVelocity(
            ((float) Math.random() - 0.5f) * 1.2f
        );
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

    public boolean isWon() {
        return won;
    }

    public boolean isCaptured() {
        return captured;
    }

    public boolean isInTray() {
        return inTray;
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
