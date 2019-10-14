package com.gadarts.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.gadarts.engine.collision.events.ActorCollisionEvent;
import com.gadarts.engine.collision.events.BlockingLineCollisionEvent;
import com.gadarts.engine.collision.events.SurfaceCollisionEvent;
import com.gadarts.engine.elements.Line;
import elements.actor.Type;

import java.util.HashMap;

public class CollisionsEventsComponent implements Component, Pool.Poolable {
    private HashMap<Type, ActorCollisionEvent> actorsCollisionsEvents = new HashMap<Type, ActorCollisionEvent>();
    private BlockingLineCollisionEvent blockingLineCollisionEvent;
    private SurfaceCollisionEvent surfaceCollisionEvent;

    @Override
    public void reset() {

    }

    public void executeActorCollisionEvent(Entity entity, Entity other, Vector3 nextPositionVector) {
        Type type = ComponentsMapper.actor.get(other).getType();
        if (actorsCollisionsEvents.containsKey(type)) {
            actorsCollisionsEvents.get(type).run(entity, other, nextPositionVector);
        }
    }

    public void executeBlockingLineCollisionEvent(Line line, Entity entity) {
        if (blockingLineCollisionEvent != null) {
            blockingLineCollisionEvent.run(line, entity);
        }
    }

    public void executeSurfaceCollisionEvent() {
        if (surfaceCollisionEvent != null) {
            surfaceCollisionEvent.run();
        }
    }

    public void add(Type type, ActorCollisionEvent actorCollisionEvent) {
        actorsCollisionsEvents.put(type, actorCollisionEvent);
    }

    public void defineCollisionWithBlockingLine(BlockingLineCollisionEvent blockingLineCollisionEvent) {
        this.blockingLineCollisionEvent = blockingLineCollisionEvent;
    }

    public void defineCollisionWithBlockingLine(SurfaceCollisionEvent surfaceCollisionEvent) {
        this.surfaceCollisionEvent = surfaceCollisionEvent;
    }
}
