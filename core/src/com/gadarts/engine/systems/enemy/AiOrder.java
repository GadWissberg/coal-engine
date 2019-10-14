package com.gadarts.engine.systems.enemy;

import com.badlogic.gdx.utils.Pool;
import com.gadarts.engine.elements.Line;

public class AiOrder implements Pool.Poolable {
    private float direction;
    private int duration;
    private long began;
    private Line collidedWall;

    @Override
    public void reset() {
        duration = 0;
        direction = 0;
        began = 0;
    }

    public void init(float direction, int duration, Line wall) {
        this.direction = direction;
        this.duration = duration;
        this.collidedWall = wall;
    }

    public float getDirection() {
        return direction;
    }

    public void setBeganTime(long millis) {
        began = millis;
    }

    public long getBeganTime() {
        return began;
    }

    public long getDuration() {
        return duration;
    }

    public Line getCollidedWall() {
        return collidedWall;
    }
}
