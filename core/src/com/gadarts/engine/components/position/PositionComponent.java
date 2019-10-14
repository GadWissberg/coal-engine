package com.gadarts.engine.components.position;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;

public class PositionComponent implements Component, Pool.Poolable {
    float radius;
    Vector3 position = new Vector3();
    private PositionComponentSectorData positionComponentSectorData = new PositionComponentSectorData();
    private ArrayList<PositionEventsSubscriber> positionEventsSubscribers;
    private float bodyAltitude;

    public void setCurrentSectorId(long currentSectorId) {
        positionComponentSectorData.setCurrentSectorId(currentSectorId);
    }

    public long getCurrentSectorId() {
        return positionComponentSectorData.getCurrentSectorId();
    }

    public void setX(float x) {
        setX(x, true);
    }

    public void setX(float x, boolean informSubscribers) {
        if (position.x == x) return;
        if (informSubscribers) positionChanged(Math.abs(position.dst(x, position.y, position.z)));
        position.x = x;
    }

    public void setY(float y) {
        setY(y, true);
    }

    public void setY(float y, boolean informSubscribers) {
        if (position.y == y) return;
        positionChanged(Math.abs(position.dst(position.x, y, position.z)));
        position.y = y;
    }

    public void setZ(float z) {
        setZ(z, true);
    }

    public void setZ(float z, boolean informSubscribers) {
        if (position.z == z) return;
        positionChanged(Math.abs(position.dst(position.x, position.y, z)));
        position.z = z;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getX() {
        return position.x;
    }

    public float getZ() {
        return position.z;
    }

    public float getY() {
        return position.y;
    }

    public float getRadius() {
        return radius;
    }

    public Vector3 getPosition(Vector3 vector) {
        return vector.set(position);
    }

    public float getCurrentFloorAltitude() {
        return positionComponentSectorData.getCurrentFloorAltitude();
    }

    public float getCurrentCeilingAltitude() {
        return positionComponentSectorData.getCurrentCeilingAltitude();
    }

    public void setCurrentCeilingAltitude(float currentCeilingAltitude) {
        positionComponentSectorData.setCurrentCeilingAltitude(currentCeilingAltitude);
    }

    public void setPosition(Vector3 vector) {
        setPosition(vector.x, vector.y, vector.z);
    }

    public void setPosition(float x, float y, float z) {
        if (position.x == x && position.y == y && position.z == z) return;
        positionChanged(Math.abs(position.dst(x, y, z)));
        position.set(x, y, z);
    }

    public void positionChanged(float delta) {
        if (positionEventsSubscribers == null) return;
        for (PositionEventsSubscriber sub : positionEventsSubscribers) {
            sub.onPositionChanged(position.x, position.y, position.z, delta);
        }
    }

    public void subscribeForPositionEvents(PositionEventsSubscriber sub) {
        if (positionEventsSubscribers == null) positionEventsSubscribers = new ArrayList<PositionEventsSubscriber>();
        positionEventsSubscribers.add(sub);
        sub.onPositionChanged(position.x, position.y, position.z, 0);
    }

    public float getBodyAltitude() {
        return bodyAltitude;
    }

    public void setCurrentFloorAltitude(float v) {
        positionComponentSectorData.setCurrentFloorAltitude(v);
    }

    public void setBodyAltitude(float bodyAltitude) {
        this.bodyAltitude = bodyAltitude;
    }

    public boolean isOnGround() {
        return getZ() <= getCurrentFloorAltitude();
    }

    public void collisionWithNonPassableLine() {
        if (positionEventsSubscribers == null) return;
        for (PositionEventsSubscriber sub : positionEventsSubscribers) {
            sub.onCollisionWithNonPassableLine();
        }
    }

    public void landed(float speedOnLanding) {
        if (positionEventsSubscribers == null) return;
        for (PositionEventsSubscriber sub : positionEventsSubscribers) {
            sub.onLanding(speedOnLanding);
        }
    }

    public void ceilingCollided() {
        if (positionEventsSubscribers == null) return;
        for (PositionEventsSubscriber sub : positionEventsSubscribers) {
            sub.onCeilingCollision();
        }
    }

    @Override
    public void reset() {
        positionEventsSubscribers.clear();
    }
}
