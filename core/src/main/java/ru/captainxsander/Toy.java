package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Toy {

    private final Body body;
    private final Texture texture;

    private boolean captured = false;
    private boolean won = false;
    private boolean inTray = false;
    private boolean releasedToPhysicsTray = false;

    private float settleTimer = 0f;

    // 🔥 новое
    private float releaseDelay = 0f;
    private float slideTimer = 0f;
    private float slideDir = 0f;
    private boolean justReleased = false;

    public Toy(World world, float x, float y, String texturePath) {
        texture = new Texture(Gdx.files.internal(texturePath));

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

        // 🔥 плавный сброс
        if (justReleased) {

            if (releaseDelay > 0) {
                releaseDelay -= delta;
                body.setLinearVelocity(body.getLinearVelocity().x * 0.9f, 0);
                return;
            }

            if (slideTimer > 0) {
                slideTimer -= delta;
                body.setLinearVelocity(
                    slideDir * GameTuning.SLIDE_SPEED,
                    GameTuning.SLIDE_FALL_SPEED
                );
                return;
            }

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
            float speed = body.getLinearVelocity().len();

            if (speed < GameTuning.TOY_SETTLE_SPEED) {
                settleTimer += delta;
                if (settleTimer > GameTuning.TOY_SETTLE_TIME) {
                    lockInTray();
                }
            } else {
                settleTimer = 0f;
            }
        }
    }

    private void lockInTray() {
        inTray = true;
        won = true;
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0f);
        body.setType(BodyDef.BodyType.StaticBody);
    }

    // ВАЖНО: сигнатура не менялась
    public void attachTo(float x, float y, float swing) {
        captured = true;
        body.setType(BodyDef.BodyType.KinematicBody);
        body.setTransform(x, y, 0);
    }

    public void releaseToPhysicalTray(WinZone winZone, boolean missTray, boolean earlyRelease) {

        captured = false;
        releasedToPhysicsTray = true;
        settleTimer = 0f;

        justReleased = true;

        releaseDelay = GameTuning.RELEASE_DELAY_MIN +
            (float)Math.random() *
                (GameTuning.RELEASE_DELAY_MAX - GameTuning.RELEASE_DELAY_MIN);

        slideTimer = GameTuning.SLIDE_TIME_MIN +
            (float)Math.random() *
                (GameTuning.SLIDE_TIME_MAX - GameTuning.SLIDE_TIME_MIN);

        slideDir = Math.random() < 0.5f ? -1f : 1f;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(GameTuning.RELEASE_GRAVITY_SCALE);

        float vx;
        float vy = GameTuning.RELEASE_INITIAL_VY;

        if (missTray) {
            vx = GameTuning.RELEASE_RANDOM_X;
        } else {
            float dx = winZone.getCenterX() - body.getPosition().x;
            vx = dx * GameTuning.RELEASE_TO_CENTER_FORCE;
        }

        body.setLinearVelocity(vx, vy);
    }

    public float getX() { return body.getPosition().x; }
    public float getY() { return body.getPosition().y; }
    public Body getBody() { return body; }

    public boolean isCaptured() { return captured; }
    public boolean isWon() { return won; }
    public boolean isInTray() { return inTray; }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void dispose() {
        texture.dispose();
    }
}
