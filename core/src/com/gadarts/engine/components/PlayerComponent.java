package com.gadarts.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PlayerComponent implements Component, Pool.Poolable {
    private float maxStepAltitude;
    private float movementAltitudeSpeed;
    private float maxMovementAltitudeSpeed;
    private float jumpSpeed;
    private boolean jumping;

    public float getMaxStepAltitude() {
        return maxStepAltitude;
    }

    public void setMaxMovementAltitudeSpeed(float maxMovementAltitudeSpeed) {
        this.maxMovementAltitudeSpeed = maxMovementAltitudeSpeed;
    }

    public float getMaxMovementAltitudeSpeed() {
        return maxMovementAltitudeSpeed;
    }

    public void setMovementAltitudeSpeed(float movementAltitudeSpeed) {
        this.movementAltitudeSpeed = movementAltitudeSpeed;
    }

    public float getMovementAltitudeSpeed() {
        return movementAltitudeSpeed;
    }

    @Override
    public void reset() {
        maxStepAltitude = 0;
    }

    public void setMaxStepAltitude(float v) {
        maxStepAltitude = v;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }

    public void setJumpSpeed(float jumpSpeed) {
        this.jumpSpeed = jumpSpeed;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public boolean isJumping() {
        return jumping;
    }
}