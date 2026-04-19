package ru.captainxsander;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
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

    public GameScreen(GameMode gameMode) {
        // Экран знает свой режим, потому что только rescue открывает карточки.
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

        createToys();
    }

    private void createToys() {
        // В обычной игре оставляем знакомый набор игрушек,
        // а в режиме спасения используем весь каталог зверинца.
        // В "Поиске Зверей" используем тот же полный каталог,
        // что и в "Спасении", чтобы можно было искать любую игрушку.
        ToyType[] toyPool = usesAllToysPool() ? ToyType.values() : ToyType.NORMAL_POOL;

        for (int i = 0; i < 45; i++) {

            float x = 3.5f + (float) Math.random() * 6.5f;
            float y = 1.0f + (float) Math.random() * 2.5f;

            ToyType toyType = toyPool[(int) (Math.random() * toyPool.length)];

            float difficulty = 0.2f + (float) Math.random() * 0.5f;
            float restitution = 0.1f + (float) Math.random() * 0.3f;

            toys.add(new Toy(world, x, y, toyType, difficulty, restitution));
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
        debugOverlay.updateToggle();
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

    private boolean usesAllToysPool() {
        // Полный пул нужен в двух режимах:
        // 1) rescue — для открытия карточек;
        // 2) find-animal — для ручного поиска конкретной игрушки в куче.
        return gameMode == GameMode.RESCUE || gameMode == GameMode.FIND_ANIMAL;
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
        batch.end();

        debugOverlay.render(camera, claw, winZone);
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
    }
}
