package com.gadarts.engine.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.components.pickups.PickupComponent;

public class PickupSystem extends EntitySystem {
    private static Color pickupColor;
    private static float radius;

    private ImmutableArray<Entity> entities;

    public static float getRadius() {
        return radius;
    }

    public static void setPickupColor(Color pickupColor) {
        PickupSystem.pickupColor = pickupColor;
    }

    public static Color getPickupColor() {
        return pickupColor;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        entities = engine.getEntitiesFor(Family.all(PickupComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : entities) {
            ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
            modelInstanceComponent.rotate(1);
        }
    }

    public static void setRadius(float radius) {
        PickupSystem.radius = radius;
    }
}
