package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class Claw {

    private enum State {
        IDLE, MOVE_DOWN, CLOSE, MOVE_UP, MOVE_TO_TRAY, OPEN, RETURN_HOME
    }

    private static final float HOME_X = GameTuning.CLAW_HOME_X;
    private static final float HOME_Y = GameTuning.CLAW_HOME_Y;
    private static final float TRAY_DROP_X = GameTuning.TRAY_DROP_X;
    private static final float DOWN_LIMIT_Y = GameTuning.CLAW_DOWN_LIMIT_Y;

    private static final float MOVE_SPEED_X = GameTuning.CLAW_MOVE_SPEED_X;
    private static final float MOVE_SPEED_Y = GameTuning.CLAW_MOVE_SPEED_Y;

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

    private Toy capturedToy;

    // =========================
    // 🔥 РАСЧЁТ СКОРОСТИ (ВАЖНО)
    // =========================
    private float prevX;
    private float velocityX;

    // =========================
    // 🔥 РАСКАЧКА
    // =========================
    private float swing = 0f;
    private float swingVelocity = 0f;

    public Claw() {
        headTexture = createRectTexture(110, 28, new Color(0.35f, 0.70f, 1f, 1f));
        fingerTexture = createRectTexture(18, 90, Color.WHITE);
        cableTexture = createRectTexture(6, 240, Color.LIGHT_GRAY);
    }

    public void create() {}

    public void update(float delta, List<Toy> toys, List<Toy> trayToys, WinZone winZone) {

        // =========================
        // считаем скорость клешни
        // =========================
        velocityX = (x - prevX) / Math.max(delta, 0.0001f);
        prevX = x;

        updateSwing(delta);

        // =========================
        // управление
        // =========================
        if (state == State.IDLE) {

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                x -= MOVE_SPEED_X * delta;
                swingVelocity += GameTuning.CLAW_SWING_FROM_MOVE * delta;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                x += MOVE_SPEED_X * delta;
                swingVelocity -= GameTuning.CLAW_SWING_FROM_MOVE * delta;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                state = State.MOVE_DOWN;
                stateTimer = 0;
            }
        }

        switch (state) {

            case MOVE_DOWN -> {
                y -= MOVE_SPEED_Y * delta;

                if (y <= DOWN_LIMIT_Y) {
                    y = DOWN_LIMIT_Y;
                    state = State.CLOSE;
                    stateTimer = 0;
                }
            }

            case CLOSE -> {
                stateTimer += delta;

                float t = Math.min(stateTimer / GameTuning.CLAW_CLOSE_TIME, 1f);
                fingerGap = lerp(FINGER_GAP_OPEN, FINGER_GAP_CLOSED, t);

                if (capturedToy == null) {
                    capturedToy = findToy(toys);

                    if (capturedToy != null) {
                        capturedToy.setCaptured(true);

                        // =========================
                        // 🔥 УДАР ПРИ ЗАХВАТЕ
                        // =========================
                        capturedToy.getBody().applyLinearImpulse(
                            new Vector2(
                                (float)(Math.random() - 0.5f) * GameTuning.CLAW_CATCH_IMPULSE_X,
                                GameTuning.CLAW_CATCH_IMPULSE_Y
                            ),
                            capturedToy.getBody().getWorldCenter(),
                            true
                        );
                    }
                }

                if (t >= 1f) {
                    state = State.MOVE_UP;
                }
            }

            case MOVE_UP -> {
                y += MOVE_SPEED_Y * delta;

                if (y >= HOME_Y) {
                    y = HOME_Y;
                    state = State.MOVE_TO_TRAY;
                }
            }

            case MOVE_TO_TRAY -> {
                float dx = TRAY_DROP_X - x;
                x += Math.signum(dx) * MOVE_SPEED_X * delta;

                if (Math.abs(dx) < 0.05f) {
                    state = State.OPEN;
                    stateTimer = 0;
                }
            }

            case OPEN -> {
                stateTimer += delta;

                float t = Math.min(stateTimer / GameTuning.CLAW_OPEN_TIME, 1f);
                fingerGap = lerp(FINGER_GAP_CLOSED, FINGER_GAP_OPEN, t);

                if (capturedToy != null) {

                    // =========================
                    // 🔥 ПЕРЕДАЧА ДВИЖЕНИЯ
                    // =========================
                    capturedToy.getBody().applyLinearImpulse(
                        new Vector2(
                            velocityX * GameTuning.CLAW_RELEASE_VX_TRANSFER,
                            GameTuning.CLAW_RELEASE_IMPULSE_Y
                        ),
                        capturedToy.getBody().getWorldCenter(),
                        true
                    );

                    capturedToy.releaseToPhysicalTray(winZone, false, false);
                    capturedToy = null;
                }

                if (t >= 1f) {
                    state = State.RETURN_HOME;
                }
            }

            case RETURN_HOME -> {
                float dx = HOME_X - x;
                x += Math.signum(dx) * MOVE_SPEED_X * delta;

                if (Math.abs(dx) < 0.05f) {
                    x = HOME_X;
                    state = State.IDLE;
                }
            }
        }

        // =========================
        // прикрепление игрушки
        // =========================
        if (capturedToy != null) {
            capturedToy.attachTo(x, y - 1.1f, swing);
        }
    }

    private Toy findToy(List<Toy> toys) {
        for (Toy t : toys) {
            if (!t.isCaptured()) return t;
        }
        return null;
    }

    // =========================
    // 🔥 ФИЗИКА РАСКАЧКИ
    // =========================
    private void updateSwing(float dt) {
        swingVelocity += -swing * GameTuning.CLAW_SWING_SPRING * dt;
        swingVelocity *= GameTuning.CLAW_SWING_DAMPING;
        swing += swingVelocity * dt;
    }

    public void render(SpriteBatch batch) {
        batch.draw(headTexture, x - 0.5f, y - 0.2f, 1f, 0.4f);

        float leftX = x - fingerGap / 2f;
        float rightX = x + fingerGap / 2f;

        batch.draw(fingerTexture, leftX, y - 1f, 0.2f, 1f);
        batch.draw(fingerTexture, rightX, y - 1f, 0.2f, 1f);
    }

    // =========================
    // геттеры
    // =========================
    public float getX() { return x; }
    public float getY() { return y; }
    public float getHomeY() { return HOME_Y; }
    public float getDownLimitY() { return DOWN_LIMIT_Y; }
    public float getFingerGap() { return fingerGap; }

    private static Texture createRectTexture(int w,int h, Color c){
        Pixmap p=new Pixmap(w,h, Pixmap.Format.RGBA8888);
        p.setColor(c); p.fill();
        Texture t=new Texture(p); p.dispose(); return t;
    }

    private static float lerp(float a,float b,float t){
        return a+(b-a)*t;
    }

    public void dispose() {
        headTexture.dispose();
        fingerTexture.dispose();
        cableTexture.dispose();
    }
}
