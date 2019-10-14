package com.gadarts.engine.collision.events;

import com.badlogic.ashley.core.Entity;
import com.gadarts.engine.elements.Line;

public interface BlockingLineCollisionEvent {
    void run(Line other, Entity entity);
}
