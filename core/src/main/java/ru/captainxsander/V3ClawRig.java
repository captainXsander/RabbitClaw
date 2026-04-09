package ru.captainxsander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

import java.util.List;

public class V3ClawRig {

    private final World world;

    private Body carriage;
    private Body claw;
    private Body leftFinger;
    private Body rightFinger;

    private DistanceJoint cable;
    private RevoluteJoint leftJoint;
    private RevoluteJoint rightJoint;

    private WeldJoint gripJoint;
    private V3Toy gripped;
    private float gripStrength = 0f;

    private float cableLen = V3Config.HOME_CABLE;

    private enum State { IDLE, DOWN, CLOSE, UP, TO_TRAY, OPEN, RETURN }
    private State state = State.IDLE;

    private float stateTimer = 0f;

    private final Texture rect = createRect();

    public V3ClawRig(World world) {
        this.world = world;
        create();
    }

    private void create() {
        BodyDef cDef = new BodyDef();
        cDef.type = BodyDef.BodyType.KinematicBody;
        cDef.position.set(V3Config.HOME_X, V3Config.CARRIAGE_Y);
        carriage = world.createBody(cDef);

        BodyDef clawDef = new BodyDef();
        clawDef.type = BodyDef.BodyType.DynamicBody;
        clawDef.position.set(V3Config.HOME_X, V3Config.CARRIAGE_Y - V3Config.HOME_CABLE);
        claw = world.createBody(clawDef);

        PolygonShape headShape = new PolygonShape();
        headShape.setAsBox(0.4f, 0.15f);
        claw.createFixture(headShape, 1f);
        headShape.dispose();

        DistanceJointDef dj = new DistanceJointDef();
        dj.bodyA = carriage;
        dj.bodyB = claw;
        dj.length = cableLen;
        cable = (DistanceJoint) world.createJoint(dj);

        leftFinger = createFinger(-0.3f);
        rightFinger = createFinger(0.3f);

        leftJoint = createFingerJoint(leftFinger, -0.3f, true);
        rightJoint = createFingerJoint(rightFinger, 0.3f, false);
    }

    private Body createFinger(float xOffset) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(claw.getPosition().x + xOffset, claw.getPosition().y - 0.4f);
        Body b = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.06f, 0.4f);
        b.createFixture(shape, 1f);
        shape.dispose();

        return b;
    }

    private RevoluteJoint createFingerJoint(Body finger, float xOffset, boolean left) {
        RevoluteJointDef def = new RevoluteJointDef();
        def.bodyA = claw;
        def.bodyB = finger;
        def.localAnchorA.set(xOffset, -0.1f);
        def.enableLimit = true;
        if (left) {
            def.lowerAngle = -0.6f;
            def.upperAngle = 0.1f;
        } else {
            def.lowerAngle = -0.1f;
            def.upperAngle = 0.6f;
        }
        def.enableMotor = true;
        def.maxMotorTorque = 4f;
        return (RevoluteJoint) world.createJoint(def);
    }

    public void update(float delta, List<V3Toy> toys, WinZone winZone) {
        stateTimer += delta;

        switch (state) {
            case IDLE -> {
                if (stateTimer > 0.2f) {
                    state = State.DOWN;
                    stateTimer = 0;
                }
            }
            case DOWN -> {
                cableLen = Math.min(V3Config.MAX_CABLE, cableLen + V3Config.CABLE_SPEED * delta);
                cable.setLength(cableLen);
                if (cableLen >= V3Config.MAX_CABLE) {
                    state = State.CLOSE;
                    stateTimer = 0;
                }
            }
            case CLOSE -> {
                closeFingers();
                tryGrab(toys);
                if (stateTimer > 0.6f) {
                    state = State.UP;
                    stateTimer = 0;
                }
            }
            case UP -> {
                cableLen = Math.max(V3Config.HOME_CABLE, cableLen - V3Config.CABLE_SPEED * delta);
                cable.setLength(cableLen);
                maybeSlip();
                if (cableLen <= V3Config.HOME_CABLE) {
                    state = State.TO_TRAY;
                }
            }
            case TO_TRAY -> {
                float dx = V3Config.TRAY_X - carriage.getPosition().x;
                carriage.setTransform(carriage.getPosition().x + Math.signum(dx)*delta*V3Config.CARRIAGE_SPEED,
                        carriage.getPosition().y, 0);
                maybeSlip();
                if (Math.abs(dx) < 0.1f) state = State.OPEN;
            }
            case OPEN -> {
                openFingers();
                release();
                if (stateTimer > 0.5f) state = State.RETURN;
            }
            case RETURN -> {
                float dx = V3Config.HOME_X - carriage.getPosition().x;
                carriage.setTransform(carriage.getPosition().x + Math.signum(dx)*delta*V3Config.CARRIAGE_SPEED,
                        carriage.getPosition().y, 0);
                if (Math.abs(dx) < 0.1f) {
                    state = State.IDLE;
                    stateTimer = 0;
                }
            }
        }
    }

    private void closeFingers() {
        leftJoint.setMotorSpeed(1f);
        rightJoint.setMotorSpeed(-1f);
    }

    private void openFingers() {
        leftJoint.setMotorSpeed(-1f);
        rightJoint.setMotorSpeed(1f);
    }

    private void tryGrab(List<V3Toy> toys) {
        if (gripped != null) return;

        for (V3Toy t : toys) {
            float dx = Math.abs(t.getBody().getPosition().x - claw.getPosition().x);
            if (dx < 0.2f) {
                WeldJointDef def = new WeldJointDef();
                def.bodyA = claw;
                def.bodyB = t.getBody();
                def.collideConnected = false;
                gripJoint = (WeldJoint) world.createJoint(def);
                gripped = t;
                gripStrength = 1f - dx*3f;
                break;
            }
        }
    }

    private void maybeSlip() {
        if (gripped == null) return;

        float angle = Math.abs(claw.getAngle());
        if (angle > 0.2f && Math.random() > gripStrength) {
            release();
        }
    }

    private void release() {
        if (gripJoint != null) {
            world.destroyJoint(gripJoint);
            gripJoint = null;
            gripped = null;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(rect, carriage.getPosition().x - 0.3f, carriage.getPosition().y, 0.6f, 0.1f);
        batch.draw(rect, claw.getPosition().x - 0.3f, claw.getPosition().y, 0.6f, 0.2f);

        batch.draw(rect, leftFinger.getPosition().x - 0.05f, leftFinger.getPosition().y - 0.4f, 0.1f, 0.8f);
        batch.draw(rect, rightFinger.getPosition().x - 0.05f, rightFinger.getPosition().y - 0.4f, 0.1f, 0.8f);
    }

    private Texture createRect() {
        Pixmap p = new Pixmap(10,10, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }
}
