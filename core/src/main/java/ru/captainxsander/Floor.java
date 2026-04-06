package ru.captainxsander;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Floor {
    private Body floor;

    public void createFloor(World world) {
        BodyDef floorDef = new BodyDef();
        floorDef.type = BodyDef.BodyType.StaticBody;
        floorDef.position.set(0, 1); // Y=1, чуть выше нуля

        floor = world.createBody(floorDef);

        PolygonShape floorShape = new PolygonShape();
        floorShape.setAsBox(10, 0.5f); // ширина 20 юнитов, высота 1

        floor.createFixture(floorShape, 0);
        floorShape.dispose();
    }
}
