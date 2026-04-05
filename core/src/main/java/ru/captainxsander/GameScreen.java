package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private World world;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Body clawLeft, clawRight;
    private List<Body> toys = new ArrayList<>();
    private WeldJoint clawJoint;
    private Texture clawTexture, toyTexture;

    private boolean toyCaptured = false;

    @Override
    public void show() {
        world = new World(new Vector2(0, -5f), true);
        camera = new OrthographicCamera(20, 20);
        camera.position.set(0, 10, 0);

        batch = new SpriteBatch();

        Pixmap clawPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        clawPixmap.setColor(Color.RED);
        clawPixmap.fillRectangle(0, 0, 64, 64);
        clawTexture = new Texture(clawPixmap);

        Pixmap toyPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        toyPixmap.setColor(Color.BLUE);
        toyPixmap.fillCircle(32, 32, 32);
        toyTexture = new Texture(toyPixmap);

        createClaw();
        createToys();
        createFloor();
    }

    private void createClaw() {
        float y = 15;

        // Левый палец
        BodyDef leftDef = new BodyDef();
        leftDef.type = BodyDef.BodyType.KinematicBody;
        leftDef.position.set(-1f, y);
        clawLeft = world.createBody(leftDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 1f);
        clawLeft.createFixture(shape, 0);
        shape.dispose();

        // Правый палец
        BodyDef rightDef = new BodyDef();
        rightDef.type = BodyDef.BodyType.KinematicBody;
        rightDef.position.set(1f, y);
        clawRight = world.createBody(rightDef);
        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(0.3f, 1f);
        clawRight.createFixture(shape2, 0);
        shape2.dispose();
    }

    private void createToys() {
        for (int i = -3; i <= 3; i+=2) {
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;
            def.position.set(i, 5);
            Body toy = world.createBody(def);

            CircleShape shape = new CircleShape();
            shape.setRadius(0.5f);

            FixtureDef fixture = new FixtureDef();
            fixture.shape = shape;
            fixture.density = 1f;
            fixture.friction = 0.5f;
            fixture.restitution = 0.3f;

            toy.createFixture(fixture);
            shape.dispose();

            toys.add(toy);
        }
    }

    private void handleInput() {
        float speed = 5f;

        // Влево-вправо
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            clawLeft.setLinearVelocity(-speed, 0);
            clawRight.setLinearVelocity(-speed, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            clawLeft.setLinearVelocity(speed, 0);
            clawRight.setLinearVelocity(speed, 0);
        } else {
            clawLeft.setLinearVelocity(0, 0);
            clawRight.setLinearVelocity(0, 0);
        }

        // Опустить клешню
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !toyCaptured) {
            clawLeft.setLinearVelocity(0, -15);
            clawRight.setLinearVelocity(0, -15);
        }

        // Поднять клешню
        if (toyCaptured && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            clawLeft.setLinearVelocity(0, 15);
            clawRight.setLinearVelocity(0, 15);
        }
    }

    private void checkCapture() {
        if (!toyCaptured) {
            for (Body toy : toys) {
                float leftX = clawLeft.getPosition().x;
                float rightX = clawRight.getPosition().x;
                float clawY = clawLeft.getPosition().y;

                float toyX = toy.getPosition().x;
                float toyY = toy.getPosition().y;

                if (toyY < clawY + 1 && toyY > clawY - 1 && toyX > leftX && toyX < rightX) {
                    WeldJointDef weldDef = new WeldJointDef();
                    weldDef.bodyA = clawLeft;
                    weldDef.bodyB = toy;
                    weldDef.collideConnected = false;
                    clawJoint = (WeldJoint) world.createJoint(weldDef);
                    toyCaptured = true;

                    clawLeft.setLinearVelocity(0, 0);
                    clawRight.setLinearVelocity(0, 0);
                    break;
                }
            }
        }
    }

    private void createFloor() {
        BodyDef floorDef = new BodyDef();
        floorDef.type = BodyDef.BodyType.StaticBody;
        floorDef.position.set(0, 1); // Y=1, чуть выше нуля

        Body floor = world.createBody(floorDef);

        PolygonShape floorShape = new PolygonShape();
        floorShape.setAsBox(10, 0.5f); // ширина 20 юнитов, высота 1

        floor.createFixture(floorShape, 0);
        floorShape.dispose();
    }

    @Override
    public void render(float delta) {
        handleInput();
        checkCapture();

        world.step(1/60f, 6, 2);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(clawTexture, clawLeft.getPosition().x - 0.3f, clawLeft.getPosition().y - 1f, 0.6f, 2f);
        batch.draw(clawTexture, clawRight.getPosition().x - 0.3f, clawRight.getPosition().y - 1f, 0.6f, 2f);
        for (Body toy : toys) {
            batch.draw(toyTexture, toy.getPosition().x - 0.5f, toy.getPosition().y - 0.5f, 1f, 1f);
        }
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        world.dispose();
        batch.dispose();
        clawTexture.dispose();
        toyTexture.dispose();
    }
}
