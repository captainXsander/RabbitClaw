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

    private final float width = 0.90f;
    private final float height = 0.90f;

    public Toy(World world, float x, float y, String texturePath) {
        texture = new Texture(Gdx.files.internal(texturePath));

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.38f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.7f;
        fix.friction = 1.2f;
        fix.restitution = 0.02f;

        body.createFixture(fix);
        body.setLinearDamping(4f);
        body.setAngularDamping(6f);

        shape.dispose();
    }

    public void update(float delta) {
        if (captured || inTray) {
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

    public void attachTo(float x, float y) {
        captured = true;
        body.setTransform(x, y, 0f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    public void releaseIntoTray(float trayX, float trayY, int trayIndex) {
        captured = false;
        won = true;
        inTray = true;

        float offsetX;
        float offsetY;

        if (trayIndex == 0) {
            offsetX = -0.35f;
            offsetY = 0.18f;
        } else if (trayIndex == 1) {
            offsetX = 0.15f;
            offsetY = 0.18f;
        } else if (trayIndex == 2) {
            offsetX = -0.10f;
            offsetY = 0.55f;
        } else {
            offsetX = 0.28f * (trayIndex % 3);
            offsetY = 0.18f + 0.22f * (trayIndex / 3);
        }

        body.setTransform(trayX + offsetX, trayY + offsetY, 0f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0f);
        body.setType(BodyDef.BodyType.StaticBody);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture,
            body.getPosition().x - width / 2f,
            body.getPosition().y - height / 2f,
            width,
            height);
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void dispose() {
        texture.dispose();
    }
}
