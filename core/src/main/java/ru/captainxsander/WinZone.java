package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class WinZone {

    private Body body;
    private Texture texture;

    public void create(World world) {
        texture = createFrameTexture(220, 110, Color.LIME);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(13.3f, 1.05f);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1.1f, 0.55f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.isSensor = true;
        body.createFixture(fix);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, 12.2f, 0.5f, 2.2f, 1.1f);
    }

    public Body getBody() {
        return body;
    }

    private Texture createFrameTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);

        int t = 4;
        pixmap.fillRectangle(0, 0, width, t);
        pixmap.fillRectangle(0, height - t, width, t);
        pixmap.fillRectangle(0, 0, t, height);
        pixmap.fillRectangle(width - t, 0, t, height);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        texture.dispose();
    }
}
