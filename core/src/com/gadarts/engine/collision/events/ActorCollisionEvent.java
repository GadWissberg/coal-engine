package com.gadarts.engine.collision.events;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

/**
 * Represents an event that runs when a collision with another actor occurs.
 */
public interface ActorCollisionEvent {
    boolean run(Entity entity, Entity other, Vector3 nextPositionVector);

}
