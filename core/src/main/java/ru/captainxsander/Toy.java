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

    private final float width = 0.90f;
    private final float height = 0.90f;

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
        shape.setRadius(0.38f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.7f;
        fix.friction = 1.0f;
        fix.restitution = 0.04f;

        body.createFixture(fix);
        body.setLinearDamping(3.2f);
        body.setAngularDamping(5.0f);

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

            if (inside && speed < 0.35f) {
                settleTimer += delta;
                if (settleTimer > 0.25f) {
                    lockInTray();
                }
            } else {
                settleTimer = 0f;
            }
        } else {
            Vector2 v = body.getLinearVelocity();
            if (Math.abs(v.x) < 0.03f) {
                body.setLinearVelocity(0, v.y);
            }
            if (Math.abs(body.getAngularVelocity()) < 0.03f) {
                body.setAngularVelocity(0);
            }
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

    public void releaseToPhysicalTray(WinZone winZone, boolean missTray) {
        captured = false;
        releasedToPhysicsTray = true;
        inTray = false;
        won = false;
        settleTimer = 0f;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);

        // меняем restitution у игрушки на время падения в лоток
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.85f);
        }

        float impulseX;
        float impulseY;

        if (missTray) {
            // намеренный промах правее лотка
            impulseX = 2.4f + (float)Math.random() * 0.8f;
            impulseY = -0.8f - (float)Math.random() * 0.4f;
        } else {
            // в сторону центра лотка, но с разбросом
            float dx = winZone.getCenterX() - body.getPosition().x;
            impulseX = dx * 1.6f + ((float)Math.random() - 0.5f) * trayScatterX * 4f;
            impulseY = -1.3f - (float)Math.random() * 0.7f;
        }

        body.setLinearVelocity(impulseX, impulseY);
        body.setAngularVelocity(((float)Math.random() - 0.5f) * 5f);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture,
            body.getPosition().x - width / 2f,
            body.getPosition().y - height / 2f,
            width, height);
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
