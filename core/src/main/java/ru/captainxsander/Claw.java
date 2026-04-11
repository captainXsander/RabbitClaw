package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.List;

import static ru.captainxsander.GameTuning.*;

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

    private static final float HEAD_W = GameTuning.CLAW_HEAD_W;
    private static final float HEAD_H = GameTuning.CLAW_HEAD_H;

    private static final float FINGER_W = GameTuning.CLAW_FINGER_W;
    private static final float FINGER_H = GameTuning.CLAW_FINGER_H;

    private static final float FINGER_GAP_OPEN = GameTuning.CLAW_FINGER_GAP_OPEN;
    private static final float FINGER_GAP_CLOSED = GameTuning.CLAW_FINGER_GAP_CLOSED;

    private Body physicsBody;
    private World world;

    private final Texture headTexture;
    private final Texture fingerTexture;
    private final Texture cableTexture;

    private float x = HOME_X;
    private float y = HOME_Y;
    private float fingerGap = FINGER_GAP_OPEN;
    private float swingOffsetX = 0f;

    private State state = State.IDLE;
    private float stateTimer = 0f;

    private Toy capturedToy;

    // =========================
    // 🔥 ТРОС (маятник)
    // =========================
    private float swing = 0f;
    private float swingVelocity = 0f;
    private float lastInputVelocity = 0f;
    // =========================
    // 🔥 ГОЛОВА (запаздывает за тросом)
    // =========================
    private float headSwing = 0f;
    private float headSwingVelocity = 0f;

    // 🔥 скорость клешни по X (передаётся игрушке)
    private float velocityX = 0f;

    private float fingerAngleLeft = -20f;
    private float fingerAngleRight = 20f;
    private float fingerAngleVelLeft = 0f;
    private float fingerAngleVelRight = 0f;

    private boolean slipCheckedThisCycle = false;
    private boolean earlyReleaseCheckedThisCycle = false;
    private boolean triedToCatch = false;
    private boolean hasMovedDown = false;
    private float pressDepth = 0f;

    public Claw() {
        headTexture = createRectTexture(110, 28, new Color(0.35f, 0.70f, 1f, 1f));
        fingerTexture = createRectTexture(18, 90, Color.WHITE);
        cableTexture = createRectTexture(6, 240, Color.LIGHT_GRAY);
    }

    public void createPhysics(World world) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.KinematicBody;
        def.position.set(getRealX(), y - 0.9f);

        physicsBody = world.createBody(def);
        physicsBody.setBullet(true);

        PolygonShape shape = new PolygonShape();

        // Небольшой "толкатель" в зоне головы/пальцев.
        // Пока делаем только один прямоугольник, без физических пальцев.
        shape.setAsBox(0.30f, 0.12f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 1f;
        fix.friction = 0.4f;
        fix.restitution = 0.08f;

        physicsBody.createFixture(fix);
        shape.dispose();
    }

    public void update(float delta, List<Toy> toys, List<Toy> trayToys, WinZone winZone) {
        // 👉 ВСЕГДА синхронизируем физическое тело с логикой
        if (physicsBody != null) {
            physicsBody.setTransform(getRealX(), y - 0.7f, 0f);
            physicsBody.setLinearVelocity(0f, 0f);
            physicsBody.setAngularVelocity(0f);
        }
        if (state == State.IDLE) {
            handleIdleInput(delta);

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                hasMovedDown = false;
                capturedToy = null;
                pressDepth = 0f;

                state = State.MOVE_DOWN;
                stateTimer = 0f;

                slipCheckedThisCycle = false;
                earlyReleaseCheckedThisCycle = false;

                if (physicsBody != null) {
                    physicsBody.setLinearVelocity(0f, 0f);
                    physicsBody.setTransform(getRealX(), y - 0.7f, 0f);
                }

                boolean noInput =
                    !Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
                        !Gdx.input.isKeyPressed(Input.Keys.RIGHT);

                if (noInput && Math.abs(swing) < 0.05f && Math.abs(swingVelocity) < 0.05f) {
                    swing = 0f;
                    swingVelocity = 0f;
                }
            }
        }

        switch (state) {
            case MOVE_DOWN -> updateMoveDown(delta);
            case CLOSE -> updateClose(delta, toys, trayToys);
            case MOVE_UP -> updateMoveUp(delta);
            case MOVE_TO_TRAY -> updateMoveToTray(delta, trayToys, winZone);
            case OPEN -> updateOpen(delta, trayToys, winZone);
            case RETURN_HOME -> updateReturnHome(delta);
            case IDLE -> {
            }
        }

        // "приклеиваем" игрушку к клешне
        if (capturedToy != null) {
            capturedToy.attachTo(getRealX(), y - 1.10f, headSwing);
        }
        updateSwing(delta);
    }

    private void handleIdleInput(float delta) {
        float oldX = x;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= MOVE_SPEED_X * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += MOVE_SPEED_X * delta;

        x = clamp(x, 2.0f, 12.0f);

        float dx = x - oldX;
        float inputVelocity = dx / delta;

        // длина троса
        float cableLen = Math.max(0.2f, 9f - y);
        float lengthFactor = cableLen / 6f;

        // =========================
        // 🔥 0. СЛАБЫЙ БАЗОВЫЙ ИМПУЛЬС (очень важен!)
        // =========================
        swingVelocity += dx * SWING_INPUT_BASE * lengthFactor;

        // =========================
        // 🔥 1. РЫВОК
        // =========================
        float accel = (inputVelocity - lastInputVelocity);

        if (Math.abs(accel) > 2.0f) { // ↓ БЫЛО 6 → стало 2
            swingVelocity += accel * SWING_ACCEL_MULT * lengthFactor;
        }

        // =========================
        // 🔥 2. СМЕНА НАПРАВЛЕНИЯ
        // =========================
        if (Math.signum(inputVelocity) != Math.signum(lastInputVelocity)
            && Math.abs(inputVelocity) > 0.2f) { // ↓ БЫЛО 1.0 → стало 0.2

            float phaseBoost = (float) Math.cos(swing);

            swingVelocity += inputVelocity * SWING_DIRECTION_CHANGE_MULT * lengthFactor * phaseBoost;
        }

        lastInputVelocity = inputVelocity;

        velocityX = 0f;
    }

    private void updateMoveDown(float delta) {
        y -= MOVE_SPEED_Y * delta;
        if (y < HOME_Y - CLAW_MIN_DROP_BEFORE_CHECK) {
            hasMovedDown = true;
        }

        boolean touching = hasMovedDown && isTouchingAnyToy();
        boolean blocked = hasMovedDown && isBlockedByToy();

        if (touching) {

            // если есть опора — ограничиваем продавливание
            if (blocked) {

                pressDepth += MOVE_SPEED_Y * delta;

                if (pressDepth >= CLAW_MAX_PRESS_DEPTH) {
                    state = State.CLOSE;
                    stateTimer = 0f;
                    triedToCatch = false;
                    return;
                }

            } else {
                // нет опоры → сразу пытаемся схватить
                state = State.CLOSE;
                stateTimer = 0f;
                triedToCatch = false;
                return;
            }

        } else {
            pressDepth = 0f;
        }

        if (y <= DOWN_LIMIT_Y) {
            y = DOWN_LIMIT_Y;
            state = State.CLOSE;
            stateTimer = 0f;
            triedToCatch = false;
        }
    }

    private void updateClose(float delta, List<Toy> toys, List<Toy> trayToys) {
        velocityX = 0f;
        stateTimer += delta;
        float progress = clamp(stateTimer / GameTuning.CLAW_CLOSE_TIME, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_OPEN, FINGER_GAP_CLOSED, progress);

        if (!triedToCatch && stateTimer < 0.08f) {
            triedToCatch = true;

            capturedToy = findTouchingToy(toys);
            if (capturedToy == null) {
                capturedToy = findTouchingToy(trayToys);
            }

            if (capturedToy != null) {
                capturedToy.setCaptured(true);

                // Выравниваем игрушку по центру клешни в момент захвата,
                // чтобы её не "утащило" влево/вправо из-за старой позиции.
                capturedToy.getBody().setTransform(
                    getRealX(),
                    capturedToy.getY(),
                    capturedToy.getBody().getAngle()
                );

                // 🔥 фикс перелёта
                if (Math.abs(swing) < 0.15f) {
                    swingVelocity *= 0.2f;
                }
            }
        }

        if (stateTimer >= GameTuning.CLAW_CLOSE_TIME) {
            state = State.MOVE_UP;
            stateTimer = 0f;
        }
    }

    private void updateMoveUp(float delta) {
        y += MOVE_SPEED_Y * delta;
        velocityX = 0f;

        if (capturedToy != null && !slipCheckedThisCycle && y > GameTuning.SLIP_CHECK_Y) {
            slipCheckedThisCycle = true;

            double slipChance = GameTuning.BASE_SLIP_CHANCE
                + capturedToy.getCatchDifficulty() * GameTuning.SLIP_DIFFICULTY_MULT;

            if (Math.random() < slipChance) {
                Toy toy = capturedToy;
                capturedToy = null;

                toy.releaseFailedGrab((float) (Math.random() * 0.32 - 0.16), -0.08f);
                swingVelocity -= 0.30f;
            }
        }

        if (y >= HOME_Y) {
            y = HOME_Y;
            state = State.MOVE_TO_TRAY;
            swingVelocity += 0.18f;
        }
    }

    private void updateMoveToTray(float delta, List<Toy> trayToys, WinZone winZone) {
        float oldX = getRealX();
        float dx = TRAY_DROP_X - getRealX();

        if (Math.abs(dx) < 0.04f) {
            float oldX2 = x + swingOffsetX;

            x = TRAY_DROP_X;

            float newX2 = x + swingOffsetX;
            velocityX = (newX2 - oldX2) / delta;

            state = State.OPEN;
            stateTimer = 0f;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;

        float newX = x + swingOffsetX;

        float moved = newX - oldX;
        velocityX = moved / delta;

        if (capturedToy != null && !earlyReleaseCheckedThisCycle && x > GameTuning.EARLY_RELEASE_CHECK_X) {
            earlyReleaseCheckedThisCycle = true;

            double earlyReleaseChance = GameTuning.BASE_EARLY_RELEASE_CHANCE
                + capturedToy.getCatchDifficulty() * GameTuning.EARLY_RELEASE_DIFFICULTY_MULT;

            if (Math.random() < earlyReleaseChance) {
                Toy toy = capturedToy;
                capturedToy = null;

                toy.releaseToPhysicalTray(winZone, true, velocityX);

                if (!trayToys.contains(toy)) trayToys.add(toy);

                fingerGap = FINGER_GAP_OPEN;
            }
        }
    }

    private void updateOpen(float delta, List<Toy> trayToys, WinZone winZone) {
        stateTimer += delta;
        float progress = clamp(stateTimer / GameTuning.CLAW_OPEN_TIME, 0f, 1f);
        fingerGap = lerp(FINGER_GAP_CLOSED, FINGER_GAP_OPEN, progress);

        if (capturedToy != null) {
            Toy toy = capturedToy;

            toy.releaseToPhysicalTray(winZone, false, velocityX);

            if (!trayToys.contains(toy)) trayToys.add(toy);

            capturedToy = null;
            swingVelocity -= 0.24f;
        }

        if (stateTimer >= GameTuning.CLAW_OPEN_TIME) {
            state = State.RETURN_HOME;
            stateTimer = 0f;
        }
    }

    private void updateReturnHome(float delta) {
        velocityX = 0f;

        float oldX = x;
        float dx = HOME_X - x;

        if (Math.abs(dx) < 0.05f) {
            x = HOME_X;
            fingerGap = FINGER_GAP_OPEN;

            swing *= 0.10f;
            swingVelocity *= 0.05f;
            state = State.IDLE;
            return;
        }

        x += Math.signum(dx) * MOVE_SPEED_X * delta;
    }

    /**
     * 🔥 ФИЗИКА МАЯТНИКА + ЗАПАЗДЫВАНИЕ ГОЛОВЫ
     */
    private void updateSwing(float delta) {

        float cableLen = Math.max(0.2f, 9f - y);

        // 🔥 физика маятника
        swingVelocity += (-swing * GameTuning.SWING_SPRING) * delta;
        swingVelocity *= GameTuning.SWING_DAMPING;

        swingVelocity = clamp(swingVelocity, -SWING_MAX_VELOCITY, SWING_MAX_VELOCITY);

        swing += swingVelocity * delta;

        // 🔥 Амплитуда раскачки
        float sin = (float) Math.sin(swing);
        float boostedSin = sin * (1f + 2.0f * Math.abs(swing));

        swingOffsetX =
            boostedSin * cableLen * 5f
                + swingVelocity * 0.12f * cableLen;

        // 🔥 голова догоняет
        float diff = swing - headSwing;

        headSwingVelocity += diff * 25f * delta;
        headSwingVelocity *= 0.85f;

        headSwing += headSwingVelocity * delta;

        if (Math.abs(swing) < GameTuning.SWING_STOP_EPS
            && Math.abs(swingVelocity) < GameTuning.SWING_STOP_EPS) {
            swing = 0f;
            swingVelocity = 0f;
        }

        swing = clamp(swing, -GameTuning.SWING_MAX, GameTuning.SWING_MAX);
    }

    private Toy findTouchingToy(List<Toy> source) {
        if (physicsBody == null || world == null) return null;

        Toy best = null;
        float bestScore = Float.MAX_VALUE;

        for (Contact contact : world.getContactList()) {
            if (!contact.isTouching()) continue;

            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();

            Body bodyA = fixtureA.getBody();
            Body bodyB = fixtureB.getBody();

            Body otherBody = null;

            if (bodyA == physicsBody) {
                otherBody = bodyB;
            } else if (bodyB == physicsBody) {
                otherBody = bodyA;
            } else {
                continue;
            }

            for (Toy toy : source) {
                if (toy.isWon() || toy.isCaptured() || toy.isInTray()) continue;
                if (toy.getBody() != otherBody) continue;

                // дополнительно оставляем проверку по X,
                // чтобы не цеплять крайние касания
                if (!isToyCatchableByXOnly(toy)) continue;

                float dx = Math.abs(toy.getX() - getRealX());
                float dy = Math.abs(toy.getY() - (y - 0.9f));

                float score =
                    dx * CLAW_SCORE_WEIGHT_X +
                        dy * CLAW_SCORE_WEIGHT_Y +
                        toy.getCatchDifficulty() * CLAW_SCORE_WEIGHT_DIFFICULTY;

                if (score < bestScore) {
                    bestScore = score;
                    best = toy;
                }
            }
        }

        if (best != null && passesCatchChance(best)) {
            return best;
        }

        return null;
    }

    private boolean isToyCatchableByXOnly(Toy toy) {
        float toyX = toy.getX();
        float realX = getRealX();

        float leftEdge = realX - fingerGap * 0.5f;
        float rightEdge = realX + fingerGap * 0.5f;

        return toyX > leftEdge + CLAW_GRAB_X_MARGIN && toyX < rightEdge - CLAW_GRAB_X_MARGIN;
    }

    private boolean passesCatchChance(Toy toy) {
        float chance = 1f - toy.getCatchDifficulty();
        return Math.random() < chance;
    }

    public void render(SpriteBatch batch) {
        float cableLen = Math.max(0.2f, 9f - y);

        float cableDeg = (float) Math.toDegrees(swing);
        float headDeg = (float) Math.toDegrees(headSwing);

        // 🔥 КЛЮЧ: реальная позиция с учётом раскачки
        float drawX = x + swingOffsetX;

        // 🔥 трос
        batch.draw(
            cableTexture,
            drawX - 0.03f, y,
            0.03f, 0f,
            0.06f, cableLen,
            1f, 1f,
            cableDeg,
            0, 0,
            cableTexture.getWidth(), cableTexture.getHeight(),
            false, false
        );

        // 🔥 голова (с запаздыванием)
        batch.draw(
            headTexture,
            drawX - HEAD_W / 2f, y - HEAD_H / 2f,
            HEAD_W / 2f, HEAD_H / 2f,
            HEAD_W, HEAD_H,
            1f, 1f,
            headDeg,
            0, 0,
            headTexture.getWidth(), headTexture.getHeight(),
            false, false
        );

        float fingerTopY = y - 0.05f;
        float leftX = drawX - fingerGap / 2f - FINGER_W / 2f;
        float rightX = drawX + fingerGap / 2f - FINGER_W / 2f;

        float openAmount = (fingerGap - FINGER_GAP_CLOSED) / (FINGER_GAP_OPEN - FINGER_GAP_CLOSED);

        float targetLeft = -20f + openAmount * 24f + headDeg * 0.20f;
        float targetRight = 20f - openAmount * 24f + headDeg * 0.20f;

        fingerAngleVelLeft += (targetLeft - fingerAngleLeft) * 0.22f;
        fingerAngleVelRight += (targetRight - fingerAngleRight) * 0.22f;

        fingerAngleVelLeft *= 0.78f;
        fingerAngleVelRight *= 0.78f;

        fingerAngleLeft += fingerAngleVelLeft;
        fingerAngleRight += fingerAngleVelRight;

        batch.draw(
            fingerTexture,
            leftX, fingerTopY - FINGER_H,
            FINGER_W / 2f, FINGER_H,
            FINGER_W, FINGER_H,
            1f, 1f,
            fingerAngleLeft,
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
            fingerAngleRight,
            0, 0,
            fingerTexture.getWidth(), fingerTexture.getHeight(),
            false, false
        );
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getHomeY() {
        return HOME_Y;
    }

    public float getDownLimitY() {
        return DOWN_LIMIT_Y;
    }

    public float getFingerGap() {
        return fingerGap;
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

    public float getRealX() {
        return x + swingOffsetX;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    private boolean isBlockedByToy() {

        if (physicsBody == null || world == null) return false;

        for (Contact contact : world.getContactList()) {
            if (!contact.isTouching()) continue;

            Body a = contact.getFixtureA().getBody();
            Body b = contact.getFixtureB().getBody();

            Body other = null;

            if (a == physicsBody) other = b;
            else if (b == physicsBody) other = a;
            else continue;

            if (!(other.getUserData() instanceof Toy toy)) continue;

            if (toy.isCaptured() || toy.isWon() || toy.isInTray()) continue;

            float toyY = toy.getY();

            // 🔥 ИЩЕМ ОПОРУ СНИЗУ
            boolean hasSupport = false;

            Array<Body> bodies = new Array<>();
            world.getBodies(bodies);

            for (Body body : bodies) {

                if (body == other) continue;

                if (!(body.getUserData() instanceof Toy belowToy)) continue;

                if (belowToy.isCaptured() || belowToy.isWon()) continue;

                float dy = toyY - belowToy.getY();
                float dx = Math.abs(toy.getX() - belowToy.getX());

                if (dy > 0 && dy < SUPPORT_CHECK_DY && dx < SUPPORT_CHECK_DX) {
                    hasSupport = true;
                    break;
                }
            }

            // 🔥 ЕСЛИ ЕСТЬ ОПОРА — БЛОК
            if (hasSupport) {
                return true;
            }
        }

        return false;
    }

    private boolean isTouchingAnyToy() {

        if (physicsBody == null || world == null) return false;

        for (Contact contact : world.getContactList()) {
            if (!contact.isTouching()) continue;

            Body a = contact.getFixtureA().getBody();
            Body b = contact.getFixtureB().getBody();

            Body other = null;

            if (a == physicsBody) other = b;
            else if (b == physicsBody) other = a;
            else continue;

            if (!(other.getUserData() instanceof Toy toy)) continue;

            if (toy.isCaptured() || toy.isWon() || toy.isInTray()) continue;

            // 🔥 КЛЮЧ 1: контакт должен быть РЯДОМ (не старый)
            Vector2 posA = physicsBody.getPosition();
            Vector2 posB = other.getPosition();

            if (posA.dst2(posB) > 0.5f) continue;

            // 🔥 КЛЮЧ 2: игрушка должна быть ПОД клешней
            float dy = toy.getY() - (y - 0.9f);

            if (dy > 0.3f) continue; // слишком высоко
            if (dy < -0.2f) continue; // слишком низко

            return true;
        }

        return false;
    }

}
