package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Экран настроек с безопасным сбросом прогресса через подтверждение.
 */
public class SettingsScreen extends ScreenAdapter {
    private static final float UI_WIDTH = 16f;
    private static final float UI_HEIGHT = 9f;
    private static final String FONT_PATH = "fonts/arial.ttf";

    private final MainGame game;
    private final MenagerieProgress progress = new MenagerieProgress();

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(UI_WIDTH, UI_HEIGHT, camera);
    private final SpriteBatch batch = new SpriteBatch();

    private final Texture titleTexture = new Texture(Gdx.files.internal("menu_settings_title.png"));
    private final Texture rabbitLeftTexture = new Texture(Gdx.files.internal("toys/default/rabbit_big.png"));
    private final Texture rabbitRightTexture = new Texture(Gdx.files.internal("toys/animals/rabbit_large.png"));
    private final Texture pixelTexture = createSolidTexture(1, 1, Color.WHITE);
    private final Texture circleTexture = createCircleTexture(192);
    private final Texture panelTexture = createSolidTexture(1, 1, new Color(1f, 1f, 1f, 0.12f));
    private final Texture highlightTexture = createSolidTexture(1, 1, new Color(0.98f, 0.84f, 0.25f, 0.26f));

    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;

    private final GlyphLayout glyphLayout = new GlyphLayout();

    private final Rectangle resetBounds = new Rectangle(4.3f, 3.9f, 7.4f, 1.0f);
    private final Rectangle backBounds = new Rectangle(4.3f, 2.55f, 7.4f, 1.0f);
    private final Rectangle confirmYesBounds = new Rectangle(4.3f, 2.8f, 3.55f, 0.95f);
    private final Rectangle confirmNoBounds = new Rectangle(8.15f, 2.8f, 3.55f, 0.95f);

    private boolean confirmationVisible;
    private String statusMessage = "";

    public SettingsScreen(MainGame game) {
        this.game = game;
        titleFont = createFont(30, new Color(0.98f, 0.92f, 0.82f, 1f));
        bodyFont = createFont(24, new Color(0.96f, 0.92f, 0.84f, 1f));
    }

    @Override
    public void show() {
        viewport.apply(true);
        Gdx.input.setInputProcessor(new SettingsInputAdapter());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.06f, 0.13f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawBackground();
        drawButtons();
        if (confirmationVisible) {
            drawConfirmationDialog();
        }
        batch.end();
    }

    private void drawBackground() {
        batch.setColor(0.05f, 0.06f, 0.13f, 1f);
        batch.draw(pixelTexture, 0f, 0f, UI_WIDTH, UI_HEIGHT);
        batch.setColor(0.28f, 0.24f, 0.50f, 0.95f);
        batch.draw(pixelTexture, 0.65f, 0.72f, UI_WIDTH - 1.3f, UI_HEIGHT - 1.45f);
        batch.setColor(0.56f, 0.47f, 0.74f, 0.22f);
        batch.draw(pixelTexture, 0.8f, 0.7f, UI_WIDTH - 1.6f, 1.7f);

        batch.setColor(0.99f, 0.93f, 0.72f, 0.90f);
        batch.draw(circleTexture, 1.7f, 6.1f, 0.72f, 0.72f);
        batch.setColor(0.30f, 0.26f, 0.52f, 1f);
        batch.draw(circleTexture, 1.92f, 6.2f, 0.58f, 0.58f);
        drawStar(3.0f, 6.8f, 0.06f);
        drawStar(5.1f, 7.1f, 0.05f);
        drawStar(8.4f, 6.9f, 0.06f);
        drawStar(10.9f, 7.0f, 0.05f);
        drawStar(13.1f, 6.75f, 0.06f);

        batch.setColor(Color.WHITE);
        batch.draw(panelTexture, 0.65f, 0.72f, UI_WIDTH - 1.3f, UI_HEIGHT - 1.45f);
        batch.draw(highlightTexture, 0.8f, 0.7f, UI_WIDTH - 1.6f, 1.7f);
        batch.draw(panelTexture, 0.8f, 0.8f, 14.4f, 7.4f);
        batch.draw(highlightTexture, 1.1f, 6.95f, 13.8f, 0.08f);

        batch.setColor(1f, 1f, 1f, 0.20f);
        batch.draw(rabbitLeftTexture, 0.35f, 0.40f, 1.25f, 1.25f);
        batch.draw(rabbitRightTexture, UI_WIDTH - 1.75f, 0.42f, 1.22f, 1.22f);
        batch.setColor(Color.WHITE);

        float titleWidth = 8.8f;
        float titleHeight = titleWidth * titleTexture.getHeight() / titleTexture.getWidth();
        batch.draw(titleTexture, (UI_WIDTH - titleWidth) * 0.5f, 7f, titleWidth, titleHeight);

        drawCenteredText(bodyFont, "Сброс затрагивает зверинец, уровни и доступные режимы.", new Rectangle(1.3f, 5.6f, 13.4f, 0.8f));

        if (!statusMessage.isEmpty()) {
            drawCenteredText(bodyFont, statusMessage, new Rectangle(1.3f, 1.45f, 13.4f, 0.7f));
        }
    }

