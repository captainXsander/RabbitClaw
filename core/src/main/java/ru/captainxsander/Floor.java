package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Floor {

    private Body body;
    private Texture texture;

    public void create(World world) {
        texture = createRectTexture(1600, 42, new Color(0.20f, 0.72f, 0.24f, 1f));

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(8f, 0.35f);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f, 0.35f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.friction = 1.5f;
        fix.restitution = 0f;

        body.createFixture(fix);
        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, 0f, 0f, 16f, 0.7f);
    }

    private Texture createRectTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        texture.dispose();
    }
}
