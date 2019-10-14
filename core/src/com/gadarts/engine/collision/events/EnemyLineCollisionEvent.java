package com.gadarts.engine.collision.events;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.systems.enemy.AiOrder;

import static com.gadarts.engine.collision.events.BlockCollisionEvent.auxVector2_1;

public class EnemyLineCollisionEvent implements BlockingLineCollisionEvent {

    @Override
    public void run(Line other, com.badlogic.ashley.core.Entity entity) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        boolean orderInProcess = ec.isOrderInProcess();
        if (!orderInProcess || ec.getCurrentOrder().getCollidedWall().getId() == other.getId()) {
            AiOrder order = Pools.get(AiOrder.class).obtain();
            int duration = MathUtils.random(1000, 2000);
            float angle = auxVector2_1.set(1, 0).setAngle(ec.getDirection() + (MathUtils.randomBoolean() ? 1 : -1) + 90).angle();
            order.init(angle, duration, other);
            ec.forceOrder(order);
        }
    }
}
