package com.gadarts.engine.components.position;

public interface PositionEventsSubscriber {
    void onPositionChanged(float x, float y, float z, float delta);

    void onCollisionWithNonPassableLine();

    void onLanding(float fallingSpeedOnLanding);

    void onCeilingCollision();
}
