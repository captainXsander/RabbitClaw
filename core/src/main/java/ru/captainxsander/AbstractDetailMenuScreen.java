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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractDetailMenuScreen extends ScreenAdapter {
    protected static final float UI_WIDTH = 16f;
    protected static final float UI_HEIGHT = 9f;
    private static final String FONT_PATH = "fonts/arial.ttf";

    protected final MainGame game;

    private final OrthographicCamera camera = new OrthographicCamera();
    protected final Viewport viewport = new FitViewport(UI_WIDTH, UI_HEIGHT, camera);
    protected final SpriteBatch batch = new SpriteBatch();

    private final Texture rabbitLeftTexture = new Texture(Gdx.files.internal("toys/default/rabbit_big.png"));
    private final Texture rabbitRightTexture = new Texture(Gdx.files.internal("toys/animals/rabbit_large.png"));
    protected final Texture pixelTexture = createSolidTexture(1, 1, Color.WHITE);
    private final Texture circleTexture = createCircleTexture(192);
    protected final Texture panelTexture = createSolidTexture(1, 1, new Color(1f, 1f, 1f, 0.12f));
    protected final Texture highlightTexture = createSolidTexture(1, 1, new Color(0.98f, 0.84f, 0.25f, 0.26f));

    protected final BitmapFont titleFont;
    protected final BitmapFont bodyFont;
    protected final GlyphLayout glyphLayout = new GlyphLayout();

    protected final List<Rectangle> actionBounds = new ArrayList<>();
    protected int selectedIndex;

    protected AbstractDetailMenuScreen(MainGame game) {
        this.game = game;
        titleFont = createFont(30, new Color(0.98f, 0.92f, 0.82f, 1f));
        bodyFont = createFont(23, new Color(0.96f, 0.92f, 0.84f, 1f));
    }

    @Override
    public void show() {
        viewport.apply(true);
        Gdx.input.setInputProcessor(new DetailInputAdapter());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.06f, 0.13f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawBackground();
        drawContent();
        batch.end();
    }

    protected abstract void drawContent();

    protected abstract int getActionCount();

    protected abstract void onActionTriggered(int actionIndex);

    protected void drawMenuTitle(String title) {
        drawCenteredText(titleFont, title, new Rectangle(1.3f, 7.35f, 13.4f, 0.7f), 0.014f, Align.center);
    }

    protected void drawParagraph(String text, Rectangle bounds) {
        bodyFont.getData().setScale(0.0108f);
        glyphLayout.setText(bodyFont, text, bodyFont.getColor(), bounds.width, Align.center, true);
        bodyFont.draw(batch, glyphLayout, bounds.x, bounds.y + bounds.height);
    }

    protected void drawButton(Rectangle bounds, String label, boolean selected) {
        batch.draw(panelTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        if (selected) {
            batch.draw(highlightTexture, bounds.x + 0.04f, bounds.y + 0.04f, bounds.width - 0.08f, bounds.height - 0.08f);
        }
        drawCenteredText(titleFont, label, bounds, 0.0115f, Align.center);
    }

    protected void drawCenteredText(BitmapFont font, String text, Rectangle bounds, float scale, int align) {
        font.getData().setScale(scale);
        glyphLayout.setText(font, text, font.getColor(), bounds.width, align, true);
        float x = bounds.x;
        float y = bounds.y + (bounds.height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, x, y);
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
        batch.draw(panelTexture, 0.8f, 0.8f, 14.4f, 7.4f);
        batch.draw(highlightTexture, 1.1f, 6.95f, 13.8f, 0.08f);

        batch.setColor(1f, 1f, 1f, 0.20f);
        batch.draw(rabbitLeftTexture, 0.35f, 0.40f, 1.25f, 1.25f);
        batch.draw(rabbitRightTexture, UI_WIDTH - 1.75f, 0.42f, 1.22f, 1.22f);
        batch.setColor(Color.WHITE);
    }

    private void drawStar(float x, float y, float size) {
        batch.setColor(0.99f, 0.91f, 0.68f, 0.92f);
        batch.draw(circleTexture, x, y, size, size);
        batch.setColor(Color.WHITE);
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

    private boolean handleTouch(float screenX, float screenY) {
        Vector2 worldTouch = viewport.unproject(new Vector2(screenX, screenY));
        for (int i = 0; i < actionBounds.size(); i++) {
            if (actionBounds.get(i).contains(worldTouch)) {
                selectedIndex = i;
                onActionTriggered(i);
                return true;
            }
        }
        return false;
    }

    private final class DetailInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                return onBackRequested();
            }

            if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                selectedIndex = (selectedIndex - 1 + getActionCount()) % getActionCount();
                return true;
            }
            if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                selectedIndex = (selectedIndex + 1) % getActionCount();
                return true;
            }
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                onActionTriggered(selectedIndex);
                return true;
            }

            return onExtraKeyDown(keycode);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return handleTouch(screenX, screenY);
        }
    }

    protected boolean onExtraKeyDown(int keycode) {
        return false;
    }

    protected boolean onBackRequested() {
        game.showPreviousMenu();
        return true;
    }
}

