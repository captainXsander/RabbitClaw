package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class V3Toy {

    private final Body body;
    private final Texture texture;

    private boolean won = false;

    public V3Toy(World world, float x, float y, String texturePath) {
        texture = new Texture(Gdx.files.internal(texturePath));

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(V3Config.TOY_RADIUS);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.7f;
        fix.friction = 1.0f;
        fix.restitution = 0.05f;

        body.createFixture(fix);

        body.setLinearDamping(0.9f);
        body.setAngularDamping(1.2f);

        shape.dispose();
    }

    public void update(WinZone winZone) {
        if (won) return;

        Vector2 p = body.getPosition();
        if (p.x > winZone.getInnerLeft() && p.x < winZone.getInnerRight() &&
            p.y > winZone.getInnerBottom() && p.y < winZone.getInnerTop()) {
            if (body.getLinearVelocity().len() < 0.1f) {
                won = true;
                body.setType(BodyDef.BodyType.StaticBody);
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture,
                body.getPosition().x - 0.45f,
                body.getPosition().y - 0.45f,
                0.9f, 0.9f);
    }

    public Body getBody() { return body; }
}
