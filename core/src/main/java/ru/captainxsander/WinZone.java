package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class WinZone {

    private Body sensorBody;
    private Body floorBody;
    private Body leftWallBody;
    private Body rightWallBody;

    private Texture bottomTexture;
    private Texture wallTexture;

    // -------------------------
    // V2.3c:
    // Лоток максимально вправо,
    // но не уходит за экран.
    // -------------------------
    private final float x = 14.15f;
    private final float y = 0.45f;

    private final float width = 2.45f;
    private final float height = 2.25f;

    public void create(World world) {
        bottomTexture = createRectTexture(300, 20, Color.LIME);
        wallTexture = createRectTexture(20, 260, Color.LIME);

        createSensor(world);
        createPhysicalWalls(world);
    }

    private void createSensor(World world) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(x, y + height * 0.5f);

        sensorBody = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * 0.5f - 0.08f, height * 0.5f - 0.08f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.isSensor = true;
        sensorBody.createFixture(fix);

        shape.dispose();
    }

    private void createPhysicalWalls(World world) {
        // Дно
        BodyDef floorDef = new BodyDef();
        floorDef.type = BodyDef.BodyType.StaticBody;
        floorDef.position.set(x, y + 0.04f);

        floorBody = world.createBody(floorDef);

        PolygonShape floorShape = new PolygonShape();
        floorShape.setAsBox(width * 0.5f, 0.04f);

        FixtureDef floorFix = new FixtureDef();
        floorFix.shape = floorShape;
        floorFix.friction = 0.32f;
        floorFix.restitution = 0.30f;
        floorBody.createFixture(floorFix);
        floorShape.dispose();

        // Левая стенка
        BodyDef leftDef = new BodyDef();
        leftDef.type = BodyDef.BodyType.StaticBody;
        leftDef.position.set(x - width * 0.5f + 0.04f, y + height * 0.5f);

        leftWallBody = world.createBody(leftDef);

        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(0.04f, height * 0.5f);

        FixtureDef leftFix = new FixtureDef();
        leftFix.shape = leftShape;
        leftFix.friction = 0.20f;
        leftFix.restitution = 0.80f;
        leftWallBody.createFixture(leftFix);
        leftShape.dispose();

        // Правая стенка
        BodyDef rightDef = new BodyDef();
        rightDef.type = BodyDef.BodyType.StaticBody;
        rightDef.position.set(x + width * 0.5f - 0.04f, y + height * 0.5f);

        rightWallBody = world.createBody(rightDef);

        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(0.04f, height * 0.5f);

        FixtureDef rightFix = new FixtureDef();
        rightFix.shape = rightShape;
        rightFix.friction = 0.20f;
        rightFix.restitution = 0.80f;
        rightWallBody.createFixture(rightFix);
        rightShape.dispose();
    }

    public void render(SpriteBatch batch) {
        batch.draw(bottomTexture, x - width * 0.5f, y, width, 0.08f);
        batch.draw(wallTexture, x - width * 0.5f, y, 0.08f, height);
        batch.draw(wallTexture, x + width * 0.5f - 0.08f, y, 0.08f, height);
    }

    public float getDropX() {
        // Чуть левее центра лотка, чтобы цеплять кромку
        return x - 0.48f;
    }

    public float getDropY() {
        // Выше, чтобы был длиннее видимый путь
        return y + height + 0.65f;
    }

    public float getCenterX() {
        return x;
    }

    public float getCenterY() {
        return y + height * 0.5f;
    }

    // Узкое "горло" зачёта
    public float getInnerLeft() {
        return x - 0.42f;
    }

    public float getInnerRight() {
        return x + 0.18f;
    }

    public float getInnerBottom() {
        return y + 0.12f;
    }

    public float getInnerTop() {
        return y + 1.30f;
    }

    public Body getSensorBody() {
        return sensorBody;
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
