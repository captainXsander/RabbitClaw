package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

import java.util.List;

public class Claw {

    private static Texture texture = createClawTexture();

    private Body clawLeft;
    private Body clawRight;
    private WeldJoint clawJoint;
    private boolean toyCaptured;

    public void createClaw(World world) {

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

    private static Texture createClawTexture() {
        Pixmap clawPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        clawPixmap.setColor(Color.RED);
        clawPixmap.fillRectangle(0, 0, 64, 64);
        return new Texture(clawPixmap);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, clawLeft.getPosition().x - 0.3f, clawLeft.getPosition().y - 1f, 0.6f, 2f);
        batch.draw(texture, clawRight.getPosition().x - 0.3f, clawRight.getPosition().y - 1f, 0.6f, 2f);
    }

    public void update(World world, List<Toy> toys) {
        float speed = 5f;
        checkCapture(world, toys);
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

    private void checkCapture(World world, List<Toy> toys) {
        if (!toyCaptured) {
            for (Toy toy : toys) {
                float leftX = clawLeft.getPosition().x;
                float rightX = clawRight.getPosition().x;
                float clawY = clawLeft.getPosition().y;

                float toyX = toy.getToy().getPosition().x;
                float toyY = toy.getToy().getPosition().y;

                if (toyY < clawY + 1 && toyY > clawY - 1 && toyX > leftX && toyX < rightX) {
                    WeldJointDef weldDef = new WeldJointDef();
                    weldDef.bodyA = clawLeft;
                    weldDef.bodyB = toy.getToy();
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

    public static Texture getTexture() {
        return texture;
    }
}
