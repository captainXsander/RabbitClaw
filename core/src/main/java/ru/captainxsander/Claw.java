package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public class Claw {

    private enum State {
        IDLE,
        MOVE_DOWN,
        PICK,
        MOVE_UP,
        MOVE_TO_DROP,
        RELEASE,
        RETURN_HOME
    }

    private static final float HOME_X = 8.0f;
    private static final float HOME_Y = 7.8f;

    private static final float DROP_X = 13.3f;
    private static final float DROP_Y = 2.2f;

    private static final float DOWN_LIMIT_Y = 2.15f;

    private static final float MOVE_SPEED_X = 4.0f;
    private static final float MOVE_SPEED_Y = 5.0f;

    private static final float BODY_W = 0.9f;
    private static final float BODY_H = 0.35f;

    private static final float FINGER_W = 0.18f;
    private static final float FINGER_H = 1.0f;
    private static final float FINGER_GAP = 0.95f;

    private final Texture bodyTexture;
    private final Texture fingerTexture;
    private final Texture cableTexture;

    private float x = HOME_X;
    private float y = HOME_Y;

    private State state = State.IDLE;

    private Toy capturedToy;
    private float stateTimer = 0f;

    public Claw() {
        bodyTexture = createRectTexture(90, 35, Color.SKY);
        fingerTexture = createRectTexture(18, 90, Color.WHITE);
        cableTexture = createRectTexture(6, 200, Color.LIGHT_GRAY);
    }

    public void create() {
        // ничего не нужно, оставил метод для единообразия
    }

    public void update(float delta, List<Toy> toys, WinZone winZone) {
        if (state == State.IDLE) {
            handleHorizontalInput(delta);

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                state = State.MOVE_DOWN;
                stateTimer = 0f;
            }
        }

        switch (state) {
            case MOVE_DOWN -> moveDown(delta);
            case PICK -> pick(toys, delta);
            case MOVE_UP -> moveUp(delta);
            case MOVE_TO_DROP -> moveToDrop(delta);
            case RELEASE -> release(delta);
            case RETURN_HOME -> returnHome(delta);
            case IDLE -> {
            }
        }

        if (capturedToy != null) {
            capturedToy.attachTo(x, y - 1.2f);
        }
    }

    private void handleHorizontalInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= MOVE_SPEED_X * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += MOVE_SPEED_X * delta;
        }

        x = clamp(x, 2.0f, 12.2f);
    }

    private void moveDown(float delta) {
        y -= MOVE_SPEED_Y * delta;
        if (y <= DOWN_LIMIT_Y) {
            y = DOWN_LIMIT_Y;
            state = State.PICK;
            stateTimer = 0f;
        }
    }

    private void pick(List<Toy> toys, float delta) {
        stateTimer += delta;

        if (capturedToy == null) {
            for (Toy toy : toys) {
                if (toy.isWon()) continue;

                if (isToyInsideClaw(toy)) {
                    capturedToy = toy;
                    toy.setCaptured(true);
                    break;
                }
            }
        }

        if (stateTimer >= 0.25f) {
            state = State.MOVE_UP;
        }
    }

    private boolean isToyInsideClaw(Toy toy) {
        float toyX = toy.getX();
        float toyY = toy.getY();

        float leftEdge = x - FINGER_GAP * 0.5f;
        float rightEdge = x + FINGER_GAP * 0.5f;

        return toyX > leftEdge && toyX < rightEdge && Math.abs(toyY - (y - 0.9f)) < 0.8f;
    }

    private void moveUp(float delta) {
        y += MOVE_SPEED_Y * delta;
        if (y >= HOME_Y) {
            y = HOME_Y;
            state = State.MOVE_TO_DROP;
        }
    }

    private void moveToDrop(float delta) {
        float dx = DROP_X - x;
        if (Math.abs(dx) < 0.05f) {
            x = DROP_X;
            state = State.RELEASE;
            stateTimer = 0f;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;
    }

    private void release(float delta) {
        stateTimer += delta;

        if (capturedToy != null) {
            capturedToy.release();
            if (capturedToy.getX() > 12.4f && capturedToy.getY() < 2.4f) {
                capturedToy.setWon(true);
            }
            capturedToy = null;
        }

        if (stateTimer >= 0.15f) {
            state = State.RETURN_HOME;
        }
    }

    private void returnHome(float delta) {
        float dx = HOME_X - x;

        if (Math.abs(dx) < 0.05f) {
            x = HOME_X;
            state = State.IDLE;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;
    }

    public void render(SpriteBatch batch) {
        float cableHeight = Math.max(0.2f, 9f - y);
        batch.draw(cableTexture, x - 0.03f, y, 0.06f, cableHeight);

        batch.draw(bodyTexture, x - BODY_W / 2f, y - BODY_H / 2f, BODY_W, BODY_H);

        float fingerTopY = y - 0.1f;
        batch.draw(fingerTexture,
            x - FINGER_GAP / 2f - FINGER_W / 2f,
            fingerTopY - FINGER_H,
            FINGER_W,
            FINGER_H);

        batch.draw(fingerTexture,
            x + FINGER_GAP / 2f - FINGER_W / 2f,
            fingerTopY - FINGER_H,
            FINGER_W,
            FINGER_H);
    }

    private static Texture createRectTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void dispose() {
        bodyTexture.dispose();
        fingerTexture.dispose();
        cableTexture.dispose();
    }
}
