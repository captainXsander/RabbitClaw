package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class WinZone {

    private Body body;

    private Texture bottomTexture;
    private Texture wallTexture;

    private final float x = 13.15f;
    private final float y = 0.45f;

    private final float width = 2.9f;
    private final float height = 2.25f;

    public void create(World world) {
        bottomTexture = createRectTexture(320, 20, Color.LIME);
        wallTexture = createRectTexture(20, 260, Color.LIME);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(x, y + height * 0.5f);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f, height * 0.5f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.isSensor = true;
        body.createFixture(fix);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        // дно
        batch.draw(bottomTexture, x - width * 0.5f, y, width, 0.08f);

        // левая стенка
        batch.draw(wallTexture, x - width * 0.5f, y, 0.08f, height);

        // правая стенка
        batch.draw(wallTexture, x + width * 0.5f - 0.08f, y, 0.08f, height);
    }

    public float getDropX() {
        return x;
    }

    public float getDropY() {
        return y + 0.22f;
    }

    public Body getBody() {
        return body;
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
        bottomTexture.dispose();
        wallTexture.dispose();
    }
}