    private void drawButtons() {
        drawButton(resetBounds, "Сбросить прогресс");
        drawButton(backBounds, "Назад");
    }

    private void drawConfirmationDialog() {
        Rectangle dialogBounds = new Rectangle(3.6f, 2.25f, 8.8f, 2.9f);
        batch.draw(panelTexture, dialogBounds.x, dialogBounds.y, dialogBounds.width, dialogBounds.height);
        batch.draw(highlightTexture, dialogBounds.x + 0.08f, dialogBounds.y + 0.08f,
            dialogBounds.width - 0.16f, dialogBounds.height - 0.16f);

        drawCenteredText(bodyFont, "Подтвердите сброс прогресса", new Rectangle(4.0f, 4.25f, 8.0f, 0.7f));
        drawCenteredText(bodyFont, "Это действие нельзя отменить", new Rectangle(4.0f, 3.82f, 8.0f, 0.6f));

        drawButton(confirmYesBounds, "Да, сбросить");
        drawButton(confirmNoBounds, "Отмена");
    }

    private void drawButton(Rectangle bounds, String label) {
        batch.draw(panelTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.draw(highlightTexture, bounds.x + 0.05f, bounds.y + 0.05f, bounds.width - 0.1f, bounds.height - 0.1f);
        drawCenteredText(titleFont, label, bounds);
    }

    private void drawCenteredText(BitmapFont font, String text, Rectangle bounds) {
        applyFontScale(font);
        glyphLayout.setText(font, text);
        float x = bounds.x + (bounds.width - glyphLayout.width) * 0.5f;
        float y = bounds.y + (bounds.height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, x, y);
    }

    private void applyFontScale(BitmapFont font) {
        if (font == titleFont) {
            font.getData().setScale(0.013f);
            return;
        }
        font.getData().setScale(0.011f);
    }

    private BitmapFont createFont(int size, Color color) {
        FileHandle fontFile = Gdx.files.internal(FONT_PATH);
        if (!fontFile.exists()) {
            BitmapFont fallback = new BitmapFont();
            fallback.setUseIntegerPositions(false);
            fallback.setColor(color);
            return fallback;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "№«»—…";

        BitmapFont font = generator.generateFont(parameter);
        font.setUseIntegerPositions(false);
        generator.dispose();
        return font;
    }

    private Texture createSolidTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createCircleTexture(int diameter) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void drawStar(float x, float y, float size) {
        batch.setColor(0.99f, 0.91f, 0.68f, 0.92f);
        batch.draw(circleTexture, x, y, size, size);
        batch.setColor(Color.WHITE);
    }

    private boolean handleTouch(float screenX, float screenY) {
        Vector2 worldTouch = viewport.unproject(new Vector2(screenX, screenY));

        if (confirmationVisible) {
            if (confirmYesBounds.contains(worldTouch)) {
                // Сброс выполняется только после явного подтверждения игрока.
                progress.resetAllProgress();
                confirmationVisible = false;
                statusMessage = "Прогресс сброшен.";
                return true;
            }

            if (confirmNoBounds.contains(worldTouch)) {
                confirmationVisible = false;
                statusMessage = "Сброс отменён.";
                return true;
            }

            return true;
        }

        if (resetBounds.contains(worldTouch)) {
            confirmationVisible = true;
            statusMessage = "";
            return true;
        }

        if (backBounds.contains(worldTouch)) {
            game.showPreviousMenu();
            return true;
        }

        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        titleTexture.dispose();
        rabbitLeftTexture.dispose();
        rabbitRightTexture.dispose();
        pixelTexture.dispose();
        circleTexture.dispose();
        panelTexture.dispose();
        highlightTexture.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        batch.dispose();
    }

    private final class SettingsInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                if (confirmationVisible) {
                    confirmationVisible = false;
                    statusMessage = "Сброс отменён.";
                } else {
                    game.showPreviousMenu();
                }
                return true;
            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return handleTouch(screenX, screenY);
        }
    }
}
