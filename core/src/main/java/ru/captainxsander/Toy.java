package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Toy {

    private static Texture texture = createToyTexture();

    public Body getToy() {
        return toy;
    }

    private Body toy;

    private static Texture createToyTexture() {
        Pixmap toyPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        toyPixmap.setColor(Color.BLUE);
        toyPixmap.fillCircle(32, 32, 32);
        return new Texture(toyPixmap);
    }

    public void createToy(World world, float xPosition) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(xPosition, 5);
        toy = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = 0.5f;
        fixture.restitution = 0.3f;

        toy.createFixture(fixture);
        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, toy.getPosition().x - 0.5f, toy.getPosition().y - 0.5f, 1f, 1f);
    }

    public static Texture getTexture() {
        return texture;
    }
}
