package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DebugOverlay {

    private boolean enabled = true;
    private boolean togglePressedLastFrame = false;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void updateToggle() {
        boolean pressed = Gdx.input.isKeyPressed(Input.Keys.F1);
        if (pressed && !togglePressedLastFrame) {
            enabled = !enabled;
        }
        togglePressedLastFrame = pressed;
    }

    public void render(OrthographicCamera camera, Claw claw, WinZone winZone) {
        if (!enabled) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(
            winZone.getX() - winZone.getWidth() / 2f,
            winZone.getY(),
            winZone.getWidth(),
            winZone.getHeight()
        );

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
            winZone.getInnerLeft(),
            winZone.getInnerBottom(),
            winZone.getInnerRight() - winZone.getInnerLeft(),
            winZone.getInnerTop() - winZone.getInnerBottom()
        );

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(winZone.getDropX(), winZone.getDropY(), 0.08f, 16);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0f, claw.getHomeY(), 16f, claw.getHomeY());

        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.line(0f, claw.getDownLimitY(), 16f, claw.getDownLimitY());

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(claw.getX(), claw.getY(), 0.06f, 12);

        float halfGap = claw.getFingerGap() * 0.5f;
        float top = claw.getY();
        float bottom = claw.getY() - 1.05f;

        shapeRenderer.setColor(Color.PURPLE);
        shapeRenderer.line(claw.getX() - halfGap, top, claw.getX() - halfGap, bottom);
        shapeRenderer.line(claw.getX() + halfGap, top, claw.getX() + halfGap, bottom);
        shapeRenderer.line(claw.getX() - halfGap, bottom, claw.getX() + halfGap, bottom);

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
