package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Toy {

    private final Body body;
    private final Texture texture;

    private boolean captured = false;
    private boolean won = false;
    private boolean inTray = false;
    private boolean releasedToPhysicsTray = false;

    private final float width = GameTuning.TOY_DRAW_W;
    private final float height = GameTuning.TOY_DRAW_H;

    // Характер игрушки
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

        // Меньше damping, чтобы дольше было видно столкновения
        body.setLinearDamping(GameTuning.TOY_LINEAR_DAMPING);
        body.setAngularDamping(GameTuning.TOY_ANGULAR_DAMPING);

        shape.dispose();
    }

    public void update(float delta, WinZone winZone) {
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

            Vector2 v = body.getLinearVelocity();
            float speed = v.len();

            // Если успокоилась внутри — выиграна
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

            // Если снова дошла до пола и не в лотке —
            // возвращаем в обычное состояние.
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

        Vector2 v = body.getLinearVelocity();
        if (Math.abs(v.x) < 0.02f) {
            body.setLinearVelocity(0, v.y);
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

    public void attachTo(float x, float y) {
        captured = true;
        releasedToPhysicsTray = false;

        body.setType(BodyDef.BodyType.KinematicBody);
        body.setGravityScale(0f);
        body.setTransform(x, y, 0f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    public void releaseFailedGrab(float impulseX, float impulseY) {
        captured = false;
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);
        body.setLinearVelocity(impulseX, impulseY);
        body.setAngularVelocity(impulseX * 0.7f);
    }

    // earlyRelease = ранний сброс по пути к лотку
    public void releaseToPhysicalTray(WinZone winZone, boolean missTray, boolean earlyRelease) {
        captured = false;
        releasedToPhysicsTray = true;
        inTray = false;
        won = false;
        settleTimer = 0f;

        body.setType(BodyDef.BodyType.DynamicBody);

        // Пониженная локальная "гравитация" для красивой дуги
        body.setGravityScale(GameTuning.TOY_TRAY_GRAVITY_SCALE);

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.55f);
        }

        float vx;
        float vy;

        if (missTray) {
            vx = 2.8f + (float)Math.random() * 1.0f;
            vy = -0.75f - (float)Math.random() * 0.45f;
        } else {
            float dx = winZone.getCenterX() - body.getPosition().x;

            float sideNoise = ((float)Math.random() - 0.5f) * 1.65f;
            vx = dx * 1.05f + sideNoise + ((float)Math.random() - 0.5f) * trayScatterX * 4.0f;
            vy = -0.78f - (float)Math.random() * 0.42f;

            if (earlyRelease) {
                vx += 0.38f + ((float)Math.random() - 0.5f) * 0.55f;
                vy -= 0.05f;
            }
        }

        body.setLinearVelocity(vx, vy);
        body.setAngularVelocity(((float)Math.random() - 0.5f) * 5.5f);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture,
            body.getPosition().x - width / 2f,
            body.getPosition().y - height / 2f,
            width, height);
    }

    public float getX() { return body.getPosition().x; }
    public float getY() { return body.getPosition().y; }
    public Body getBody() { return body; }

    public boolean isWon() { return won; }
    public boolean isCaptured() { return captured; }
    public boolean isInTray() { return inTray; }
    public boolean isReleasedToPhysicsTray() { return releasedToPhysicsTray; }
    public float getCatchDifficulty() { return catchDifficulty; }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void dispose() {
        texture.dispose();
    }
}
