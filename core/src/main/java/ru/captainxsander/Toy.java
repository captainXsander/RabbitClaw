package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Toy {

    private final Body body;
    private final Texture texture;

    private float releaseDelay = 0f;
    private boolean justReleased = false;

    private float slidePhase = 0f;
    private float slideDir = 0f;

    private boolean captured = false;
    private boolean won = false;
    private boolean inTray = false;
    private boolean releasedToPhysicsTray = false;

    private final float width = GameTuning.TOY_DRAW_W;
    private final float height = GameTuning.TOY_DRAW_H;

    private final float catchDifficulty;
    private final float trayScatterX;
    private final float trayRestitution;

    private float settleTimer = 0f;

    public Toy(World world, float x, float y, String texturePath) {
        this(world, x, y, texturePath, 0.25f, 0.15f, 0.20f);
    }

    public Toy(World world, float x, float y, String texturePath,
               float catchDifficulty, float trayScatterX, float trayRestitution) {
        texture = new Texture(Gdx.files.internal(texturePath));

        this.catchDifficulty = catchDifficulty;
        this.trayScatterX = trayScatterX;
        this.trayRestitution = trayRestitution;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(GameTuning.TOY_RADIUS);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = GameTuning.TOY_DENSITY;
        fix.friction = GameTuning.TOY_FRICTION;
        fix.restitution = GameTuning.TOY_RESTITUTION;

        body.createFixture(fix);

        body.setLinearDamping(GameTuning.TOY_LINEAR_DAMPING);
        body.setAngularDamping(GameTuning.TOY_ANGULAR_DAMPING);

        shape.dispose();
    }

    public void update(float delta, WinZone winZone) {
        // 🔥 фаза "залипания" + соскальзывания
        if (justReleased) {

            releaseDelay -= delta;

            if (releaseDelay > 0) {
                // висит
                body.setLinearVelocity(
                    body.getLinearVelocity().x * 0.9f,
                    0
                );
                return;
            }

            // 🔥 соскальзывание с пальцев
            if (slidePhase > 0) {
                slidePhase -= delta;

                // 🔥 вообще НЕ трогаем velocity
                // только визуально “ждём соскальзывание”

                return;
            }

            // 🔥 включаем нормальную физику
            justReleased = false;
            body.setGravityScale(GameTuning.TOY_TRAY_GRAVITY_SCALE);
        }
        if (captured) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        if (inTray) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        if (releasedToPhysicsTray) {
            boolean inside =
                body.getPosition().x > winZone.getInnerLeft() &&
                    body.getPosition().x < winZone.getInnerRight() &&
                    body.getPosition().y > winZone.getInnerBottom() &&
                    body.getPosition().y < winZone.getInnerTop();

            float speed = body.getLinearVelocity().len();

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

        if (Math.abs(body.getLinearVelocity().x) < 0.02f) {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        if (Math.abs(body.getAngularVelocity()) < 0.02f) {
            body.setAngularVelocity(0);
        }
    }

    private void lockInTray() {
        releasedToPhysicsTray = false;
        inTray = true;
        won = true;

        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0f);
        body.setType(BodyDef.BodyType.StaticBody);
    }

    public void attachTo(float x, float y, float swing) {
        captured = true;
        releasedToPhysicsTray = false;

        body.setType(BodyDef.BodyType.KinematicBody);
        body.setGravityScale(0f);
        body.setTransform(x, y, swing * 0.12f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    public void releaseFailedGrab(float impulseX, float impulseY) {
        captured = false;
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);
        body.setLinearVelocity(impulseX, impulseY);
        body.setAngularVelocity(impulseX * 0.25f);
    }

    public void releaseToPhysicalTray(WinZone winZone, boolean missTray,
                                      boolean earlyRelease, float clawVelocityX) {

        captured = false;
        releasedToPhysicsTray = true;
        inTray = false;
        won = false;
        settleTimer = 0f;

        justReleased = true;

        releaseDelay = 0.08f + (float) Math.random() * 0.12f;
        slidePhase = 0.12f + (float) Math.random() * 0.1f;
        slideDir = Math.random() < 0.5 ? -1f : 1f;

        body.setType(BodyDef.BodyType.DynamicBody);

        // 🔥 КЛЮЧ: почти нет гравитации сначала
        body.setGravityScale(0.6f);

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.6f);
        }

        float vx;
        float vy;

        // =========================
        // 🔥 РЕАЛИСТИЧНАЯ ФИЗИКА
        // =========================

        // 1. базовая скорость (ослабленная)
        vx = clawVelocityX * 0.6f;

        // 2. небольшой шум
        vx += (Math.random() - 0.5f) * 0.3f;

        // 3. early release чуть усиливает разброс
        if (earlyRelease) {
            vx += (Math.random() - 0.5f) * 0.3f;
        }

        // 4. "вес"
        vx += slideDir * 0.25f;

        vx *= 0.9f + (1f - catchDifficulty) * 0.2f;

        vx = Math.max(-2.0f, Math.min(2.0f, vx));

        vy = -0.6f - (float)Math.random() * 0.2f;

        float trayCenter = winZone.getCenterX();
        float toyX = body.getPosition().x;

        float dxToTray = trayCenter - toyX;
        float distance = Math.abs(dxToTray);

        // =========================
        // 🎯 ЗОНЫ ПОВЕДЕНИЯ
        // =========================

        // размеры зон (можешь потом крутить)
        float perfectZone = 0.6f;
        float assistZone = 2.2f;

        if (distance < perfectZone) {
            // 🟩 ИДЕАЛЬНАЯ ЗОНА (почти гарант попадание)

            // мягко центрируем
            vx += dxToTray * 0.35f;

            // чуть подбрасываем → красивая дуга
            vy += 0.35f;

        }
        else if (distance < assistZone) {
            // 🟨 ЗОНА ШАНСА

            float t = 1f - (distance - perfectZone) / (assistZone - perfectZone);

            // ослабленная помощь
            vx += dxToTray * 0.25f * t;

            // небольшой подброс
            vy += 0.25f * t;
        }
        else {
            // 🟥 ДАЛЕКО — ничего не делаем (честная физика)
        }

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

    public boolean isReleasedToPhysicsTray() {
        return releasedToPhysicsTray;
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
