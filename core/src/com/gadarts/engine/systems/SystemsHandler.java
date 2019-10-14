package com.gadarts.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.gadarts.engine.Level;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.PlayerComponent;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.systems.player.PlayerSystem;
import com.gadarts.engine.systems.render.RenderSystem;
import com.gadarts.engine.systems.velocity.VelocitySystem;

public class SystemsHandler {
    private PooledEngine engine;
    private RenderSystem renderSystem;

    private void createBulletSystem() {
        BulletSystem bulletSystem = new BulletSystem();
        engine.addSystem(bulletSystem);
    }

    private void createEffectsSystem() {
        EffectsSystem effectsSystem = new EffectsSystem();
        engine.addSystem(effectsSystem);
    }

    private void createPickupSystem() {
        PickupSystem pickupSystem = new PickupSystem();
        engine.addSystem(pickupSystem);
    }

    private void createVelocitySystem() {
        VelocitySystem velocitySystem = new VelocitySystem();
        engine.addSystem(velocitySystem);
    }

    private void createPlayerSystem() {
        PlayerSystem playerSystem = new PlayerSystem();
        engine.addSystem(playerSystem);
    }

    private void createRenderSystem() {
        renderSystem = new RenderSystem();
        engine.addSystem(renderSystem);
    }

    public void createSystems(PooledEngine engine) {
        this.engine = engine;
        createEffectsSystem();
        createRenderSystem();
        createPlayerSystem();
        createVelocitySystem();
        createPickupSystem();
        createBulletSystem();
        engine.addSystem(new EnemySystem());
    }

    public void initializeSystems(Level level) {
        initializeEnemySystem(engine, level);
        Entity playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
        PlayerSystem playerSystem = initializePlayerSystem(engine);
        ComponentsMapper.position.get(playerEntity).subscribeForPositionEvents(playerSystem);
        RenderSystem renderSystem = engine.getSystem(RenderSystem.class);
        renderSystem.init(level, engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get()));
        VelocitySystem velocitySystem = engine.getSystem(VelocitySystem.class);
        velocitySystem.init(level);
    }

    private PlayerSystem initializePlayerSystem(PooledEngine engine) {
        PlayerSystem playerSystem = engine.getSystem(PlayerSystem.class);
        playerSystem.setPlayer(engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
        return playerSystem;
    }

    private void initializeEnemySystem(PooledEngine engine, Level level) {
        EnemySystem enemySystem = engine.getSystem(EnemySystem.class);
        enemySystem.init(level, engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
    }

    public RenderSystem getRenderSystem() {
        return renderSystem;
    }
}
