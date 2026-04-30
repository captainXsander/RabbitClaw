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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static ru.captainxsander.GameScreen.FONT_PATH;

abstract class AbstractMenuScreen extends ScreenAdapter {
    // Логические размеры UI, чтобы меню выглядело одинаково на разных экранах.
    protected static final float UI_WIDTH = 16f;
    protected static final float UI_HEIGHT = 9f;

    // Ссылка на главный объект игры нужна для переключения экранов.
    protected final MainGame game;
    // Путь к текстуре заголовка текущего меню.
    private final String titleTexturePath;
    // Список пунктов меню с текстурой, действием и зоной клика.
    private final Array<MenuOption> options = new Array<>();

    // Камера и viewport управляют отображением меню на любом разрешении.
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(UI_WIDTH, UI_HEIGHT, camera);
    // Один batch используется для рисования всего интерфейса.
    private final SpriteBatch batch = new SpriteBatch();
    // Полупрозрачная подложка под панель и кнопки.
    private final Texture panelTexture = createSolidTexture(1, 1, new Color(1f, 1f, 1f, 0.12f));
    // Подсветка выбранного пункта меню.
    private final Texture highlightTexture = createSolidTexture(1, 1, new Color(0.98f, 0.84f, 0.25f, 0.3f));
    // Однопиксельная текстура для фоновых декоративных слоёв.
    private final Texture pixelTexture = createSolidTexture(1, 1, Color.WHITE);
    private final Texture circleTexture = createCircleTexture(192);
    private final Texture rabbitLeftTexture = new Texture(Gdx.files.internal("toys/default/rabbit_big.png"));
    private final Texture rabbitRightTexture = new Texture(Gdx.files.internal("toys/animals/rabbit_large.png"));
    private final BitmapFont brandFont = createFont(34, new Color(0.98f, 0.92f, 0.80f, 1f));
    private final BitmapFont buttonFont = createFont(30, new Color(0.96f, 0.92f, 0.84f, 1f));
    private final GlyphLayout brandLayout = new GlyphLayout();
    private final GlyphLayout buttonLayout = new GlyphLayout();
    // Заголовок меню хранится отдельной текстурой.
    private final Texture titleTexture;

    // Индекс текущего выбранного пункта.
    private int selectedIndex;

    protected AbstractMenuScreen(MainGame game, String titleTexturePath) {
        this.game = game;
        this.titleTexturePath = titleTexturePath;
        // Загружаем картинку заголовка из assets.
        this.titleTexture = new Texture(Gdx.files.internal(titleTexturePath));
    }

    protected void addOption(String label, Runnable action) {
        // Каждый пункт состоит из подписи-кнопки и действия при выборе.
        options.add(new MenuOption(label, action));
    }

    @Override
    public void show() {
        // Сразу применяем viewport и начинаем слушать клавиатуру/тач.
        viewport.apply(true);
        Gdx.input.setInputProcessor(new MenuInputAdapter());
    }

