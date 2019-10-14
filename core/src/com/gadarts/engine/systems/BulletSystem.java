package com.gadarts.engine.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.gadarts.engine.components.bullets.BulletComponent;

public class BulletSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    public BulletSystem() {
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        entities = engine.getEntitiesFor(Family.all(BulletComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : entities) {
            updateBullet(deltaTime, entity);
        }
    }

    private void updateBullet(float deltaTime, Entity entity) {
    }


}
