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
import java.util.List;

public class GameScreen implements Screen {

    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;

    private World world;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Floor floor;
    private WinZone winZone;
    private Claw claw;

    private final List<Toy> toys = new ArrayList<>();
    private final List<Toy> trayToys = new ArrayList<>();

    @Override
    public void show() {
        Box2D.init();

        world = new World(new Vector2(0, -9.8f), true);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        batch = new SpriteBatch();

        floor = new Floor();
        floor.create(world);

        winZone = new WinZone();
        winZone.create(world);

        claw = new Claw();
        claw.create();

        createToys();
    }

    private void createToys() {
        toys.add(new Toy(world, 5.1f, 1.12f, "pig.png", 0.18f, 0.10f, 0.18f));
        toys.add(new Toy(world, 6.05f, 1.12f, "cow.png", 0.32f, 0.16f, 0.24f));
        toys.add(new Toy(world, 7.2f, 1.12f, "heart.png", 0.42f, 0.22f, 0.30f));
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        claw.update(delta, toys, trayToys, winZone);

        world.step(1 / 60f, 6, 2);

        for (Toy toy : toys) {
            toy.update(delta, winZone);
        }
        for (Toy toy : trayToys) {
            toy.update(delta, winZone);
        }
    }

    private void draw() {
        ScreenUtils.clear(0.03f, 0.03f, 0.04f, 1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        floor.render(batch);
        winZone.render(batch);

        for (Toy toy : toys) {
            toy.render(batch);
        }

        for (Toy toy : trayToys) {
            toy.render(batch);
        }

        claw.render(batch);

        batch.end();
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
        batch.dispose();
        world.dispose();
    }
}
