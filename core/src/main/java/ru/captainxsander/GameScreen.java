package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application;
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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameScreen implements Screen {

    // Мир в логических единицах, а не в пикселях.
    public static final float WORLD_WIDTH = GameTuning.WORLD_WIDTH;
    public static final float WORLD_HEIGHT = GameTuning.WORLD_HEIGHT;

    public static final String FONT_PATH = "fonts/arial.ttf";
    private static final float FIND_ANIMAL_RESULT_SHOW_TIME = 2.5f;
    private static final int TOY_COUNT_PER_ROUND = 45;
    // Настройки физики и котов берём из GameTuning,
    // чтобы удобно балансить поведение в одном месте.
    private static final float PHYSICS_TIME_STEP = GameTuning.PHYSICS_TIME_STEP;
    private static final float PHYSICS_MAX_ACCUMULATED_TIME = GameTuning.PHYSICS_MAX_ACCUMULATED_TIME;
    private static final float CAT_MOTION_MIN_X = GameTuning.CAT_MOTION_MIN_X;
    private static final float CAT_MOTION_MAX_X = GameTuning.CAT_MOTION_MAX_X;
    private static final float CAT_MOTION_MIN_SPEED = GameTuning.CAT_MOTION_MIN_SPEED;
    private static final float CAT_MOTION_MAX_SPEED = GameTuning.CAT_MOTION_MAX_SPEED;
    private static final float CAT_MOTION_MAX_VERTICAL_SPEED = GameTuning.CAT_MOTION_MAX_VERTICAL_SPEED;

    private final MainGame game;
    private final GameMode gameMode;
    private final MenagerieProgress menagerieProgress;
    private final GameSessionSettings sessionSettings;

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
    private final Rectangle factBounds = new Rectangle(0.75f, WORLD_HEIGHT - 2.0f, WORLD_WIDTH - 1.5f, 1.05f);
    private final Rectangle resultBounds = new Rectangle(1.4f, WORLD_HEIGHT * 0.52f, WORLD_WIDTH - 2.8f, 1.2f);

    // Пауза доступна из любого режима с возвратом в главное меню.
    private BitmapFont pauseFont;
    private Texture pauseOverlayTexture;
    private boolean pauseActive;
    private final Rectangle pausePanelBounds = new Rectangle(2.4f, 2.05f, WORLD_WIDTH - 4.8f, 4.5f);
    private final Rectangle resumeButtonBounds = new Rectangle(4.2f, 4.25f, WORLD_WIDTH - 8.4f, 0.95f);
    private final Rectangle menuButtonBounds = new Rectangle(4.2f, 3.0f, WORLD_WIDTH - 8.4f, 0.95f);
    // Сенсорные контролы размещаем по бокам и делаем круглыми/полупрозрачными.
    private static final float TOUCH_JOYSTICK_RADIUS = 0.82f;
    private static final float TOUCH_JOYSTICK_KNOB_RADIUS = 0.34f;
    private static final float TOUCH_ACTION_RADIUS = 0.78f;
    private final Vector2 touchJoystickCenter = new Vector2(1.50f, 1.05f);
    private final Vector2 touchActionCenter = new Vector2(WORLD_WIDTH - 1.42f, 1.04f);
    // Круги также храним как прямоугольники-ограничители для hit-test.
    private final Rectangle touchJoystickBounds = new Rectangle(
        touchJoystickCenter.x - TOUCH_JOYSTICK_RADIUS,
        touchJoystickCenter.y - TOUCH_JOYSTICK_RADIUS,
        TOUCH_JOYSTICK_RADIUS * 2f,
        TOUCH_JOYSTICK_RADIUS * 2f
    );
    private final Rectangle touchGrabButtonBounds = new Rectangle(
        touchActionCenter.x - TOUCH_ACTION_RADIUS,
        touchActionCenter.y - TOUCH_ACTION_RADIUS,
        TOUCH_ACTION_RADIUS * 2f,
        TOUCH_ACTION_RADIUS * 2f
    );
    // Позиция "ручки" джойстика (визуал + источник оси).
    private final Vector2 touchJoystickKnob = new Vector2(touchJoystickCenter);
    // Активные pointer id для мультитача: джойстик и кнопка действия.
    private int touchJoystickPointer = -1;
    private int touchActionPointer = -1;
    // Сырые состояния touch-управления, передаются в Claw каждый кадр.
    private float touchHorizontalAxis = 0f;
    // Целевая ось от джойстика: фильтруем её, чтобы убрать рывки на Android.
    private float touchHorizontalAxisTarget = 0f;
    private boolean touchActionPressed = false;
    private Texture touchCircleTexture;
    private Texture pixelTexture;
    private Texture rabbitLeftTexture;
    private Texture rabbitRightTexture;

    private FindAnimalFacts.FindAnimalTask findAnimalTask;
    private ToyType catchCatTargetToyType;
    private String catchCatTargetLabelRu;
    private boolean findAnimalRoundResolved;
    private String findAnimalResultText;
    private float findAnimalExitTimer;
    private boolean findAnimalExitRequested;
    private final Map<Toy, CatToyMotionState> catchCatMotionStates = new HashMap<>();
    // Аккумулятор фиксированного шага физики (FPS-independent).
    private float physicsAccumulator = 0f;

    public GameScreen(MainGame game, GameMode gameMode, GameSessionSettings sessionSettings) {
        // Экран знает игру (для возврата в меню) и свой режим.
        this.game = game;
        this.gameMode = gameMode;
        this.sessionSettings = sessionSettings == null ? GameSessionSettings.defaults() : sessionSettings;
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
        claw = new Claw(gameMode, this.sessionSettings);
        claw.createPhysics(world);
        claw.setWorld(world);
        debugOverlay = new DebugOverlay();

        if (gameMode == GameMode.FIND_ANIMAL) {
            setupFindAnimalRound();
        } else if (gameMode == GameMode.CATCH_CAT) {
            setupCatchCatRound();
        }

        // Единый шрифт используем для оверлея паузы и действий внутри него.
        pauseFont = createFont(28, new Color(0.98f, 0.92f, 0.82f, 1f));
        pauseOverlayTexture = createSolidTexture(1, 1, Color.WHITE);
        pixelTexture = pauseOverlayTexture;
        touchCircleTexture = createCircleTexture(192);
        rabbitLeftTexture = new Texture(Gdx.files.internal("toys/default/rabbit_big.png"));
        rabbitRightTexture = new Texture(Gdx.files.internal("toys/animals/rabbit_large.png"));

        createToys();
        // Перехватываем системную кнопку BACK, чтобы она открывала нашу паузу.
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new GameInputAdapter());
    }

    private void setupFindAnimalRound() {
        // На каждый запуск режима выбираем новую задачу из JSON.
        FindAnimalFacts facts = new FindAnimalFacts();
        ToyType[] findPool = menagerieProgress.getFindAnimalPool();
        if (findPool.length == 0) {
            // Защитный fallback: если режим открыт некорректно, не падаем с пустым пулом.
            findPool = ToyType.ANIMAL_POOL;
        }

        findAnimalTask = facts.createRandomTask(findPool);
        // Сбрасываем состояние завершения, чтобы раунд начинался "с нуля".
        findAnimalRoundResolved = false;
        findAnimalResultText = null;
        findAnimalExitTimer = 0f;
        findAnimalExitRequested = false;

        // Отдельные шрифты: для текста факта и для статуса победы/поражения.
        factFont = createFont(26, new Color(0.98f, 0.92f, 0.84f, 1f));
        statusFont = createFont(30, new Color(0.98f, 0.84f, 0.25f, 1f));
    }

    private void setupCatchCatRound() {
        ToyType[] catPool = ToyType.CAT_EMOTION_POOL;
        catchCatTargetToyType = catPool[(int) (Math.random() * catPool.length)];
        catchCatTargetLabelRu = getCatEmotionLabelRu(catchCatTargetToyType);

        findAnimalRoundResolved = false;
        findAnimalResultText = null;
        findAnimalExitTimer = 0f;
        findAnimalExitRequested = false;

        factFont = createFont(27, new Color(0.98f, 0.92f, 0.84f, 1f));
        statusFont = createFont(30, new Color(0.98f, 0.84f, 0.25f, 1f));
    }

    private String getCatEmotionLabelRu(ToyType toyType) {
        switch (toyType) {
            case CAT_BORED:
                return "скучающего кота";
            case CAT_EVIL:
                return "злого кота";
            case CAT_FUNNY:
                return "весёлого кота";
            case CAT_CRYING:
                return "грустного кота";
            case CAT_ILL:
                return "заболевшего кота";
            case CAT_NORMAL:
            default:
                return "обычного кота";
        }
    }

    private void createToys() {
        ToyType[] toyPool = getToyPoolForCurrentMode();
        ToyType[] currentRescueAnimals = menagerieProgress.getCurrentRescueLevelAnimals();
        ToyType[] completedRescueAnimals = menagerieProgress.getCompletedRescueAnimals();
        ToyType[] normalSpawnOrder = buildEqualNormalSpawnOrder(toyPool);

        if (gameMode == GameMode.CATCH_CAT) {
            createCatchCatToys(toyPool);
            return;
        }

        for (int i = 0; i < TOY_COUNT_PER_ROUND; i++) {
            float x = 3.5f + (float) Math.random() * 6.5f;
            float y = 1.0f + (float) Math.random() * 2.5f;

            ToyType toyType = pickToyTypeForSpawn(i, toyPool, currentRescueAnimals, completedRescueAnimals, normalSpawnOrder);

            float difficulty = 0.2f + (float) Math.random() * 0.5f;
            float restitution = 0.1f + (float) Math.random() * 0.3f;

            Toy toy = new Toy(world, x, y, toyType, difficulty, restitution);
            toys.add(toy);
            if (gameMode == GameMode.CATCH_CAT) {
                catchCatMotionStates.put(toy, new CatToyMotionState());
            }
        }
    }

    private void createCatchCatToys(ToyType[] catPool) {
        // В режиме CATCH_CAT создаём ровно по одному коту на каждый доступный ассет.
        for (int i = 0; i < catPool.length; i++) {
            float x = 3.0f + (float) Math.random() * 7.8f;
            float y = 1.0f + (float) Math.random() * 2.1f;
            float difficulty = 0.22f + (float) Math.random() * 0.25f;
            float restitution = 0.06f + (float) Math.random() * 0.06f;

            Toy toy = new Toy(world, x, y, catPool[i], difficulty, restitution, true);
            toys.add(toy);
            catchCatMotionStates.put(toy, new CatToyMotionState());
        }
    }

    private ToyType pickToyTypeForSpawn(
        int spawnIndex,
        ToyType[] modePool,
        ToyType[] currentRescueAnimals,
        ToyType[] completedRescueAnimals,
        ToyType[] normalSpawnOrder
    ) {
        if (gameMode == GameMode.RESCUE) {
            // 1-й уровень: только текущие 5 зверей.
            if (completedRescueAnimals.length == 0) {
                return currentRescueAnimals[(int) (Math.random() * currentRescueAnimals.length)];
            }

            // 2+ уровни: 80% уже знакомые звери, 20% новые звери уровня.
            float newPercent = menagerieProgress.getRescueNewAnimalPercent() / 100f;
            boolean spawnNew = Math.random() < newPercent;
            ToyType[] selectedPool = spawnNew ? currentRescueAnimals : completedRescueAnimals;

            // Если один из пулов пуст (edge-case после смены версии), безопасно падаем в общий.
            if (selectedPool.length == 0) {
                selectedPool = modePool;
            }

            return selectedPool[(int) (Math.random() * selectedPool.length)];
        }

        if (gameMode == GameMode.FIND_ANIMAL && findAnimalTask != null && spawnIndex == 0) {
            // В FIND_ANIMAL гарантируем наличие целевой игрушки,
            // но оставляем общее количество ровно 45.
            return findAnimalTask.getTargetToyType();
        }

        if (gameMode == GameMode.CATCH_CAT && catchCatTargetToyType != null && spawnIndex == 0) {
            // В CATCH_CAT тоже гарантируем хотя бы один целевой тип в раунде.
            return catchCatTargetToyType;
        }

        if (gameMode == GameMode.NORMAL && normalSpawnOrder.length > 0) {
            return normalSpawnOrder[spawnIndex % normalSpawnOrder.length];
        }

        return modePool[(int) (Math.random() * modePool.length)];
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();

        // ВАЖНО: переключаем экран только после завершения текущего кадра.
        if (findAnimalExitRequested) {
            findAnimalExitRequested = false;
            game.showPreviousMenu();
        }
    }

    private void update(float delta) {
        if (pauseActive) {
            // В паузе принудительно обнуляем мобильный ввод, чтобы не было "залипания".
            claw.setTouchHorizontalAxis(0f);
            claw.setTouchActionPressed(false);
            physicsAccumulator = 0f;
            return;
        }

        // Пробрасываем актуальные значения touch-ввода в игровую логику клешни.
        // На Android дополнительно сглаживаем ось, чтобы джойстик не создавал
        // резкие скачки скорости и избыточную раскачку.
        if (isTouchControlsVisible()) {
            float smoothing = clamp(delta * 14f, 0f, 1f);
            touchHorizontalAxis += (touchHorizontalAxisTarget - touchHorizontalAxis) * smoothing;
        }
        claw.setTouchHorizontalAxis(touchHorizontalAxis);
        claw.setTouchActionPressed(touchActionPressed);
        claw.update(delta, toys, trayToys, winZone);

        // Фиксированный шаг Box2D через аккумулятор:
        // симуляция идёт одинаково на desktop/mobile при разном FPS.
        stepWorldFixed(delta);

        for (Toy toy : toys) {
            toy.update(delta, winZone);
        }
        for (Toy toy : trayToys) {
            toy.update(delta, winZone);
        }

        if (gameMode == GameMode.CATCH_CAT) {
            updateCatchCatToyMotion(delta);
        }

        updateMenagerieUnlocks();
        updateFindAnimalRoundState();
        updateFindAnimalRoundExitTimer(delta);
        debugOverlay.updateToggle();
    }

    private void updateCatchCatToyMotion(float delta) {
        if (isFindAnimalFinished()) {
            return;
        }

        for (Toy toy : toys) {
            if (toy.isCaptured()
                || toy.isInTray()
                || toy.isWon()
                || toy.isReleasedToPhysicsTray()
                || trayToys.contains(toy)
                || toy.getBody().getType() != BodyDef.BodyType.DynamicBody
                || toy.isInsideTrayBounds(winZone)) {
                continue;
            }

            CatToyMotionState motionState = catchCatMotionStates.computeIfAbsent(toy, key -> new CatToyMotionState());
            motionState.update(delta, toy);
        }
    }

    private void stepWorldFixed(float frameDelta) {
        float clampedDelta = Math.min(frameDelta, PHYSICS_MAX_ACCUMULATED_TIME);
        physicsAccumulator += clampedDelta;

        while (physicsAccumulator >= PHYSICS_TIME_STEP) {
            world.step(PHYSICS_TIME_STEP, 6, 2);
            physicsAccumulator -= PHYSICS_TIME_STEP;
        }
    }

    private boolean isToyTouchingAnotherToy(Toy toy) {
        Body toyBody = toy.getBody();
        for (Contact contact : world.getContactList()) {
            if (!contact.isTouching()) {
                continue;
            }

            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            Body bodyA = fixtureA.getBody();
            Body bodyB = fixtureB.getBody();
            Body otherBody = null;

            if (bodyA == toyBody) {
                otherBody = bodyB;
            } else if (bodyB == toyBody) {
                otherBody = bodyA;
            }

            if (otherBody == null) {
                continue;
            }

            if (otherBody.getUserData() instanceof Toy) {
                return true;
            }
        }
        return false;
    }

    private void updateFindAnimalRoundExitTimer(float delta) {
        // После показа результата автоматически возвращаем игрока в предыдущее меню.
        if (!isFindAnimalFinished()) {
            return;
        }

        findAnimalExitTimer -= delta;
        if (findAnimalExitTimer <= 0f) {
            findAnimalExitRequested = true;
        }
    }

    private void updateFindAnimalRoundState() {
        // Логика результата нужна только до момента, пока раунд не завершён.
        if ((gameMode != GameMode.FIND_ANIMAL && gameMode != GameMode.CATCH_CAT) || findAnimalRoundResolved) {
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
        if (reportedWins.contains(toy)) {
            return;
        }

        if (!hasToyReachedTray(toy)) {
            return;
        }

        reportedWins.add(toy);
        findAnimalRoundResolved = true;
        findAnimalExitTimer = FIND_ANIMAL_RESULT_SHOW_TIME;

        ToyType targetToyType = getCurrentTargetToyType();
        String targetLabel = getCurrentTargetLabelRu();
        if (targetToyType != null && toy.getToyType() == targetToyType) {
            findAnimalResultText = "Молодец, это действительно " + targetLabel;
        } else {
            findAnimalResultText = "Было близко, но нужно было поймать " + targetLabel;
        }
    }

    private ToyType getCurrentTargetToyType() {
        if (gameMode == GameMode.CATCH_CAT) {
            return catchCatTargetToyType;
        }
        return findAnimalTask == null ? null : findAnimalTask.getTargetToyType();
    }

    private String getCurrentTargetLabelRu() {
        if (gameMode == GameMode.CATCH_CAT) {
            return catchCatTargetLabelRu == null ? "нужного кота" : catchCatTargetLabelRu;
        }
        return findAnimalTask == null ? "нужного зверя" : findAnimalTask.getTargetAnimalLabelRu();
    }

    private void updateMenagerieUnlocks() {
        // Прогресс зверинца и уровней обновляется только в RESCUE.
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
        if (gameMode == GameMode.RESCUE) {
            ToyType[] currentLevelAnimals = menagerieProgress.getCurrentRescueLevelAnimals();
            return currentLevelAnimals.length == 0 ? ToyType.ANIMAL_POOL : currentLevelAnimals;
        }

        if (gameMode == GameMode.FIND_ANIMAL) {
            ToyType[] unlockedFindPool = menagerieProgress.getFindAnimalPool();
            return unlockedFindPool.length == 0 ? ToyType.ANIMAL_POOL : unlockedFindPool;
        }

        if (gameMode == GameMode.CATCH_CAT) {
            return ToyType.CAT_EMOTION_POOL;
        }

        ToyType[] configuredPool = sessionSettings.getNormalToyPool();
        if (configuredPool != null && configuredPool.length > 0) {
            return configuredPool;
        }

        return menagerieProgress.getNormalModePool();
    }

    private ToyType[] buildEqualNormalSpawnOrder(ToyType[] toyPool) {
        if (gameMode != GameMode.NORMAL || toyPool.length == 0) {
            return new ToyType[0];
        }

        List<ToyType> order = new ArrayList<>();
        int baseCount = TOY_COUNT_PER_ROUND / toyPool.length;
        int remainder = TOY_COUNT_PER_ROUND % toyPool.length;

        for (int i = 0; i < toyPool.length; i++) {
            int repeats = baseCount + (i < remainder ? 1 : 0);
            for (int j = 0; j < repeats; j++) {
                order.add(toyPool[i]);
            }
        }

        java.util.Collections.shuffle(order);
        return order.toArray(new ToyType[0]);
    }

    private boolean hasToyReachedTray(Toy toy) {
        return toy.isWon() || toy.isInsideWinZone(winZone) || toy.isInsideTrayBounds(winZone);
    }

    private void registerWonToy(Toy toy) {
        if (reportedWins.contains(toy) || !hasToyReachedTray(toy)) {
            return;
        }

        reportedWins.add(toy);

        // В режиме спасения карточка зверинца открывается по старой логике.
        menagerieProgress.unlock(toy.getToyType());
        menagerieProgress.markRescued(toy.getToyType());

        // Как только собраны все 5 уникальных зверей уровня,
        // они становятся доступны в NORMAL/FIND и открывается следующий уровень.
        menagerieProgress.completeCurrentRescueLevelIfNeeded();
    }

    private void draw() {
        ScreenUtils.clear(0.03f, 0.03f, 0.04f, 1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawMachineBackdrop();

        for (Toy toy : toys) {
            toy.render(batch);
        }
        for (Toy toy : trayToys) {
            toy.render(batch);
        }

        claw.render(batch);
        drawMachineForeground();

        if (gameMode == GameMode.FIND_ANIMAL || gameMode == GameMode.CATCH_CAT) {
            drawFindAnimalUi();
        }

        if (pauseActive) {
            drawPauseOverlay();
        } else {
            // Контролы рисуем поверх мира, но только вне состояния паузы.
            drawTouchControls();
        }

        batch.end();

        debugOverlay.render(camera, claw, winZone);
    }

    private void drawMachineBackdrop() {
        batch.setColor(0.05f, 0.06f, 0.13f, 1f);
        batch.draw(pixelTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        batch.setColor(0.28f, 0.24f, 0.50f, 0.94f);
        batch.draw(pixelTexture, 0.65f, 0.72f, WORLD_WIDTH - 1.3f, WORLD_HEIGHT - 1.45f);

        batch.setColor(0.56f, 0.47f, 0.74f, 0.30f);
        batch.draw(pixelTexture, 0.8f, 0.7f, WORLD_WIDTH - 1.6f, 1.9f);

        batch.setColor(0.99f, 0.93f, 0.72f, 0.9f);
        batch.draw(touchCircleTexture, 2.0f, 5.2f, 0.85f, 0.85f);
        batch.setColor(0.30f, 0.26f, 0.52f, 1f);
        batch.draw(touchCircleTexture, 2.25f, 5.35f, 0.72f, 0.72f);

        drawStar(3.8f, 6.0f, 0.08f);
        drawStar(5.5f, 6.35f, 0.06f);
        drawStar(7.3f, 5.95f, 0.07f);
        drawStar(9.8f, 6.2f, 0.08f);
        drawStar(11.5f, 5.9f, 0.06f);
        drawStar(12.7f, 6.3f, 0.07f);

        batch.setColor(1f, 1f, 1f, 0.07f);
        batch.draw(pixelTexture, 1.8f, 1.2f, 0.8f, 6.8f);
        batch.draw(pixelTexture, 10.9f, 1.1f, 0.65f, 6.7f);

        drawNeonFrame(0.35f, 0.38f, WORLD_WIDTH - 0.7f, WORLD_HEIGHT - 0.76f, 0.16f, new Color(0.84f, 0.70f, 0.98f, 0.95f));
        drawNeonFrame(0.55f, 0.58f, WORLD_WIDTH - 1.1f, WORLD_HEIGHT - 1.16f, 0.10f, new Color(0.30f, 0.30f, 0.55f, 0.95f));

        drawMachineTitle();
        batch.setColor(Color.WHITE);
    }

    private void drawMachineForeground() {
        batch.setColor(0.25f, 0.24f, 0.46f, 0.97f);
        batch.draw(pixelTexture, 0f, 0f, WORLD_WIDTH, 0.24f);
        batch.setColor(0.43f, 0.40f, 0.70f, 0.95f);
        batch.draw(pixelTexture, 0f, 0.22f, WORLD_WIDTH, 0.03f);
        batch.setColor(0.70f, 0.66f, 0.95f, 0.35f);
        batch.draw(pixelTexture, 0f, 0.68f, WORLD_WIDTH, 0.03f);

        float trayX = winZone.getX() - winZone.getWidth() * 0.5f;
        float trayY = winZone.getY();
        float trayW = winZone.getWidth();
        float trayH = winZone.getHeight();
        batch.setColor(0.88f, 0.90f, 1f, 0.16f);
        batch.draw(pixelTexture, trayX, trayY, trayW, trayH);
        drawNeonFrame(trayX, trayY, trayW, trayH, 0.08f, new Color(0.80f, 0.82f, 0.96f, 0.62f));
        batch.setColor(0.86f, 0.90f, 1f, 0.10f);
        batch.draw(pixelTexture, trayX + 0.04f, trayY + trayH * 0.52f, trayW - 0.08f, trayH * 0.40f);
        batch.setColor(Color.WHITE);
    }

    private void drawMachineTitle() {
        if (pauseFont == null) {
            return;
        }

        pauseFont.getData().setScale(0.0135f);
        String title = "RabbitClaw";
        glyphLayout.setText(pauseFont, title);
        float titleX = (WORLD_WIDTH - glyphLayout.width) * 0.5f;
        float titleY = WORLD_HEIGHT * 0.675f;
        float sharpX = (float) Math.round(titleX);
        float sharpY = (float) Math.round(titleY);

        pauseFont.setColor(0.08f, 0.06f, 0.16f, 0.72f);
        pauseFont.draw(batch, glyphLayout, sharpX + 1f / 90f, sharpY - 1f / 90f);
        pauseFont.setColor(0.98f, 0.92f, 0.78f, 1f);
        pauseFont.draw(batch, glyphLayout, sharpX, sharpY);
        pauseFont.setColor(0.98f, 0.92f, 0.82f, 1f);
    }

    private void drawNeonFrame(float x, float y, float width, float height, float thickness, Color color) {
        batch.setColor(color);
        batch.draw(pixelTexture, x, y, width, thickness);
        batch.draw(pixelTexture, x, y + height - thickness, width, thickness);
        batch.draw(pixelTexture, x, y, thickness, height);
        batch.draw(pixelTexture, x + width - thickness, y, thickness, height);
    }

    private void drawStar(float x, float y, float size) {
        batch.setColor(0.99f, 0.91f, 0.68f, 0.92f);
        batch.draw(touchCircleTexture, x, y, size, size);
    }

    private void drawFindAnimalUi() {
        if (factFont == null) {
            return;
        }

        factFont.getData().setScale(0.011f);
        String topText = gameMode == GameMode.CATCH_CAT
            ? "Поймай " + getCurrentTargetLabelRu()
            : findAnimalTask == null ? "" : "Факт: " + findAnimalTask.getFact();
        glyphLayout.setText(factFont, topText, factFont.getColor(), factBounds.width, Align.center, true);
        factFont.draw(batch, glyphLayout, factBounds.x, factBounds.y + factBounds.height);

        if (!isFindAnimalFinished()) {
            return;
        }

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

        factFont.getData().setScale(0.012f);
        int secondsLeft = Math.max(1, (int) Math.ceil(findAnimalExitTimer));
        String hint = "Возврат в меню через " + secondsLeft + " сек.";
        glyphLayout.setText(factFont, hint);
        factFont.draw(batch, glyphLayout, (WORLD_WIDTH - glyphLayout.width) * 0.5f, resultBounds.y - 0.2f);
    }

    private void drawPauseOverlay() {
        if (pauseFont == null) {
            return;
        }

        pauseFont.getData().setScale(0.013f);
        glyphLayout.setText(pauseFont, "Пауза");

        // Делаем паузу в том же стиле, что и меню/игра.
        batch.setColor(0.05f, 0.06f, 0.13f, 0.84f);
        batch.draw(pauseOverlayTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        batch.setColor(0.28f, 0.24f, 0.50f, 0.35f);
        batch.draw(pauseOverlayTexture, 0.65f, 0.72f, WORLD_WIDTH - 1.3f, WORLD_HEIGHT - 1.45f);
        batch.setColor(0.56f, 0.47f, 0.74f, 0.20f);
        batch.draw(pauseOverlayTexture, 0.8f, 0.7f, WORLD_WIDTH - 1.6f, 1.7f);
        batch.setColor(1f, 1f, 1f, 0.08f);
        batch.draw(rabbitLeftTexture, 0.35f, 0.40f, 1.25f, 1.25f);
        batch.draw(rabbitRightTexture, WORLD_WIDTH - 1.75f, 0.42f, 1.22f, 1.22f);
        batch.setColor(Color.WHITE);

        batch.setColor(0.24f, 0.23f, 0.30f, 0.95f);
        batch.draw(pauseOverlayTexture, pausePanelBounds.x, pausePanelBounds.y, pausePanelBounds.width, pausePanelBounds.height);
        batch.setColor(Color.WHITE);

        pauseFont.getData().setScale(0.011f);
        glyphLayout.setText(pauseFont, "RabbitClaw");
        pauseFont.setColor(0.98f, 0.92f, 0.80f, 0.30f);
        pauseFont.draw(batch, glyphLayout, (WORLD_WIDTH - glyphLayout.width) * 0.5f, pausePanelBounds.y + pausePanelBounds.height + 0.25f);
        pauseFont.setColor(0.98f, 0.92f, 0.82f, 1f);

        pauseFont.getData().setScale(0.013f);
        glyphLayout.setText(pauseFont, "Пауза");
        pauseFont.draw(batch, glyphLayout, (WORLD_WIDTH - glyphLayout.width) * 0.5f, pausePanelBounds.y + pausePanelBounds.height - 0.6f);

        drawPauseButton(resumeButtonBounds, "Продолжить (Esc / Enter)");
        drawPauseButton(menuButtonBounds, "Выйти в главное меню (M)");
    }

    private void drawPauseButton(Rectangle bounds, String label) {
        batch.setColor(0.42f, 0.40f, 0.52f, 0.98f);
        batch.draw(pauseOverlayTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(Color.WHITE);

        pauseFont.getData().setScale(0.0105f);
        glyphLayout.setText(pauseFont, label);
        pauseFont.draw(batch, glyphLayout, bounds.x + (bounds.width - glyphLayout.width) * 0.5f,
            bounds.y + (bounds.height + glyphLayout.height) * 0.5f);
    }

    private void drawTouchControls() {
        // На desktop/web этот UI не показываем.
        if (!isTouchControlsVisible()) {
            return;
        }

        drawTouchJoystick();
        drawTouchActionButton();
    }

    private void drawTouchJoystick() {
        // Визуально "гасим" джойстик, если по правилам режима он временно недоступен.
        boolean enabled = claw != null && claw.isHorizontalControlAllowed();
        float plateAlpha = enabled ? 0.20f : 0.10f;
        float ringAlpha = enabled ? 0.34f : 0.18f;
        float knobAlpha = enabled ? 0.42f : 0.22f;

        // Основание джойстика: тёмный полупрозрачный круг.
        float baseSize = TOUCH_JOYSTICK_RADIUS * 2f;
        batch.setColor(0.09f, 0.10f, 0.13f, plateAlpha);
        batch.draw(
            touchCircleTexture,
            touchJoystickCenter.x - TOUCH_JOYSTICK_RADIUS,
            touchJoystickCenter.y - TOUCH_JOYSTICK_RADIUS,
            baseSize,
            baseSize
        );

        // Внешнее "кольцо": рисуем второй, чуть меньший круг цветом контура.
        float ringSize = (TOUCH_JOYSTICK_RADIUS - 0.05f) * 2f;
        batch.setColor(0.92f, 0.94f, 0.98f, ringAlpha);
        batch.draw(
            touchCircleTexture,
            touchJoystickCenter.x - ringSize * 0.5f,
            touchJoystickCenter.y - ringSize * 0.5f,
            ringSize,
            ringSize
        );

        // Внутренность снова затемняем, чтобы сформировать визуальное кольцо.
        float ringInnerSize = (TOUCH_JOYSTICK_RADIUS - 0.09f) * 2f;
        batch.setColor(0.12f, 0.14f, 0.17f, enabled ? 0.30f : 0.16f);
        batch.draw(
            touchCircleTexture,
            touchJoystickCenter.x - ringInnerSize * 0.5f,
            touchJoystickCenter.y - ringInnerSize * 0.5f,
            ringInnerSize,
            ringInnerSize
        );

        // Ручка джойстика.
        float knobSize = TOUCH_JOYSTICK_KNOB_RADIUS * 2f;
        batch.setColor(0.92f, 0.95f, 1.0f, knobAlpha);
        batch.draw(
            touchCircleTexture,
            touchJoystickKnob.x - TOUCH_JOYSTICK_KNOB_RADIUS,
            touchJoystickKnob.y - TOUCH_JOYSTICK_KNOB_RADIUS,
            knobSize,
            knobSize
        );
        // Мини-иконка направления: невысокий шум, но хорошо читается на тёмном фоне.
        if (pauseFont != null) {
            pauseFont.getData().setScale(0.011f);
            pauseFont.setColor(1f, 1f, 1f, enabled ? 0.62f : 0.34f);
            glyphLayout.setText(pauseFont, "◀ ▶");
            pauseFont.draw(batch, glyphLayout, touchJoystickCenter.x - glyphLayout.width * 0.5f, touchJoystickCenter.y + 0.05f);
            pauseFont.setColor(Color.WHITE);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawTouchActionButton() {
        // Кнопка тоже может быть временно недоступна (например, пока цикл автодвижения).
        boolean enabled = claw != null && claw.isActionControlAllowed();
        // В FIND_ANIMAL после захвата меняем подпись на "Отпустить".
        String label = claw != null && claw.shouldShowReleaseAction() ? "Отпустить" : "Захват";
        float outerAlpha = enabled ? 0.46f : 0.26f;
        float innerAlpha = enabled ? 0.28f : 0.16f;

        // Внешний круг кнопки.
        float outerSize = TOUCH_ACTION_RADIUS * 2f;
        batch.setColor(0.96f, 0.78f, 0.18f, outerAlpha);
        batch.draw(
            touchCircleTexture,
            touchActionCenter.x - TOUCH_ACTION_RADIUS,
            touchActionCenter.y - TOUCH_ACTION_RADIUS,
            outerSize,
            outerSize
        );

        // Внутренний затемнённый круг, чтобы кнопка была аккуратной и не "кричащей".
        float innerRadius = TOUCH_ACTION_RADIUS - 0.08f;
        float innerSize = innerRadius * 2f;
        batch.setColor(0.12f, 0.13f, 0.16f, innerAlpha);
        batch.draw(
            touchCircleTexture,
            touchActionCenter.x - innerRadius,
            touchActionCenter.y - innerRadius,
            innerSize,
            innerSize
        );
        batch.setColor(Color.WHITE);

        if (pauseFont != null) {
            pauseFont.getData().setScale(0.0102f);
            glyphLayout.setText(pauseFont, label);
            pauseFont.draw(
                batch,
                glyphLayout,
                touchActionCenter.x - glyphLayout.width * 0.5f,
                touchActionCenter.y + glyphLayout.height * 0.32f
            );
        }
    }

    private boolean isFindAnimalFinished() {
        return (gameMode == GameMode.FIND_ANIMAL || gameMode == GameMode.CATCH_CAT) && findAnimalRoundResolved;
    }

    private final class CatToyMotionState {
        private float desiredHorizontalSpeed = randomCruiseSpeed();
        private float directionChangeTimer = 1.4f + (float) Math.random() * 1.6f;
        private float jumpTimer = 0.55f + (float) Math.random() * 0.75f;
        private float stuckTimer = 0f;

        private void update(float delta, Toy toy) {
            Body toyBody = toy.getBody();
            Vector2 velocity = toyBody.getLinearVelocity();
            Vector2 position = toyBody.getPosition();
            toyBody.setAwake(true);

            // Активное управление котом включаем только на полу,
            // чтобы в воздухе (или в клешне) не было "магического" разгона.
            boolean nearFloor = position.y < GameTuning.CAT_MOTION_GROUND_Y
                && Math.abs(velocity.y) < GameTuning.CAT_MOTION_GROUND_MAX_VY;
            directionChangeTimer -= delta;
            jumpTimer -= delta;
            if (directionChangeTimer <= 0f) {
                float currentDirection = Math.signum(desiredHorizontalSpeed);
                boolean keepDirection = Math.random() < 0.62f && currentDirection != 0f;
                float nextDirection = keepDirection
                    ? currentDirection
                    : (currentDirection >= 0f ? -1f : 1f);
                float speed = CAT_MOTION_MIN_SPEED
                    + (float) Math.random() * (CAT_MOTION_MAX_SPEED - CAT_MOTION_MIN_SPEED);
                desiredHorizontalSpeed = speed * nextDirection;
                directionChangeTimer = 1.2f + (float) Math.random() * 2.0f;
            }

            if (position.x < CAT_MOTION_MIN_X) {
                desiredHorizontalSpeed = Math.abs(desiredHorizontalSpeed);
            } else if (position.x > CAT_MOTION_MAX_X) {
                desiredHorizontalSpeed = -Math.abs(desiredHorizontalSpeed);
            }

            if (nearFloor) {
                // Экспоненциальное сглаживание: независимо от FPS
                // тянем текущую скорость к целевой.
                float response = Math.max(0.1f, GameTuning.CAT_MOTION_VELOCITY_RESPONSE);
                float blend = 1f - (float) Math.exp(-response * delta);
                float nextVx = velocity.x + (desiredHorizontalSpeed - velocity.x) * blend;
                toyBody.setLinearVelocity(nextVx, velocity.y);
            }

            float horizontalSpeed = Math.abs(toyBody.getLinearVelocity().x);
            boolean touchingToy = isToyTouchingAnotherToy(toy);
            if (nearFloor && horizontalSpeed < GameTuning.CAT_MOTION_STUCK_SPEED) {
                stuckTimer += delta;
            } else {
                stuckTimer = 0f;
            }

            // Если кот упёрся в другого кота и не может разлипнуться,
            // с вероятностью делаем "перепрыг" поверх соседа.
            if (stuckTimer > GameTuning.CAT_MOTION_STUCK_TIME && nearFloor) {
                boolean shouldHop = touchingToy && Math.random() < GameTuning.CAT_MOTION_STUCK_HOP_CHANCE;
                if (shouldHop) {
                    float unstickJump = GameTuning.CAT_MOTION_UNSTICK_JUMP_MIN
                        + (float) Math.random() * GameTuning.CAT_MOTION_UNSTICK_JUMP_RANDOM;
                    float unstickSide = Math.signum(desiredHorizontalSpeed) * GameTuning.CAT_MOTION_UNSTICK_SIDE_IMPULSE;
                    toyBody.applyLinearImpulse(
                        unstickSide,
                        unstickJump,
                        toyBody.getWorldCenter().x,
                        toyBody.getWorldCenter().y,
                        true
                    );
                } else {
                    desiredHorizontalSpeed = -desiredHorizontalSpeed;
                }
                stuckTimer = 0f;
                jumpTimer = 0.40f + (float) Math.random() * 0.45f;
            }

            if (jumpTimer <= 0f && nearFloor) {
                float jumpImpulse = GameTuning.CAT_MOTION_JUMP_MIN
                    + (float) Math.random() * GameTuning.CAT_MOTION_JUMP_RANDOM;
                float sideImpulse = Math.signum(desiredHorizontalSpeed) * GameTuning.CAT_MOTION_JUMP_SIDE_IMPULSE;
                toyBody.applyLinearImpulse(sideImpulse, jumpImpulse, toyBody.getWorldCenter().x, toyBody.getWorldCenter().y, true);
                jumpTimer = 0.58f + (float) Math.random() * 0.95f;
            }

            float limitedSpeedX = clamp(toyBody.getLinearVelocity().x, -CAT_MOTION_MAX_SPEED, CAT_MOTION_MAX_SPEED);
            float limitedSpeedY = clamp(toyBody.getLinearVelocity().y, -CAT_MOTION_MAX_VERTICAL_SPEED, CAT_MOTION_MAX_VERTICAL_SPEED);
            toyBody.setLinearVelocity(limitedSpeedX, limitedSpeedY);
            toyBody.setAngularVelocity(0f);
        }

        private float randomCruiseSpeed() {
            float abs = CAT_MOTION_MIN_SPEED + (float) Math.random() * (CAT_MOTION_MAX_SPEED - CAT_MOTION_MIN_SPEED);
            return Math.random() < 0.5f ? -abs : abs;
        }
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
        // Явно очищаем фон, чтобы не получить случайные "квадраты" на некоторых GPU/драйверах.
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private BitmapFont createFont(int size, Color color) {
        return createFont(size, color, false);
    }

    private BitmapFont createFont(int size, Color color, boolean sharp) {
        FileHandle internalFont = Gdx.files.internal(FONT_PATH);
        if (!internalFont.exists()) {
            BitmapFont fallback = new BitmapFont();
            fallback.setUseIntegerPositions(sharp);
            fallback.setColor(color);
            return fallback;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(internalFont);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.minFilter = sharp ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        parameter.magFilter = sharp ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "№«»—…";

        BitmapFont font = generator.generateFont(parameter);
        font.setUseIntegerPositions(sharp);
        generator.dispose();
        return font;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        // Возвращаем стандартное поведение BACK при выходе с игрового экрана.
        Gdx.input.setCatchKey(Input.Keys.BACK, false);
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }
    }

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
        if (pauseFont != null) {
            pauseFont.dispose();
        }
        if (pauseOverlayTexture != null) {
            pauseOverlayTexture.dispose();
        }
        if (touchCircleTexture != null) {
            touchCircleTexture.dispose();
        }
        if (rabbitLeftTexture != null) {
            rabbitLeftTexture.dispose();
        }
        if (rabbitRightTexture != null) {
            rabbitRightTexture.dispose();
        }
    }

    private final class GameInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (pauseActive) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK || keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    pauseActive = false;
                    return true;
                }

                if (keycode == Input.Keys.M) {
                    game.showMainMenu();
                    return true;
                }

                return true;
            }

            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK || keycode == Input.Keys.P) {
                pauseActive = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Переводим экранные координаты в мировые для hit-test по UI-прямоугольникам.
            Vector2 worldTouch = viewport.unproject(new Vector2(screenX, screenY));
            if (!pauseActive) {
                return handleTouchGameplayDown(worldTouch, pointer);
            }

            if (resumeButtonBounds.contains(worldTouch)) {
                pauseActive = false;
                return true;
            }

            if (menuButtonBounds.contains(worldTouch)) {
                game.showMainMenu();
                return true;
            }

            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            // Джойстик работает только вне паузы и только на Android.
            if (pauseActive || !isTouchControlsVisible()) {
                return false;
            }

            // Перетаскивание обрабатывает только pointer, который "взял" джойстик.
            if (pointer != touchJoystickPointer) {
                return false;
            }

            updateJoystickFromTouch(viewport.unproject(new Vector2(screenX, screenY)));
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            // Отпустили палец джойстика — возвращаем ручку в центр и обнуляем ось.
            if (pointer == touchJoystickPointer) {
                releaseTouchJoystick();
                return true;
            }

            // Отпустили палец кнопки — сбрасываем action.
            if (pointer == touchActionPointer) {
                touchActionPointer = -1;
                touchActionPressed = false;
                return true;
            }

            return false;
        }

        private boolean handleTouchGameplayDown(Vector2 worldTouch, int pointer) {
            if (!isTouchControlsVisible()) {
                return false;
            }

            // Привязка pointer к джойстику только если джойстик сейчас разрешён.
            if (touchJoystickPointer == -1
                && claw != null
                && claw.isHorizontalControlAllowed()
                && isPointInsideCircle(worldTouch, touchJoystickCenter, TOUCH_JOYSTICK_RADIUS)) {
                touchJoystickPointer = pointer;
                updateJoystickFromTouch(worldTouch);
                return true;
            }

            // Привязка pointer к кнопке действия только если она сейчас разрешена.
            if (touchActionPointer == -1
                && claw != null
                && claw.isActionControlAllowed()
                && isPointInsideCircle(worldTouch, touchActionCenter, TOUCH_ACTION_RADIUS)) {
                touchActionPointer = pointer;
                touchActionPressed = true;
                return true;
            }

            return false;
        }
    }

    private boolean isTouchControlsVisible() {
        // Сенсорный UI нужен только на Android-сборке.
        return Gdx.app.getType() == Application.ApplicationType.Android;
    }

    private void updateJoystickFromTouch(Vector2 worldTouch) {
        // Вычисляем смещение ручки от центра.
        float radius = TOUCH_JOYSTICK_RADIUS;
        float dx = worldTouch.x - touchJoystickCenter.x;
        float dy = worldTouch.y - touchJoystickCenter.y;

        // Ограничиваем смещение радиусом джойстика.
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > radius && distance > 0f) {
            float scale = radius / distance;
            dx *= scale;
            dy *= scale;
        }

        // Переводим смещение в ось [-1..1] только по X.
        touchJoystickKnob.set(touchJoystickCenter.x + dx, touchJoystickCenter.y + dy);
        float axis = clamp(dx / radius, -1f, 1f);
        // Небольшая "мёртвая зона", чтобы микродрожание пальца не вызывало рывков.
        if (Math.abs(axis) < 0.08f) {
            axis = 0f;
        }
        touchHorizontalAxisTarget = axis;
    }

    private void releaseTouchJoystick() {
        // Возврат в нейтральное положение.
        touchJoystickPointer = -1;
        touchHorizontalAxis = 0f;
        touchHorizontalAxisTarget = 0f;
        touchJoystickKnob.set(touchJoystickCenter);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isPointInsideCircle(Vector2 point, Vector2 center, float radius) {
        float dx = point.x - center.x;
        float dy = point.y - center.y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
