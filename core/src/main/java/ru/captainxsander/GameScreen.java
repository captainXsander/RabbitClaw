package ru.captainxsander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private World world;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Claw claw;
    private List<Toy> toys = new ArrayList<>();
    private Floor floor;


    @Override
    public void show() {
        world = new World(new Vector2(0, -5f), true);
        camera = new OrthographicCamera(20, 20);
        camera.position.set(0, 10, 0);

        batch = new SpriteBatch();

        createFloor();
        createToys();
        createClaw();
    }

    private void createClaw() {
        claw = new Claw();
        claw.createClaw(world);
    }

    private void createFloor() {
        floor = new Floor();
        floor.createFloor(world);
    }

    private void createToys() {
        for (int i = -3; i <= 3; i+=2) {
            Toy toy = new Toy();
            toy.createToy(world, i);
            toys.add(toy);
        }
    }

    @Override
    public void render(float delta) {
        claw.update(world, toys);

        world.step(1/60f, 6, 2);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        claw.render(batch);
        for (Toy toy : toys) {
            toy.render(batch);
        }
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        world.dispose();
        batch.dispose();
    }
}
