package com.gadarts.engine.components.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

public class EnemyAnimation<T> extends Animation<T> {
    private final EnemyComponent.FrameType frameType;

    public EnemyAnimation(float frameDuration, Array<? extends T> frames, PlayMode normal,
                          EnemyComponent.FrameType frameType) {
        super(frameDuration, frames, normal);
        this.frameType = frameType;
    }

    public EnemyComponent.FrameType getFrameType() {
        return frameType;
    }
}
