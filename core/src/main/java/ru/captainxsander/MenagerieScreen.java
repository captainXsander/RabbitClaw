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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Экран зверинца с сеткой карточек игрушек.
 * Закрытые карточки показывают только силуэт-заглушку,
 * а открытые можно нажать и посмотреть обратную сторону с описанием.
 */
public class MenagerieScreen extends ScreenAdapter {
    // Логические размеры UI, чтобы экран был одинаковым на разных разрешениях.
    private static final float UI_WIDTH = 16f;
    private static final float UI_HEIGHT = 9f;

    // На одной странице помещается 4 столбца и 3 строки карточек.
    private static final int CARDS_PER_PAGE = 12;

    // Путь к TTF-шрифту, который поддерживает кириллицу.
    private static final String FONT_PATH = "fonts/arial.ttf";

    // Масштаб шрифтов относительно логических координат viewport.
    private static final float TITLE_FONT_SCALE = 0.018f;
    private static final float BODY_FONT_SCALE = 0.013f;
    private static final float HINT_FONT_SCALE = 0.010f;

    // Главный объект игры нужен для возврата в меню.
    private final MainGame game;

    // Хранилище прогресса определяет, какие карточки уже открыты.
    private final MenagerieProgress progress;

    // Камера и viewport управляют логическими координатами UI.
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(UI_WIDTH, UI_HEIGHT, camera);

    // Один batch используется для всей отрисовки зверинца.
    private final SpriteBatch batch = new SpriteBatch();

    // Отдельные шрифты нужны для заголовков, текста карточки и мелких подсказок.
    private final BitmapFont cardTitleFont;
    private final BitmapFont cardBodyFont;
    private final BitmapFont hintFont;

    // Layout переиспользуется, чтобы не создавать лишние объекты каждый кадр.
    private final GlyphLayout glyphLayout = new GlyphLayout();

    // Готовые текстуры интерфейса.
    private final Texture titleTexture = new Texture(Gdx.files.internal("menu_menagerie_title.png"));
    private final Texture backTexture = new Texture(Gdx.files.internal("menu_back.png"));
    private final Texture pixelTexture = createSolidTexture(1, 1, Color.WHITE);
    private final Texture circleTexture = createCircleTexture(192);

    // Полупрозрачные подложки и подсветки для UI.
    private final Texture panelTexture = createSolidTexture(1, 1, new Color(1f, 1f, 1f, 0.12f));
    private final Texture highlightTexture = createSolidTexture(1, 1, new Color(0.98f, 0.84f, 0.25f, 0.22f));
    private final Texture hiddenCardTexture = createSolidTexture(1, 1, new Color(0.18f, 0.18f, 0.24f, 1f));
    private final Texture hiddenInnerTexture = createSolidTexture(1, 1, new Color(0.28f, 0.28f, 0.36f, 1f));
    private final Texture darkOverlayTexture = createSolidTexture(1, 1, new Color(0f, 0f, 0f, 0.55f));

    // Зоны клика для навигации по экрану.
    private final Rectangle backBounds = new Rectangle(0.85f, 0.82f, 2.9f, 0.86f);
    private final Rectangle prevPageBounds = new Rectangle(11.0f, 0.82f, 1.2f, 0.75f);
    private final Rectangle nextPageBounds = new Rectangle(12.5f, 0.82f, 1.2f, 0.75f);

    // Визуальные карточки для всех игрушек из каталога.
    private final Array<CardView> cards = new Array<>();

    // Текущая страница сетки карточек.
    private int currentPage;

    // Если не null, поверх сетки открыта обратная сторона этой карточки.
    private ToyType openedCard;

