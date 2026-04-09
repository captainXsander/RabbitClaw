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

public class V3GameScreen implements Screen {

    private World world;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private Floor floor;
    private WinZone winZone;
    private V3ClawRig claw;

    private final List<V3Toy> toys = new ArrayList<>();

    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, -9.8f), true);

        camera = new OrthographicCamera();
        viewport = new FitViewport(V3Config.WORLD_WIDTH, V3Config.WORLD_HEIGHT, camera);
        viewport.apply(true);

        batch = new SpriteBatch();

        floor = new Floor();
        floor.create(world);

        winZone = new WinZone();
        winZone.create(world);

        claw = new V3ClawRig(world);

        toys.add(new V3Toy(world, 6f, 1.1f, "pig.png"));
        toys.add(new V3Toy(world, 7f, 1.1f, "cow.png"));
        toys.add(new V3Toy(world, 6.5f, 1.6f, "heart.png"));
    }

    @Override
    public void render(float delta) {
        world.step(1/60f, 6, 2);

        claw.update(delta, toys, winZone);

        for (V3Toy t : toys) t.update(winZone);

        ScreenUtils.clear(0,0,0,1);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        floor.render(batch);
        winZone.render(batch);

        for (V3Toy t : toys) t.render(batch);

        claw.render(batch);

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
    }
}
