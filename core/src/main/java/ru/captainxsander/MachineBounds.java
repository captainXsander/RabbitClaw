package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class MachineBounds {

    private Body leftWall;
    private Body rightWall;
    private Body topWall;

    private Texture wallTexture;

    public void create(World world) {
        wallTexture = createRectTexture(20, 300, Color.DARK_GRAY);

        // =========================
        // ЛЕВАЯ СТЕНА
        // =========================
        BodyDef leftDef = new BodyDef();
        leftDef.type = BodyDef.BodyType.StaticBody;
        leftDef.position.set(0.3f, 4.5f);

        leftWall = world.createBody(leftDef);

        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(0.3f, 4.5f);

        leftWall.createFixture(createWallFix(leftShape));
        leftShape.dispose();

        // =========================
        // ПРАВАЯ СТЕНА
        // =========================
        BodyDef rightDef = new BodyDef();
        rightDef.type = BodyDef.BodyType.StaticBody;
        rightDef.position.set(15.7f, 4.5f);

        rightWall = world.createBody(rightDef);

        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(0.3f, 4.5f);

        rightWall.createFixture(createWallFix(rightShape));
        rightShape.dispose();

        // =========================
        // ВЕРХ (чтобы не вылетали)
        // =========================
        BodyDef topDef = new BodyDef();
        topDef.type = BodyDef.BodyType.StaticBody;
        topDef.position.set(8f, 8.7f);

        topWall = world.createBody(topDef);

        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(8f, 0.3f);

        topWall.createFixture(createWallFix(topShape));
        topShape.dispose();
    }

    private FixtureDef createWallFix(Shape shape) {
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.friction = 0.4f;
        fix.restitution = 0.3f; // 🔥 отскоки
        return fix;
    }

    public void render(SpriteBatch batch) {
        // чисто визуал (можешь потом заменить ассетом)
        batch.draw(wallTexture, 0f, 0f, 0.6f, 9f);
        batch.draw(wallTexture, 15.4f, 0f, 0.6f, 9f);
        batch.draw(wallTexture, 0f, 8.4f, 16f, 0.6f);
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
        wallTexture.dispose();
    }
}
