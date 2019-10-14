package com.gadarts.engine.systems.velocity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.enemies.DirectionChangeSubscriber;

import java.util.ArrayList;

public class MovementForce implements DirectionChangeSubscriber {
    private Vector3 direction = new Vector3();
    private boolean accelerateOnGroundOnly;
    private String name;
    private float speed;
    private float acceleration;
    private float maxSpeed;
    private float minSpeed;
    private boolean enabled;
    private boolean affectingZ;
    private ArrayList<ForceEventSubscriber> forceEventsSubscribers;
    private Vector2 auxVector = new Vector2(1, 0);

    public void init(float x, float y, float z) {
        init(x, y, z, -Float.MAX_VALUE, Float.MAX_VALUE, false);
    }

    public void init(float x, float y, float z, float minSpeed, float maxSpeed, boolean enabled) {
        this.direction.set(x, y, z);
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.enabled = enabled;
    }

    public void reset() {
        acceleration = 0;
        speed = 0;
        maxSpeed = Float.MAX_VALUE;
        minSpeed = -Float.MAX_VALUE;
        direction.setZero();
        accelerateOnGroundOnly = false;
        name = null;
        enabled = false;
        affectingZ = false;
        if (forceEventsSubscribers != null) forceEventsSubscribers.clear();
    }

    public boolean isAcceleratingOnGroundOnly() {
        return accelerateOnGroundOnly;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public float getSpeed() {
        return speed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        if (enabled == v) return;
        enabled = v;
        statusChanged();
    }

    private void statusChanged() {
        if (forceEventsSubscribers == null) return;
        for (ForceEventSubscriber subscriber : forceEventsSubscribers) {
            subscriber.onForceStateChange(enabled);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDirection(Vector3 direction) {
        this.direction.set(direction);
    }

    public void setDirection(float x, float y, float z) {
        this.direction.set(x, y, z);
    }

    public void setAffectingZ(boolean b) {
        affectingZ = b;
    }

    public void subscribeForForceEvents(ForceEventSubscriber subscriber) {
        if (forceEventsSubscribers == null) {
            forceEventsSubscribers = new ArrayList<ForceEventSubscriber>();
        }
        if (!forceEventsSubscribers.contains(subscriber)) {
            forceEventsSubscribers.add(subscriber);
        }
    }

    public void setAccelerateOnGroundOnly(boolean b) {
        accelerateOnGroundOnly = b;
    }

    @Override
    public void onDirectionChange(float direction) {
        auxVector.setAngle(direction);
        this.direction.set(auxVector, this.direction.z);
    }

}
