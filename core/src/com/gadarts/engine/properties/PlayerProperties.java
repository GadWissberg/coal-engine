package com.gadarts.engine.properties;

public class PlayerProperties {
    private float maxMovementAltitudeSpeed;
    private float maxSpeed;
    private float minSpeed;
    private float maxStepAltitude;
    private float radius;
    private float bodyAltitude;
    private float jumpSpeed;
    private float raiseSpeed;
    private float fov;

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return fov;
    }

    public void setBodyAltitude(float bodyAltitude) {
        this.bodyAltitude = bodyAltitude;
    }

    public void setJumpSpeed(float jumpSpeed) {
        this.jumpSpeed = jumpSpeed;
    }

    public float getRaiseSpeed() {
        return raiseSpeed;
    }

    public float getBodyAltitude() {
        return bodyAltitude;
    }


    public void setMaxStepAltitude(float maxStepAltitude) {
        this.maxStepAltitude = maxStepAltitude;
    }

    public float getMaxStepAltitude() {
        return maxStepAltitude;
    }

    public void setRadius(float value) {
        this.radius = value;
    }


    public void setMaxMovementAltitudeSpeed(float speed) {
        this.maxMovementAltitudeSpeed = speed;
    }


    public void setMaxSpeed(float value) {
        this.maxSpeed = value;
    }

    public void setMinSpeed(float value) {
        this.minSpeed = value;
    }

    public void setRaiseSpeed(float value) {
        this.raiseSpeed = value;
    }


    public float getMaxMovementAltitudeSpeed() {
        return maxMovementAltitudeSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public float getRadius() {
        return radius;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }
}
