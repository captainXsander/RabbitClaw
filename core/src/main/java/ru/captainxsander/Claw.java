package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

import java.util.List;

public class Claw {

    private static Texture texture = createClawTexture();

    private Body clawLeft;
    private Body clawRight;
    private WeldJoint clawJoint;

    private boolean toyCaptured = false;
    private State state = State.IDLE;

    private float speed = 0.1f;

    public void createClaw(World world) {
        float y = 15;

        BodyDef leftDef = new BodyDef();
        leftDef.type = BodyDef.BodyType.KinematicBody;
        leftDef.position.set(-1f, y);
        clawLeft = world.createBody(leftDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 1f);
        clawLeft.createFixture(shape, 0);
        shape.dispose();

        BodyDef rightDef = new BodyDef();
        rightDef.type = BodyDef.BodyType.KinematicBody;
        rightDef.position.set(1f, y);
        clawRight = world.createBody(rightDef);

        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(0.3f, 1f);
        clawRight.createFixture(shape2, 0);
        shape2.dispose();
    }

    private static Texture createClawTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fillRectangle(0, 0, 64, 64);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public void update(World world, List<Toy> toys) {

        // Запуск цикла
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && state == State.IDLE) {
            state = State.GOING_DOWN;
            toyCaptured = false;
        }

        float vx = 0;

        // управление влево/вправо всегда доступно
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) vx = -speed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) vx = speed;

        float vy = 0;

        switch (state) {
            case IDLE:
                vy = 0;
                break;

            case GOING_DOWN:
                vy = -speed;

                // достигли низа → начинаем подниматься
                if (clawLeft.getPosition().y <= 2.5f) {
                    state = State.GOING_UP;
                }

                checkCapture(world, toys);
                break;

            case GOING_UP:
                vy = speed;

                // достигли верха → остановка
                if (clawLeft.getPosition().y >= 15f) {
                    state = State.IDLE;

                    // отпускаем игрушку (опционально)
                    if (clawJoint != null) {
                        world.destroyJoint(clawJoint);
                        clawJoint = null;
                        toyCaptured = false;
                    }
                }
                break;
        }

        clawLeft.setLinearVelocity(vx, vy);
        clawRight.setLinearVelocity(vx, vy);
    }

    private void checkCapture(World world, List<Toy> toys) {
        if (!toyCaptured) {
            for (Toy toy : toys) {

                float leftX = clawLeft.getPosition().x;
                float rightX = clawRight.getPosition().x;
                float clawY = clawLeft.getPosition().y;

                float toyX = toy.getToy().getPosition().x;
                float toyY = toy.getToy().getPosition().y;

                if (toyY < clawY + 1 && toyY > clawY - 1 &&
                    toyX > leftX && toyX < rightX) {

                    WeldJointDef weldDef = new WeldJointDef();
                    weldDef.bodyA = clawLeft;
                    weldDef.bodyB = toy.getToy();

                    clawJoint = (WeldJoint) world.createJoint(weldDef);
                    toyCaptured = true;
                    break;
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, clawLeft.getPosition().x - 0.3f,
            clawLeft.getPosition().y - 1f, 0.6f, 2f);

        batch.draw(texture, clawRight.getPosition().x - 0.3f,
            clawRight.getPosition().y - 1f, 0.6f, 2f);
    }
}
