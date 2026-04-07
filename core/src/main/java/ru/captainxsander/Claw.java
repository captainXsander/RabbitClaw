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
        CLOSE,
        MOVE_UP,
        MOVE_TO_TRAY,
        OPEN,
        RETURN_HOME
    }

    private static final float HOME_X = 8.0f;
    private static final float HOME_Y = 7.7f;

    private static final float TRAY_DROP_X = 13.2f;
    private static final float DOWN_LIMIT_Y = 2.2f;

    private static final float MOVE_SPEED_X = 4.2f;
    private static final float MOVE_SPEED_Y = 5.2f;

    private static final float HEAD_W = 0.95f;
    private static final float HEAD_H = 0.28f;

    private static final float FINGER_W = 0.16f;
    private static final float FINGER_H = 0.95f;

    private static final float FINGER_GAP_OPEN = 0.92f;
    private static final float FINGER_GAP_CLOSED = 0.56f;

    private final Texture headTexture;
    private final Texture fingerTexture;
    private final Texture cableTexture;

    private float x = HOME_X;
    private float y = HOME_Y;

    private float fingerGap = FINGER_GAP_OPEN;

    private State state = State.IDLE;
    private float stateTimer = 0f;

    private Toy capturedToy;

    // Усиленное визуальное раскачивание
    private float swing = 0f;
    private float swingVelocity = 0f;

    public Claw() {
        headTexture = createRectTexture(110, 28, new Color(0.35f, 0.70f, 1f, 1f));
        fingerTexture = createRectTexture(18, 90, Color.WHITE);
        cableTexture = createRectTexture(6, 220, Color.LIGHT_GRAY);
    }

    public void create() {
    }

    public void update(float delta, List<Toy> toys, List<Toy> trayToys, WinZone winZone) {
        updateSwing(delta);

        if (state == State.IDLE) {
            handleIdleInput(delta);

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                state = State.MOVE_DOWN;
                stateTimer = 0f;

                // старт вниз тоже раскачивает
                swingVelocity += 0.9f;
            }
        }

        switch (state) {
            case MOVE_DOWN -> updateMoveDown(delta);
            case CLOSE -> updateClose(delta, toys);
            case MOVE_UP -> updateMoveUp(delta);
            case MOVE_TO_TRAY -> updateMoveToTray(delta);
            case OPEN -> updateOpen(delta, trayToys, winZone);
            case RETURN_HOME -> updateReturnHome(delta);
            case IDLE -> {
            }
        }

        if (capturedToy != null) {
            capturedToy.attachTo(x, y - 1.10f);
        }
    }

    private void handleIdleInput(float delta) {
        float oldX = x;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= MOVE_SPEED_X * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += MOVE_SPEED_X * delta;
        }

        x = clamp(x, 2.0f, 12.0f);

        float dx = x - oldX;
        if (Math.abs(dx) > 0.0001f) {
            swingVelocity += dx * 20f;
        }
    }

    private void updateMoveDown(float delta) {
        y -= MOVE_SPEED_Y * delta;
        if (y <= DOWN_LIMIT_Y) {
            y = DOWN_LIMIT_Y;
            state = State.CLOSE;
            stateTimer = 0f;
        }
    }

    private void updateClose(float delta, List<Toy> toys) {
        stateTimer += delta;

        float progress = clamp(stateTimer / 0.22f, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_OPEN, FINGER_GAP_CLOSED, progress);

        if (capturedToy == null) {
            for (Toy toy : toys) {
                if (toy.isWon() || toy.isCaptured()) continue;

                if (isToyCatchable(toy)) {
                    capturedToy = toy;
                    capturedToy.setCaptured(true);
                    break;
                }
            }
        }

        if (stateTimer >= 0.22f) {
            state = State.MOVE_UP;
            stateTimer = 0f;
            swingVelocity -= 0.7f;
        }
    }

    private boolean isToyCatchable(Toy toy) {
        float toyX = toy.getX();
        float toyY = toy.getY();

        float leftEdge = x - fingerGap * 0.5f;
        float rightEdge = x + fingerGap * 0.5f;

        boolean insideX = toyX > leftEdge && toyX < rightEdge;
        boolean closeY = Math.abs(toyY - (y - 0.9f)) < 0.65f;

        return insideX && closeY;
    }

    private void updateMoveUp(float delta) {
        y += MOVE_SPEED_Y * delta;
        if (y >= HOME_Y) {
            y = HOME_Y;
            state = State.MOVE_TO_TRAY;
        }
    }

    private void updateMoveToTray(float delta) {
        float oldX = x;
        float dx = TRAY_DROP_X - x;

        if (Math.abs(dx) < 0.04f) {
            x = TRAY_DROP_X;
            state = State.OPEN;
            stateTimer = 0f;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;

        float moved = x - oldX;
        swingVelocity += moved * 20f;
    }

    private void updateOpen(float delta, List<Toy> trayToys, WinZone winZone) {
        stateTimer += delta;

        float progress = clamp(stateTimer / 0.24f, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_CLOSED, FINGER_GAP_OPEN, progress);

        if (capturedToy != null) {
            Toy toy = capturedToy;
            toy.releaseIntoTray(winZone.getDropX(), winZone.getDropY(), trayToys.size());
            trayToys.add(toy);
            capturedToy = null;

            swingVelocity -= 0.5f;
        }

        if (stateTimer >= 0.24f) {
            state = State.RETURN_HOME;
            stateTimer = 0f;
        }
    }

    private void updateReturnHome(float delta) {
        float oldX = x;
        float dx = HOME_X - x;

        if (Math.abs(dx) < 0.04f) {
            x = HOME_X;
            fingerGap = FINGER_GAP_OPEN;
            state = State.IDLE;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;

        float moved = x - oldX;
        swingVelocity += moved * 18f;
    }

    private void updateSwing(float delta) {
        // Сильнее амплитуда, медленнее затухание
        swingVelocity += (-swing * 9f) * delta;
        swingVelocity *= 0.987f;
        swing += swingVelocity * delta;

        swing = clamp(swing, -0.42f, 0.42f);
    }

    public void render(SpriteBatch batch) {
        float cableLen = Math.max(0.2f, 9f - y);
        float swingDeg = (float) Math.toDegrees(swing);

        batch.draw(
            cableTexture,
            x - 0.03f, y,
            0.03f, 0f,
            0.06f, cableLen,
            1f, 1f,
            swingDeg,
            0, 0,
            cableTexture.getWidth(), cableTexture.getHeight(),
            false, false
        );

        batch.draw(
            headTexture,
            x - HEAD_W / 2f, y - HEAD_H / 2f,
            HEAD_W / 2f, HEAD_H / 2f,
            HEAD_W, HEAD_H,
            1f, 1f,
            swingDeg,
            0, 0,
            headTexture.getWidth(), headTexture.getHeight(),
            false, false
        );

        float fingerTopY = y - 0.05f;
        float leftX = x - fingerGap / 2f - FINGER_W / 2f;
        float rightX = x + fingerGap / 2f - FINGER_W / 2f;

        float openAmount = (fingerGap - FINGER_GAP_CLOSED) / (FINGER_GAP_OPEN - FINGER_GAP_CLOSED);
        float leftAngle = -20f + openAmount * 24f + swingDeg * 0.55f;
        float rightAngle = 20f - openAmount * 24f + swingDeg * 0.55f;

        batch.draw(
            fingerTexture,
            leftX, fingerTopY - FINGER_H,
            FINGER_W / 2f, FINGER_H,
            FINGER_W, FINGER_H,
            1f, 1f,
            leftAngle,
            0, 0,
            fingerTexture.getWidth(), fingerTexture.getHeight(),
            false, false
        );

        batch.draw(
            fingerTexture,
            rightX, fingerTopY - FINGER_H,
            FINGER_W / 2f, FINGER_H,
            FINGER_W, FINGER_H,
            1f, 1f,
            rightAngle,
            0, 0,
            fingerTexture.getWidth(), fingerTexture.getHeight(),
            false, false
        );
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

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void dispose() {
        headTexture.dispose();
        fingerTexture.dispose();
        cableTexture.dispose();
    }
}
