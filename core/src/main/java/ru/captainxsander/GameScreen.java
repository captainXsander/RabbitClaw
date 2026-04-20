package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScreen implements Screen {

    // Мир в логических единицах, а не в пикселях.
    public static final float WORLD_WIDTH = GameTuning.WORLD_WIDTH;
    public static final float WORLD_HEIGHT = GameTuning.WORLD_HEIGHT;

    private static final String FONT_PATH = "fonts/arial.ttf";
    private static final float FIND_ANIMAL_RESULT_SHOW_TIME = 2.5f;

    private final MainGame game;
    private final GameMode gameMode;
    private final MenagerieProgress menagerieProgress;

    private World world;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Floor floor;
    private WinZone winZone;
    private Claw claw;
    private DebugOverlay debugOverlay;
    private MachineBounds bounds;

    // Игрушки на полу.
    private final List<Toy> toys = new ArrayList<>();

    // Игрушки, которые уже летали к лотку.
    private final List<Toy> trayToys = new ArrayList<>();

    // Уже обработанные выигрыши текущего запуска.
    private final Set<Toy> reportedWins = new HashSet<>();

    // UI-слой режима FIND_ANIMAL.
    private BitmapFont factFont;
    private BitmapFont statusFont;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Rectangle factBounds = new Rectangle(0.75f, WORLD_HEIGHT - 1.3f, WORLD_WIDTH - 1.5f, 1.05f);
    private final Rectangle resultBounds = new Rectangle(1.4f, WORLD_HEIGHT * 0.52f, WORLD_WIDTH - 2.8f, 1.2f);

    private FindAnimalFacts.FindAnimalTask findAnimalTask;
    private boolean findAnimalRoundResolved;
    private String findAnimalResultText;
    private float findAnimalExitTimer;

    public GameScreen(MainGame game, GameMode gameMode) {
        // Экран знает игру (для возврата в меню) и свой режим.
        this.game = game;
        this.gameMode = gameMode;
        this.menagerieProgress = new MenagerieProgress();
    }

    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, -9.8f), true);

        // Фиксированный viewport для одинаковой камеры на разных разрешениях.
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        batch = new SpriteBatch();

        bounds = new MachineBounds();
        bounds.create(world);

        floor = new Floor();
        floor.create(world);

        winZone = new WinZone();
        winZone.create(world);

        // Клешня получает активный режим, чтобы внутри себя
        // включать/отключать расширенное управление после захвата.
        claw = new Claw(gameMode);
        claw.createPhysics(world);
        claw.setWorld(world);
        debugOverlay = new DebugOverlay();

        if (gameMode == GameMode.FIND_ANIMAL) {
            setupFindAnimalRound();
        }

        createToys();
    }

    private void setupFindAnimalRound() {
        // На каждый запуск режима выбираем новую задачу из JSON.
        FindAnimalFacts facts = new FindAnimalFacts();
        findAnimalTask = facts.createRandomTask(ToyType.FIND_ANIMAL_POOL);
        // Сбрасываем состояние завершения, чтобы раунд начинался "с нуля".
        findAnimalRoundResolved = false;
        findAnimalResultText = null;
        findAnimalExitTimer = 0f;

        // Отдельные шрифты: для текста факта и для статуса победы/поражения.
        factFont = createFont(26, new Color(0.98f, 0.92f, 0.84f, 1f));
        statusFont = createFont(30, new Color(0.98f, 0.84f, 0.25f, 1f));
    }

    private void createToys() {
        // В обычной игре оставляем знакомый набор игрушек,
        // а в режиме спасения используем весь каталог зверинца.
        // В "Поиске Зверей" используем только toys/animals.
        ToyType[] toyPool = getToyPoolForCurrentMode();

        for (int i = 0; i < 45; i++) {

            float x = 3.5f + (float) Math.random() * 6.5f;
            float y = 1.0f + (float) Math.random() * 2.5f;

            ToyType toyType = toyPool[(int) (Math.random() * toyPool.length)];

            float difficulty = 0.2f + (float) Math.random() * 0.5f;
            float restitution = 0.1f + (float) Math.random() * 0.3f;

            toys.add(new Toy(world, x, y, toyType, difficulty, restitution));
        }

        // В режиме "Найти зверей" гарантируем наличие целевой игрушки в куче.
        if (gameMode == GameMode.FIND_ANIMAL && findAnimalTask != null) {
            toys.add(new Toy(world, 6.8f, 2.8f, findAnimalTask.getTargetToyType(), 0.25f, 0.15f));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        claw.update(delta, toys, trayToys, winZone);

        // Фиксированный шаг Box2D.
        world.step(1 / 60f, 6, 2);

        for (Toy toy : toys) {
            toy.update(delta, winZone);
        }
        for (Toy toy : trayToys) {
            toy.update(delta, winZone);
        }

        updateMenagerieUnlocks();
        updateFindAnimalRoundState();
        updateFindAnimalRoundExitTimer(delta);
        debugOverlay.updateToggle();
    }

    private void updateFindAnimalRoundExitTimer(float delta) {
        // После показа результата автоматически возвращаем игрока в предыдущее меню.
        if (!isFindAnimalFinished()) {
            return;
        }

        findAnimalExitTimer -= delta;
        if (findAnimalExitTimer <= 0f) {
            game.showPreviousMenu();
        }
    }

    private void updateFindAnimalRoundState() {
        // Логика результата нужна только до момента, пока раунд не завершён.
        if (gameMode != GameMode.FIND_ANIMAL || findAnimalRoundResolved) {
            return;
        }

        for (Toy toy : toys) {
            resolveFindAnimalResultIfWon(toy);
            if (findAnimalRoundResolved) {
                return;
            }
        }

        for (Toy toy : trayToys) {
            resolveFindAnimalResultIfWon(toy);
            if (findAnimalRoundResolved) {
                return;
            }
        }
    }

    private void resolveFindAnimalResultIfWon(Toy toy) {
        // Берём в расчёт только новые игрушки.
        if (reportedWins.contains(toy)) {
            return;
        }

        // В FIND_ANIMAL считаем завершением:
        // 1) классическую победу (toy.isWon),
        // 2) либо факт попадания в внутреннюю область лотка.
        boolean reachedTray = toy.isWon()
            || toy.isInsideWinZone(winZone)
            || toy.isInsideTrayBounds(winZone);
        if (!reachedTray) {
            return;
        }

        // Первая выигранная игрушка финализирует раунд.
        reportedWins.add(toy);
        findAnimalRoundResolved = true;
        findAnimalExitTimer = FIND_ANIMAL_RESULT_SHOW_TIME;

        if (toy.getToyType() == findAnimalTask.getTargetToyType()) {
            findAnimalResultText = "Молодец, это действительно " + findAnimalTask.getTargetAnimalLabelRu();
        } else {
            findAnimalResultText = "Было близко, но это " + findAnimalTask.getTargetAnimalLabelRu();
        }
    }

    private void updateMenagerieUnlocks() {
        // Карточки открываются только в режиме спасения зверей.
        if (gameMode != GameMode.RESCUE) {
            return;
        }

        for (Toy toy : toys) {
            registerWonToy(toy);
        }
        for (Toy toy : trayToys) {
            registerWonToy(toy);
        }
    }

    private ToyType[] getToyPoolForCurrentMode() {
        // В rescue логика прежняя: доступны все типы из каталога.
        if (gameMode == GameMode.RESCUE) {
            return ToyType.values();
        }

        // В find-animal — только игрушки из assets/toys/animals.
        if (gameMode == GameMode.FIND_ANIMAL) {
            return ToyType.FIND_ANIMAL_POOL;
        }

        return ToyType.NORMAL_POOL;
    }

    private void registerWonToy(Toy toy) {
        // Открываем карточку только один раз на первую победу по игрушке.
        if (!toy.isWon() || reportedWins.contains(toy)) {
            return;
        }

        reportedWins.add(toy);
        menagerieProgress.unlock(toy.getToyType());
    }

    private void draw() {
        ScreenUtils.clear(0.03f, 0.03f, 0.04f, 1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        floor.render(batch);
        winZone.render(batch);
        bounds.render(batch);

        for (Toy toy : toys) {
            toy.render(batch);
        }
        for (Toy toy : trayToys) {
            toy.render(batch);
        }

        claw.render(batch);

        if (gameMode == GameMode.FIND_ANIMAL) {
            drawFindAnimalUi();
        }

        batch.end();

        debugOverlay.render(camera, claw, winZone);
    }

    private void drawFindAnimalUi() {
        if (factFont == null || findAnimalTask == null) {
            return;
        }

        // Факт всегда показывается вверху экрана с переносом строк.
        factFont.getData().setScale(0.011f);
        glyphLayout.setText(
            factFont,
            "Факт: " + findAnimalTask.getFact(),
            factFont.getColor(),
            factBounds.width,
            Align.center,
            true
        );
        factFont.draw(batch, glyphLayout, factBounds.x, factBounds.y + factBounds.height);

        if (!isFindAnimalFinished()) {
            return;
        }

        // Сообщение о результате рисуем по центру экрана, чтобы не перекрывать факт.
        statusFont.getData().setScale(0.015f);
        glyphLayout.setText(
            statusFont,
            findAnimalResultText,
            statusFont.getColor(),
            resultBounds.width,
            Align.center,
            true
        );
        statusFont.draw(batch, glyphLayout, resultBounds.x, resultBounds.y + resultBounds.height);

        // Показываем обратный таймер возврата в предыдущее меню.
        factFont.getData().setScale(0.012f);
        int secondsLeft = Math.max(1, (int) Math.ceil(findAnimalExitTimer));
        String hint = "Возврат в меню через " + secondsLeft + " сек.";
        glyphLayout.setText(factFont, hint);
        factFont.draw(batch, glyphLayout, (WORLD_WIDTH - glyphLayout.width) * 0.5f, resultBounds.y - 0.2f);
    }

    private boolean isFindAnimalFinished() {
        return gameMode == GameMode.FIND_ANIMAL && findAnimalRoundResolved;
    }

    private BitmapFont createFont(int size, Color color) {
        // Поддерживаем fallback на bitmap-font, если TTF внезапно недоступен.
        FileHandle internalFont = Gdx.files.internal(FONT_PATH);
        if (!internalFont.exists()) {
            BitmapFont fallback = new BitmapFont();
            fallback.setUseIntegerPositions(false);
            fallback.setColor(color);
            return fallback;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(internalFont);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        // Добавляем кириллицу и спецсимволы, чтобы факты на русском рендерились корректно.
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "№«»—…";

        BitmapFont font = generator.generateFont(parameter);
        font.setUseIntegerPositions(false);
        generator.dispose();
        return font;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        for (Toy toy : toys) {
            toy.dispose();
        }
        for (Toy toy : trayToys) {
            toy.dispose();
        }

        claw.dispose();
        floor.dispose();
        winZone.dispose();
        debugOverlay.dispose();
        batch.dispose();
        bounds.dispose();
        world.dispose();

        if (factFont != null) {
            factFont.dispose();
        }
        if (statusFont != null) {
            statusFont.dispose();
        }
    }
}
