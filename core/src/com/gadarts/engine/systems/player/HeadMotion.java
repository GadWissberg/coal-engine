package com.gadarts.engine.systems.player;

public class HeadMotion {
    private boolean inMotion;
    private float speed;
    private float relativeTargetZ;
    private boolean addReverse;
    private boolean inReverse;
    private float tiltDelta;
    private boolean tilts;

    public void setInMotion(boolean b) {
        inMotion = b;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setRelativeTargetZ(float relativeTargetZ) {
        this.relativeTargetZ = relativeTargetZ;
    }

    public void setAddReverse(boolean addReverse) {
        this.addReverse = addReverse;
    }

    public boolean isInMotion() {
        return inMotion;
    }

    public float getSpeed() {
        return speed;
    }

    public float getRelativeTargetZ() {
        return relativeTargetZ;
    }

    public boolean shouldAddReverse() {
        return addReverse;
    }

    public boolean isInReverse() {
        return inReverse;
    }

    public void setInReverseState(boolean b) {
        inReverse = b;
    }

    public void tilt(float degreesDelta) {
        tilts = true;
        tiltDelta = degreesDelta;
    }

    public float getTiltDelta() {
        return tiltDelta;
    }

    public void stopTilt() {
        tilts = false;
    }

    public boolean isTilting() {
        return tilts;
    }
}