    @Override
    public void render(float delta) {
        // Очищаем экран перед отрисовкой меню.
        ScreenUtils.clear(0.07f, 0.06f, 0.09f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // Общая раскладка экрана: заголовок сверху, кнопки ниже по центру.
        float centerX = UI_WIDTH * 0.5f;
        float titleWidth = 8.8f;
        float titleHeight = titleWidth * titleTexture.getHeight() / titleTexture.getWidth();
        float titleY = 7f;
        float buttonWidth = 6.6f;
        float buttonHeight = 0.92f;
        float buttonGap = 0.38f;
        float buttonY = 5.1f;
        float maxButtonY = 5.9f;
        float minButtonY = 1.0f;

        if (options.size > 1) {
            float defaultStep = buttonHeight + buttonGap;
            float lowestButtonY = buttonY - (options.size - 1f) * defaultStep;

            if (lowestButtonY < minButtonY) {
                float requiredLift = minButtonY - lowestButtonY;
                float maxLift = maxButtonY - buttonY;
                float appliedLift = Math.min(requiredLift, maxLift);
                buttonY += appliedLift;
                lowestButtonY += appliedLift;
            }

            if (lowestButtonY < minButtonY) {
                float maxStep = (buttonY - minButtonY) / (options.size - 1f);
                float scale = Math.max(0.78f, maxStep / defaultStep);
                buttonHeight *= scale;
                buttonGap *= scale;
            }
        }

        batch.begin();
        // Фон и декоративные элементы.
        drawBackground();
        // Рисуем заголовок меню.
        batch.draw(titleTexture, centerX - titleWidth / 2f, titleY, titleWidth, titleHeight);
        drawBrandTitleIfNeeded(centerX);

        for (int i = 0; i < options.size; i++) {
            MenuOption option = options.get(i);
            float x = centerX - buttonWidth / 2f;
            float y = buttonY - i * (buttonHeight + buttonGap);

            // Запоминаем область клика немного больше самой текстуры для удобства.
            float boundsPadX = 0.25f;
            float boundsPadY = Math.max(0.08f, buttonHeight * 0.13f);
            option.bounds.set(x - boundsPadX, y - boundsPadY, buttonWidth + boundsPadX * 2f, buttonHeight + boundsPadY * 2f);
            // Рисуем подложку для каждой кнопки.
            batch.draw(panelTexture, option.bounds.x, option.bounds.y, option.bounds.width, option.bounds.height);
            if (selectedIndex == i) {
                // Выбранный пункт подсвечивается поверх подложки.
                batch.draw(highlightTexture, option.bounds.x, option.bounds.y, option.bounds.width, option.bounds.height);
            }
            // Рисуем подпись кнопки.
            drawButtonLabel(option.label, x, y, buttonWidth, buttonHeight);
        }
        batch.end();
    }

    private void drawButtonLabel(String label, float x, float y, float width, float height) {
        buttonFont.getData().setScale(0.0155f);
        buttonLayout.setText(buttonFont, label);
        float textX = x + (width - buttonLayout.width) * 0.5f;
        float textY = y + (height + buttonLayout.height) * 0.5f;

        buttonFont.setColor(0.08f, 0.06f, 0.16f, 0.74f);
        buttonFont.draw(batch, buttonLayout, textX + 0.02f, textY - 0.02f);
        buttonFont.setColor(0.96f, 0.92f, 0.84f, 1f);
        buttonFont.draw(batch, buttonLayout, textX, textY);
    }

    protected void drawBackground() {
        batch.setColor(0.05f, 0.06f, 0.13f, 1f);
        batch.draw(pixelTexture, 0f, 0f, UI_WIDTH, UI_HEIGHT);

        batch.setColor(0.28f, 0.24f, 0.50f, 0.95f);
        batch.draw(pixelTexture, 0.65f, 0.72f, UI_WIDTH - 1.3f, UI_HEIGHT - 1.45f);
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
        // Тонкая светлая линия под шапкой для визуального разделения.
        batch.draw(highlightTexture, 1.1f, 6.95f, 13.8f, 0.08f);

        // Декоративные зайчики по краям.
        batch.setColor(1f, 1f, 1f, 0.30f);
        batch.draw(rabbitLeftTexture, 0.68f, 0.74f, 1.25f, 1.25f);
        batch.draw(rabbitRightTexture, UI_WIDTH - 1.90f, 0.74f, 1.22f, 1.22f);
        batch.setColor(Color.WHITE);
    }

    protected String getBrandTitleText() {
        return null;
    }

    protected float getBrandTitleY() {
        return 8.12f;
    }

    protected float getBrandTitleScale() {
        return 0.018f;
    }

    private void drawBrandTitleIfNeeded(float centerX) {
        String brandTitle = getBrandTitleText();
        if (brandTitle == null || brandTitle.isEmpty()) {
            return;
        }
        brandFont.getData().setScale(getBrandTitleScale());
        brandLayout.setText(brandFont, brandTitle);
        float x = centerX - brandLayout.width * 0.5f;
        float y = getBrandTitleY();
        brandFont.setColor(0.08f, 0.06f, 0.16f, 0.74f);
        brandFont.draw(batch, brandLayout, x + 0.02f, y - 0.02f);
        brandFont.setColor(1f, 0.95f, 0.82f, 1f);
        brandFont.draw(batch, brandLayout, x, y);
    }

    private void drawStar(float x, float y, float size) {
        batch.setColor(0.99f, 0.91f, 0.68f, 0.92f);
        batch.draw(circleTexture, x, y, size, size);
    }

    @Override
    public void resize(int width, int height) {
        // При изменении окна пересчитываем viewport с центрированием камеры.
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        // Когда экран скрывается, снимаем обработчик ввода.
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        // Освобождаем все ресурсы, созданные этим экраном.
        titleTexture.dispose();
        panelTexture.dispose();
        highlightTexture.dispose();
        pixelTexture.dispose();
        circleTexture.dispose();
        rabbitLeftTexture.dispose();
        rabbitRightTexture.dispose();
        brandFont.dispose();
        buttonFont.dispose();
        batch.dispose();
    }

    protected void moveSelection(int direction) {
        // Если кнопок нет, двигаться некуда.
        if (options.size == 0) {
            return;
        }
        // Ходим по списку циклически.
        selectedIndex = (selectedIndex + direction + options.size) % options.size;
    }

    protected void activateSelected() {
        // Нечего активировать, если список пуст.
        if (options.size == 0) {
            return;
        }
        // Запускаем действие выбранного пункта.
        options.get(selectedIndex).action.run();
    }

    protected boolean handleTouch(float screenX, float screenY) {
        // Без пунктов меню обработка клика не имеет смысла.
        if (options.size == 0) {
            return false;
        }
        // Переводим координаты экрана в координаты нашего viewport.
        float worldX = viewport.unproject(new com.badlogic.gdx.math.Vector2(screenX, screenY)).x;
        float worldY = viewport.unproject(new com.badlogic.gdx.math.Vector2(screenX, screenY)).y;

        for (int i = 0; i < options.size; i++) {
            if (options.get(i).bounds.contains(worldX, worldY)) {
                // Клик по кнопке делает её выбранной и сразу активирует.
                selectedIndex = i;
                activateSelected();
                return true;
            }
        }
        return false;
    }

    protected boolean onKeyDown(int keycode) {
        // Стрелки и WASD двигают выбор по меню.
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            moveSelection(-1);
            return true;
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            moveSelection(1);
            return true;
        }
        // Enter и Space подтверждают выбор.
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
            activateSelected();
            return true;
        }
        return false;
    }

    protected boolean onBackRequested() {
        // По умолчанию экран сам не знает, что делать по Back/Escape.
        return false;
    }

    private Texture createSolidTexture(int width, int height, Color color) {
        // Маленькая одноцветная текстура растягивается как фон и подсветка.
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

    private BitmapFont createFont(int size, Color color) {
        FileHandle fontFile = Gdx.files.internal(FONT_PATH);
        if (!fontFile.exists()) {
            BitmapFont fallback = new BitmapFont();
            fallback.setColor(color);
            fallback.setUseIntegerPositions(false);
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

    private final class MenuInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            // Escape/Back обрабатываются отдельно как кнопка возврата.
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                return onBackRequested();
            }
            return onKeyDown(keycode);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // На тач-экране и мыши выбираем пункт нажатием.
            return handleTouch(screenX, screenY);
        }
    }

    private static final class MenuOption {
        // Текст на кнопке.
        private final String label;
        // Что должно произойти при выборе.
        private final Runnable action;
        // Область, в которую можно нажать.
        private final Rectangle bounds = new Rectangle();

        private MenuOption(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }
}
