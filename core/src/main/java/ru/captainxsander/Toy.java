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
        shape.setRadius(0.38f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.7f;
        fix.friction = 1.0f;
        fix.restitution = 0.04f;

        body.createFixture(fix);

        // -------------------------
        // V2.3c:
        // Меньше damping = игрушка движется дольше,
        // красивее отскакивает и не "умирает" сразу.
        // -------------------------
        body.setLinearDamping(0.65f);
        body.setAngularDamping(1.1f);

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

            // -------------------------
            // V2.3c:
            // Ещё более строго:
            // игрушка должна реально успокоиться внутри,
            // иначе не считаем её выигранной.
            // -------------------------
            if (inside && speed < 0.08f && Math.abs(body.getAngularVelocity()) < 0.10f) {
                settleTimer += delta;
                if (settleTimer > 0.70f) {
                    lockInTray();
                }
            } else {
                settleTimer = 0f;
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

    // -------------------------
    // V2.3c:
    // earlyRelease = разжатие раньше времени по пути к лотку
    // Тогда игрушка летит плавнее и дольше.
    // -------------------------
    public void releaseToPhysicalTray(WinZone winZone, boolean missTray, boolean earlyRelease) {
        captured = false;
        releasedToPhysicsTray = true;
        inTray = false;
        won = false;
        settleTimer = 0f;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(1f);

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setRestitution(trayRestitution);
            fixture.setFriction(0.55f);
        }

        float vx;
        float vy;

        if (missTray) {
            // Явный промах
            vx = 2.8f + (float)Math.random() * 1.0f;
            vy = -1.15f - (float)Math.random() * 0.65f;
        } else {
            float dx = winZone.getCenterX() - body.getPosition().x;

            float sideNoise = ((float)Math.random() - 0.5f) * 1.65f;
            vx = dx * 1.15f + sideNoise + ((float)Math.random() - 0.5f) * trayScatterX * 4.5f;
            vy = -1.20f - (float)Math.random() * 0.70f;

            if (earlyRelease) {
                // Ранний сброс делаем более "дуговым":
                // меньше вниз, больше времени на полёт и столкновение
                vx += 0.45f + ((float)Math.random() - 0.5f) * 0.70f;
                vy -= 0.10f;
            }
        }

        body.setLinearVelocity(vx, vy);
        body.setAngularVelocity(((float)Math.random() - 0.5f) * 7.5f);
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
