package com.gadarts.engine.collision.events;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.systems.EnemySystem;

public class PlayerCollisionWithEnemyEvent extends BlockCollisionEvent {
    private final EnemySystem enemySystem;

    public PlayerCollisionWithEnemyEvent(EnemySystem enemySystem) {
        super();
        this.enemySystem = enemySystem;
    }

    @Override
    public boolean run(Entity entity, Entity other, Vector3 nextPositionVector) {
        boolean result = super.run(entity, other, nextPositionVector);
        if (result) {
            EnemyComponent enemyComponent = ComponentsMapper.enemy.get(other);
            if (enemyComponent.getStatus().equals(EnemyComponent.Status.IDLE)) {
                enemySystem.startRunningToTarget(other);
            }
        }
        return result;
    }
}
