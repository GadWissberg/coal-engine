package com.gadarts.engine.collision.events;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.components.position.PositionComponent;

public class BlockCollisionEvent implements ActorCollisionEvent {
    protected static Vector2 auxVector2_1 = new Vector2();
    protected static Vector2 auxVector2_2 = new Vector2();

    /**
     * Performs movement block by modifying the next position.
     *
     * @param movingEntity       The entity which get blocked.
     * @param blockingEntity     The entity which blocks.
     * @param nextPositionVector The planned next position. This will be modified if a collision occurs.
     * @return Whether a block has occurred or not.
     */
    @Override
    public boolean run(Entity movingEntity, Entity blockingEntity, Vector3 nextPositionVector) {
        if (ComponentsMapper.enemy.get(blockingEntity).getStatus().equals(EnemyComponent.Status.DEAD))
            return false;
        performBlock(movingEntity, blockingEntity, nextPositionVector);
        return true;
    }

    private void performBlock(Entity entity, Entity other, Vector3 nextPositionVector) {
        PositionComponent positionComponent = ComponentsMapper.position.get(entity);
        PositionComponent otherPc = ComponentsMapper.position.get(other);
        auxVector2_1.set(positionComponent.getX(), positionComponent.getY());
        Vector2 dirOtherToThis = auxVector2_1.sub(otherPc.getX(), otherPc.getY()).nor();
        auxVector2_2.set(positionComponent.getX(), positionComponent.getY());
        float distance = auxVector2_2.dst(otherPc.getX(), otherPc.getY());
        float stepSize = positionComponent.getRadius() + otherPc.getRadius() - distance;
        nextPositionVector.add(dirOtherToThis.x * stepSize, dirOtherToThis.y * stepSize, 0);
    }

}
