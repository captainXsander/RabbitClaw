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
    private boolean droppingToTray = false;

    private final float width = 0.90f;
    private final float height = 0.90f;

    // параметры "характера" игрушки
    private final float catchDifficulty; // 0..1, больше = сложнее удержать
    private final float trayBounce;
    private final float trayScatterX;

    // логика красивого падения в лоток
    private float trayTargetX;
    private float trayTargetY;
    private float dropTimer = 0f;
    private float dropDuration = 0.42f;
    private float startDropX = 0f;
    private float startDropY = 0f;

    public Toy(World world, float x, float y, String texturePath) {
        this(world, x, y, texturePath, 0.25f, 0.10f, 0.15f);
    }

    public Toy(World world, float x, float y, String texturePath,
               float catchDifficulty, float trayBounce, float trayScatterX) {
        texture = new Texture(Gdx.files.internal(texturePath));

        this.catchDifficulty = catchDifficulty;
        this.trayBounce = trayBounce;
        this.trayScatterX = trayScatterX;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.38f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.7f;
        fix.friction = 1.15f;
        fix.restitution = 0.04f;

        body.createFixture(fix);
        body.setLinearDamping(3.2f);
        body.setAngularDamping(5.0f);

        shape.dispose();
    }

    public void update(float delta) {
        if (captured) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        if (droppingToTray) {
            updateDropToTray(delta);
            return;
        }

        if (inTray) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            return;
        }

        Vector2 v = body.getLinearVelocity();
        if (Math.abs(v.x) < 0.03f) {
            body.setLinearVelocity(0, v.y);
        }
        if (Math.abs(body.getAngularVelocity()) < 0.03f) {
            body.setAngularVelocity(0);
        }
    }

    private void updateDropToTray(float delta) {
        dropTimer += delta;
        float t = Math.min(1f, dropTimer / dropDuration);

        // плавное движение по X/Y
        float x = lerp(startDropX, trayTargetX, t);
        float y = lerp(startDropY, trayTargetY, t);

        // дуга + маленький bounce
        float arc = (float) Math.sin(t * Math.PI) * 0.42f;
        float bounce = 0f;

        if (t > 0.78f) {
            float local = (t - 0.78f) / 0.22f;
            bounce = (float) Math.sin(local * Math.PI * 2.2f) * trayBounce * (1f - local);
        }

        body.setTransform(x, y + arc + bounce, 0f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);

        if (t >= 1f) {
            droppingToTray = false;
            inTray = true;
            won = true;

            body.setTransform(trayTargetX, trayTargetY, 0f);
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            body.setGravityScale(0f);
            body.setType(BodyDef.BodyType.StaticBody);
        }
    }

    public void attachTo(float x, float y) {
        captured = true;
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

    public void startDropToTray(float trayX, float trayY, int trayIndex, boolean missTray) {
        captured = false;

        float offsetX;
        float offsetY;

        if (trayIndex == 0) {
            offsetX = -0.42f;
            offsetY = 0.18f;
        } else if (trayIndex == 1) {
            offsetX = 0.02f;
            offsetY = 0.18f;
        } else if (trayIndex == 2) {
            offsetX = 0.46f;
            offsetY = 0.18f;
        } else if (trayIndex == 3) {
            offsetX = -0.18f;
            offsetY = 0.58f;
        } else {
            offsetX = 0.28f * (trayIndex % 3);
            offsetY = 0.18f + 0.24f * (trayIndex / 3);
        }

        if (missTray) {
            offsetX += 1.15f + trayScatterX;
            offsetY += 0.15f;
        } else {
            offsetX += (float)(Math.random() - 0.5f) * trayScatterX;
        }

        startDropX = body.getPosition().x;
        startDropY = body.getPosition().y;

        trayTargetX = trayX + offsetX;
        trayTargetY = trayY + offsetY;

        dropTimer = 0f;
        droppingToTray = true;

        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0f);
        body.setType(BodyDef.BodyType.KinematicBody);
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

    public float getCatchDifficulty() {
        return catchDifficulty;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void dispose() {
        texture.dispose();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
