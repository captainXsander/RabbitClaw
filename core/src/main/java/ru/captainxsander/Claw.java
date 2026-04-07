package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public class Claw {

    // V2: state machine
    private enum State {
        IDLE, MOVE_DOWN, CLOSE, MOVE_UP, MOVE_TO_TRAY, OPEN, RETURN_HOME
    }

    private static final float HOME_X = GameTuning.CLAW_HOME_X;
    private static final float HOME_Y = GameTuning.CLAW_HOME_Y;
    private static final float TRAY_DROP_X = GameTuning.TRAY_DROP_X;
    private static final float DOWN_LIMIT_Y = GameTuning.CLAW_DOWN_LIMIT_Y;

    private static final float MOVE_SPEED_X = GameTuning.CLAW_MOVE_SPEED_X;
    private static final float MOVE_SPEED_Y = GameTuning.CLAW_MOVE_SPEED_Y;

    private static final float HEAD_W = GameTuning.CLAW_HEAD_W;
    private static final float HEAD_H = GameTuning.CLAW_HEAD_H;

    private static final float FINGER_W = GameTuning.CLAW_FINGER_W;
    private static final float FINGER_H = GameTuning.CLAW_FINGER_H;

    private static final float FINGER_GAP_OPEN = GameTuning.CLAW_FINGER_GAP_OPEN;
    private static final float FINGER_GAP_CLOSED = GameTuning.CLAW_FINGER_GAP_CLOSED;

    private final Texture headTexture;
    private final Texture fingerTexture;
    private final Texture cableTexture;

    private float x = HOME_X;
    private float y = HOME_Y;
    private float fingerGap = FINGER_GAP_OPEN;

    private State state = State.IDLE;
    private float stateTimer = 0f;

    // V1 -> V2: логический захват
    private Toy capturedToy;

    // V2: визуальная раскачка
    private float swing = 0f;
    private float swingVelocity = 0f;

    private boolean slipCheckedThisCycle = false;
    private boolean earlyReleaseCheckedThisCycle = false;

    public Claw() {
        headTexture = createRectTexture(110, 28, new Color(0.35f, 0.70f, 1f, 1f));
        fingerTexture = createRectTexture(18, 90, Color.WHITE);
        cableTexture = createRectTexture(6, 240, Color.LIGHT_GRAY);
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
                slipCheckedThisCycle = false;
                earlyReleaseCheckedThisCycle = false;
                swingVelocity += 1.15f;
            }
        }

        switch (state) {
            case MOVE_DOWN -> updateMoveDown(delta);
            case CLOSE -> updateClose(delta, toys, trayToys);
            case MOVE_UP -> updateMoveUp(delta);
            case MOVE_TO_TRAY -> updateMoveToTray(delta, trayToys, winZone);
            case OPEN -> updateOpen(delta, trayToys, winZone);
            case RETURN_HOME -> updateReturnHome(delta);
            case IDLE -> { }
        }

        if (capturedToy != null) {
            capturedToy.attachTo(x, y - 1.10f);
        }
    }

    private void handleIdleInput(float delta) {
        float oldX = x;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= MOVE_SPEED_X * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += MOVE_SPEED_X * delta;

        x = clamp(x, 2.0f, 12.0f);

        float dx = x - oldX;
        if (Math.abs(dx) > 0.0001f) {
            // Заметная раскачка при движении
            swingVelocity += dx * GameTuning.SWING_INPUT_MULTIPLIER;
        }
    }

    private void updateMoveDown(float delta) {
        y -= MOVE_SPEED_Y * delta;
        if (y <= DOWN_LIMIT_Y) {
            y = DOWN_LIMIT_Y;
            state = State.CLOSE;
            stateTimer = 0f;
            swingVelocity -= 0.75f;
        }
    }

    private void updateClose(float delta, List<Toy> toys, List<Toy> trayToys) {
        stateTimer += delta;
        float progress = clamp(stateTimer / GameTuning.CLAW_CLOSE_TIME, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_OPEN, FINGER_GAP_CLOSED, progress);

        if (capturedToy == null) {
            capturedToy = findCatchableToy(toys);
            if (capturedToy == null) {
                capturedToy = findCatchableToy(trayToys);
            }

            if (capturedToy != null) {
                capturedToy.setCaptured(true);
                swingVelocity += 0.45f;
            }
        }

        if (stateTimer >= GameTuning.CLAW_CLOSE_TIME) {
            state = State.MOVE_UP;
            stateTimer = 0f;
        }
    }

    private Toy findCatchableToy(List<Toy> source) {
        for (Toy toy : source) {
            // Можно снова поднимать игрушки, упавшие мимо лотка
            if (toy.isWon() || toy.isCaptured() || toy.isInTray()) continue;
            if (isToyCatchable(toy) && passesCatchChance(toy)) return toy;
        }
        return null;
    }

    private boolean passesCatchChance(Toy toy) {
        float chance = 1f - toy.getCatchDifficulty();
        return Math.random() < chance;
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

        if (capturedToy != null && !slipCheckedThisCycle && y > GameTuning.SLIP_CHECK_Y) {
            slipCheckedThisCycle = true;

            double slipChance = GameTuning.BASE_SLIP_CHANCE
                + capturedToy.getCatchDifficulty() * GameTuning.SLIP_DIFFICULTY_MULT;

            if (Math.random() < slipChance) {
                Toy toy = capturedToy;
                capturedToy = null;

                toy.releaseFailedGrab((float)(Math.random() * 0.7 - 0.35), -0.10f);
                swingVelocity -= 0.55f;
            }
        }

        if (y >= HOME_Y) {
            y = HOME_Y;
            state = State.MOVE_TO_TRAY;
            swingVelocity += 0.30f;
        }
    }

    private void updateMoveToTray(float delta, List<Toy> trayToys, WinZone winZone) {
        float oldX = x;
        float dx = TRAY_DROP_X - x;

        // Ранний сброс по дороге
        if (capturedToy != null && !earlyReleaseCheckedThisCycle && x > GameTuning.EARLY_RELEASE_CHECK_X) {
            earlyReleaseCheckedThisCycle = true;

            double earlyReleaseChance = GameTuning.BASE_EARLY_RELEASE_CHANCE
                + capturedToy.getCatchDifficulty() * GameTuning.EARLY_RELEASE_DIFFICULTY_MULT;

            if (Math.random() < earlyReleaseChance) {
                Toy toy = capturedToy;
                capturedToy = null;

                boolean missTray = Math.random() < 0.58;
                toy.releaseToPhysicalTray(winZone, missTray, true);

                if (!trayToys.contains(toy)) trayToys.add(toy);

                fingerGap = FINGER_GAP_OPEN;
                swingVelocity -= 0.75f;
            }
        }

        if (Math.abs(dx) < 0.04f) {
            x = TRAY_DROP_X;
            state = State.OPEN;
            stateTimer = 0f;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;

        float moved = x - oldX;
        swingVelocity += moved * 22f;
    }

    private void updateOpen(float delta, List<Toy> trayToys, WinZone winZone) {
        stateTimer += delta;
        float progress = clamp(stateTimer / GameTuning.CLAW_OPEN_TIME, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_CLOSED, FINGER_GAP_OPEN, progress);

        if (capturedToy != null) {
            Toy toy = capturedToy;

            boolean missTray = Math.random() < (
                GameTuning.BASE_TRAY_MISS_CHANCE
                    + toy.getCatchDifficulty() * GameTuning.TRAY_MISS_DIFFICULTY_MULT
            );

            toy.releaseToPhysicalTray(winZone, missTray, false);

            if (!trayToys.contains(toy)) trayToys.add(toy);

            capturedToy = null;
            swingVelocity -= 0.60f;
        }

        if (stateTimer >= GameTuning.CLAW_OPEN_TIME) {
            state = State.RETURN_HOME;
            stateTimer = 0f;
        }
    }

    private void updateReturnHome(float delta) {
        float oldX = x;
        float dx = HOME_X - x;

        if (Math.abs(dx) < 0.05f) {
            x = HOME_X;
            fingerGap = FINGER_GAP_OPEN;

            // Быстро гасим остаточные колебания
            swing *= 0.10f;
            swingVelocity *= 0.04f;
            state = State.IDLE;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;

        float moved = x - oldX;
        swingVelocity += moved * 10f;
    }

    private void updateSwing(float delta) {
        // Заметно качается, но быстро возвращается в вертикаль
        swingVelocity += (-swing * GameTuning.SWING_SPRING) * delta;
        swingVelocity *= GameTuning.SWING_DAMPING;
        swing += swingVelocity * delta;

        if (Math.abs(swing) < GameTuning.SWING_STOP_EPS
            && Math.abs(swingVelocity) < GameTuning.SWING_STOP_EPS) {
            swing = 0f;
            swingVelocity = 0f;
        }

        swing = clamp(swing, -GameTuning.SWING_MAX, GameTuning.SWING_MAX);
    }

    public void render(SpriteBatch batch) {
        float cableLen = Math.max(0.2f, 9f - y);
        float swingDeg = (float)Math.toDegrees(swing);

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
        float leftAngle = -20f + openAmount * 24f + swingDeg * 0.42f;
        float rightAngle = 20f - openAmount * 24f + swingDeg * 0.42f;

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

    // Геттеры для overlay
    public float getX() { return x; }
    public float getY() { return y; }
    public float getHomeY() { return HOME_Y; }
    public float getDownLimitY() { return DOWN_LIMIT_Y; }

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