    public MenagerieScreen(MainGame game) {
        // Сохраняем ссылки на игру и прогресс зверинца.
        this.game = game;
        this.progress = new MenagerieProgress();

        // Создаём кириллические TTF-шрифты для всех текстовых элементов экрана.
        cardTitleFont = createFont(32, new Color(0.98f, 0.92f, 0.82f, 1f));
        cardBodyFont = createFont(24, new Color(0.96f, 0.92f, 0.84f, 1f));
        hintFont = createFont(20, new Color(0.90f, 0.86f, 0.78f, 1f));

        // В зверинце показываем только зверей из режима спасения.
        for (ToyType toyType : ToyType.ANIMAL_POOL) {
            cards.add(new CardView(toyType));
        }
    }

    @Override
    public void show() {
        // Подключаем ввод для кликов по карточкам и кнопкам.
        viewport.apply(true);
        Gdx.input.setInputProcessor(new MenagerieInputAdapter());
    }

    @Override
    public void render(float delta) {
        // Каждый кадр очищаем экран и рисуем текущую страницу зверинца.
        ScreenUtils.clear(0.05f, 0.06f, 0.13f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // Позиции карточек зависят от активной страницы, поэтому обновляем их каждый кадр.
        updateCardBounds();

        batch.begin();
        drawBackground();
        drawHeader();
        drawCards();
        drawFooter();
        if (openedCard != null) {
            drawOpenedCard(openedCard);
        }
        batch.end();
    }

    private void drawBackground() {
        batch.setColor(0.05f, 0.06f, 0.13f, 1f);
        batch.draw(pixelTexture, 0f, 0f, UI_WIDTH, UI_HEIGHT);
        batch.setColor(0.28f, 0.24f, 0.50f, 0.95f);
        batch.draw(pixelTexture, 0.65f, 0.72f, UI_WIDTH - 1.3f, UI_HEIGHT - 1.45f);
        batch.setColor(Color.WHITE);

        // Узкая светлая линия отделяет шапку экрана от сетки карточек.
        drawStar(3.0f, 6.8f, 0.06f);
        drawStar(5.1f, 7.1f, 0.05f);
        drawStar(8.4f, 6.9f, 0.06f);
        drawStar(10.9f, 7.0f, 0.05f);
        drawStar(13.1f, 6.75f, 0.06f);

        batch.setColor(Color.WHITE);

        // Узкая светлая линия отделяет шапку экрана от сетки карточек.
        batch.draw(highlightTexture, 0.8f, 6.95f, 14.4f, 0.08f);

    }

    private void drawStar(float x, float y, float size) {
        batch.setColor(0.99f, 0.91f, 0.68f, 0.92f);
        batch.draw(circleTexture, x, y, size, size);
        batch.setColor(Color.WHITE);
    }

    private void drawHeader() {
        // Рисуем текстурный заголовок зверинца.
        float titleWidth = 6.8f;
        float titleHeight = titleWidth * titleTexture.getHeight() / titleTexture.getWidth();
        batch.draw(titleTexture, 4.6f, 7.15f, titleWidth, titleHeight);

        // Рисуем кнопку возврата в главное меню.
        batch.draw(panelTexture, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        batch.draw(backTexture, backBounds.x + 0.14f, backBounds.y + 0.18f,
            backBounds.width - 0.28f, backBounds.height - 0.36f);
    }

    private void drawCards() {
        // Отображаем только карточки текущей страницы.
        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(cards.size, start + CARDS_PER_PAGE);

        for (int i = start; i < end; i++) {
            drawCardFront(cards.get(i));
        }
    }

    private void drawCardFront(CardView card) {
        // Внешняя рамка карточки одинакова и для открытого, и для закрытого состояния.
        batch.draw(panelTexture, card.bounds.x, card.bounds.y, card.bounds.width, card.bounds.height);

        if (!progress.isUnlocked(card.toyType)) {
            // Закрытая карточка показывает только затемнённую заглушку.
            batch.draw(hiddenCardTexture, card.bounds.x + 0.06f, card.bounds.y + 0.06f,
                card.bounds.width - 0.12f, card.bounds.height - 0.12f);
            batch.draw(hiddenInnerTexture, card.bounds.x + 0.22f, card.bounds.y + 0.22f,
                card.bounds.width - 0.44f, card.bounds.height - 0.44f);
            drawCenteredText(cardTitleFont, "?", card.bounds);
            return;
        }

        // Открытая карточка получает мягкую подсветку.
        batch.draw(highlightTexture, card.bounds.x + 0.04f, card.bounds.y + 0.04f,
            card.bounds.width - 0.08f, card.bounds.height - 0.08f);

        // Изображение игрушки вписываем в карточку с сохранением пропорций.
        float maxImageWidth = card.bounds.width - 0.35f;
        float maxImageHeight = card.bounds.height - 0.35f;
        float imageScale = Math.min(
            maxImageWidth / card.texture.getWidth(),
            maxImageHeight / card.texture.getHeight()
        );

        float drawWidth = card.texture.getWidth() * imageScale;
        float drawHeight = card.texture.getHeight() * imageScale;
        float drawX = card.bounds.x + (card.bounds.width - drawWidth) * 0.5f;
        float drawY = card.bounds.y + (card.bounds.height - drawHeight) * 0.5f;

        batch.draw(card.texture, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawFooter() {
        // Количество страниц считается по размеру каталога игрушек.
        int pageCount = getPageCount();

        float hintLeft = backBounds.x + backBounds.width + 0.2f;
        float hintRight = pageInfoBounds().x - 0.2f;
        if (currentPage > 0) {
            hintRight = Math.min(hintRight, prevPageBounds.x - 0.2f);
        }
        if (currentPage < pageCount - 1) {
            hintRight = Math.min(hintRight, nextPageBounds.x - 0.2f);
        }
        Rectangle menagerieHintBounds = new Rectangle(hintLeft, 0.76f, hintRight - hintLeft, 0.8f);
        drawWrappedText(
            hintFont,
            "Зверинец — это коллекция карточек, которые открываются в режиме \"Спасти Зверей\": "
                + "выигрывайте уникальные игрушки зверей на каждом новом уровне, чтобы пополнять этот раздел. " +
                "В каждой карточке есть интересный или забавный факт о животном",
            menagerieHintBounds
        );

        // Кнопка предыдущей страницы.
        if (currentPage > 0) {
            batch.draw(panelTexture, prevPageBounds.x, prevPageBounds.y, prevPageBounds.width, prevPageBounds.height);
            drawCenteredText(cardTitleFont, "<", prevPageBounds);
        }

        // Кнопка следующей страницы.
        if (currentPage < pageCount - 1) {
            batch.draw(panelTexture, nextPageBounds.x, nextPageBounds.y, nextPageBounds.width, nextPageBounds.height);
            drawCenteredText(cardTitleFont, ">", nextPageBounds);
        }

        // Индикатор вида "1/3" показывает текущую страницу каталога.
        Rectangle pageInfoBounds = pageInfoBounds();
        drawCenteredText(hintFont, (currentPage + 1) + "/" + pageCount, pageInfoBounds);
    }


    private Rectangle pageInfoBounds() {
        return new Rectangle(13.85f, 0.82f, 1.1f, 0.75f);
    }
    private void drawOpenedCard(ToyType toyType) {
        // Поверх сетки выводим оверлей с обратной стороной выбранной карточки.
        Rectangle overlayBounds = new Rectangle(3.1f, 1.1f, 9.8f, 6.2f);
        batch.draw(darkOverlayTexture, 0f, 0f, UI_WIDTH, UI_HEIGHT);
        batch.draw(panelTexture, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height);
        batch.draw(highlightTexture, overlayBounds.x + 0.08f, overlayBounds.y + 0.08f,
            overlayBounds.width - 0.16f, overlayBounds.height - 0.16f);

        // Наверху показываем имя игрушки.
        Rectangle titleBounds = new Rectangle(overlayBounds.x + 0.4f, overlayBounds.y + 5.12f, 9.0f, 0.65f);
        drawCenteredText(cardTitleFont, toyType.getTitle(), titleBounds);

        // Слева остаётся небольшое превью самой игрушки.
        Texture cardTexture = getCardTexture(toyType);
        float previewSize = 1.8f;
        batch.draw(cardTexture, overlayBounds.x + 0.55f, overlayBounds.y + 3.0f, previewSize, previewSize);

        // Справа и ниже рисуем многострочное описание карточки.
        Rectangle textBounds = new Rectangle(overlayBounds.x + 2.7f, overlayBounds.y + 1.15f, 6.2f, 3.75f);
        drawWrappedText(cardBodyFont, toyType.getCardText(), textBounds);

        // Внизу оставляем подсказку, как закрыть обратную сторону карточки.
        Rectangle closeBounds = new Rectangle(overlayBounds.x + 2.8f, overlayBounds.y + 0.35f, 4.0f, 0.55f);
        drawCenteredText(hintFont, "Нажмите, чтобы закрыть", closeBounds);
    }

    private void updateCardBounds() {
        // Карточки раскладываются сеткой 4x3 на текущей странице.
        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(cards.size, start + CARDS_PER_PAGE);

        float startX = 1.1f;
        float startY = 5.35f;
        float cardWidth = 3.2f;
        float cardHeight = 1.45f;
        float gapX = 0.35f;
        float gapY = 0.28f;

        for (int i = start; i < end; i++) {
            int localIndex = i - start;
            int column = localIndex % 4;
            int row = localIndex / 4;
            float x = startX + column * (cardWidth + gapX);
            float y = startY - row * (cardHeight + gapY);
            cards.get(i).bounds.set(x, y, cardWidth, cardHeight);
        }
    }

    private int getPageCount() {
        // Даже если карточек мало, всегда показываем хотя бы одну страницу.
        return Math.max(1, (int) Math.ceil(cards.size / (float) CARDS_PER_PAGE));
    }

    private BitmapFont createFont(int size, Color color) {
        // Генерируем шрифт из TTF при создании экрана, чтобы не хранить bitmap-font ассеты вручную.
        FileHandle fontFile = resolveFontFile();
        if (fontFile == null) {
            BitmapFont fallbackFont = new BitmapFont();
            fallbackFont.setUseIntegerPositions(false);
            fallbackFont.setColor(color);
            return fallbackFont;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "№«»—…";

        BitmapFont font = generator.generateFont(parameter);
        // BitmapFont из TTF создаётся в пикселях, поэтому уменьшаем его до масштаба игрового viewport.
        font.setUseIntegerPositions(false);
        generator.dispose();
        return font;
    }

    private FileHandle resolveFontFile() {
        FileHandle internalFont = Gdx.files.internal(FONT_PATH);
        if (internalFont.exists()) {
            return internalFont;
        }

        FileHandle windowsFont = Gdx.files.absolute("C:/Windows/Fonts/arial.ttf");
        if (windowsFont.exists()) {
            return windowsFont;
        }

        return null;
    }

    private void drawCenteredText(BitmapFont font, String text, Rectangle bounds) {
        // Универсальный метод для центрирования текста внутри любой прямоугольной области.
        applyFontScale(font);
        glyphLayout.setText(font, text);
        float x = bounds.x + (bounds.width - glyphLayout.width) * 0.5f;
        float y = bounds.y + (bounds.height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, x, y);
    }

    private void drawWrappedText(BitmapFont font, String text, Rectangle bounds) {
        // Текст описания переносится по ширине карточки.
        applyFontScale(font);
        glyphLayout.setText(font, text, font.getColor(), bounds.width, com.badlogic.gdx.utils.Align.left, true);
        float x = bounds.x;
        float y = bounds.y + bounds.height;
        font.draw(batch, glyphLayout, x, y);
    }

    private void applyFontScale(BitmapFont font) {
        // Для каждого типа текста используем свой масштаб в логических координатах экрана.
        if (font == cardTitleFont) {
            font.getData().setScale(TITLE_FONT_SCALE);
            return;
        }
        if (font == cardBodyFont) {
            font.getData().setScale(BODY_FONT_SCALE);
            return;
        }
        font.getData().setScale(HINT_FONT_SCALE);
    }

    private Texture getCardTexture(ToyType toyType) {
        // Находим текстуру карточки по типу игрушки.
        for (CardView card : cards) {
            if (card.toyType == toyType) {
                return card.texture;
            }
        }
        throw new IllegalStateException("Card texture not found for " + toyType);
    }

    @Override
    public void resize(int width, int height) {
        // При изменении размера окна пересчитываем viewport.
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
        // Освобождаем текстуры и все сгенерированные шрифты.
        titleTexture.dispose();
        backTexture.dispose();
        pixelTexture.dispose();
        circleTexture.dispose();
        panelTexture.dispose();
        highlightTexture.dispose();
        hiddenCardTexture.dispose();
        hiddenInnerTexture.dispose();
        darkOverlayTexture.dispose();
        cardTitleFont.dispose();
        cardBodyFont.dispose();
        hintFont.dispose();
        batch.dispose();

        // У каждой карточки своя текстура превью.
        for (CardView card : cards) {
            card.texture.dispose();
        }
    }

    private Texture createSolidTexture(int width, int height, Color color) {
        // Одноцветная текстура растягивается как фон, подсветка и затемнение.
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
        // Переводим экранные координаты в координаты нашего viewport.
        Vector2 worldTouch = viewport.unproject(new Vector2(screenX, screenY));

        if (openedCard != null) {
            // Если оверлей открыт, любое нажатие закрывает его.
            openedCard = null;
            return true;
        }

        if (backBounds.contains(worldTouch)) {
            // Кнопка назад возвращает в предыдущее меню.
            game.showPreviousMenu();
            return true;
        }

        if (currentPage > 0 && prevPageBounds.contains(worldTouch)) {
            // Листаем каталог на предыдущую страницу.
            currentPage--;
            return true;
        }

        if (currentPage < getPageCount() - 1 && nextPageBounds.contains(worldTouch)) {
            // Листаем каталог на следующую страницу.
            currentPage++;
            return true;
        }

        // Проверяем нажатие по карточкам текущей страницы.
        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(cards.size, start + CARDS_PER_PAGE);
        for (int i = start; i < end; i++) {
            CardView card = cards.get(i);
            if (card.bounds.contains(worldTouch) && progress.isUnlocked(card.toyType)) {
                openedCard = card.toyType;
                return true;
            }
        }

        return false;
    }

    private final class MenagerieInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            // Escape и Back закрывают открытую карточку либо возвращают в меню.
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                if (openedCard != null) {
                    openedCard = null;
                } else {
                    game.showPreviousMenu();
                }
                return true;
            }

            // Клавиши влево и вправо перелистывают страницы зверинца.
            if (keycode == Input.Keys.LEFT && currentPage > 0) {
                currentPage--;
                return true;
            }

            if (keycode == Input.Keys.RIGHT && currentPage < getPageCount() - 1) {
                currentPage++;
                return true;
            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Тач и мышь используют одну и ту же логику обработки нажатий.
            return handleTouch(screenX, screenY);
        }
    }

    private static final class CardView {
        // Тип игрушки, которой соответствует эта карточка.
        private final ToyType toyType;

        // Текстура лицевой стороны карточки с картинкой игрушки.
        private final Texture texture;

        // Область клика карточки в сетке.
        private final Rectangle bounds = new Rectangle();

        private CardView(ToyType toyType) {
            // Сохраняем тип для доступа к прогрессу и тексту карточки.
            this.toyType = toyType;

            // Загружаем превью игрушки для лицевой стороны карточки.
            this.texture = new Texture(Gdx.files.internal(toyType.getTexturePath()));
        }
    }
}
