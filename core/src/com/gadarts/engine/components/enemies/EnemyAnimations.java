package com.gadarts.engine.components.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class EnemyAnimations {
    private HashMap<EnemyComponent.FrameType, HashMap<EnemyComponent.FacingDirection, EnemyAnimation<TextureRegion>>> framesByType = new HashMap<EnemyComponent.FrameType, HashMap<EnemyComponent.FacingDirection, EnemyAnimation<TextureRegion>>>();

    public boolean containsAnimationsOfType(EnemyComponent.FrameType frameType, String frameDirection) {
        if (!framesByType.containsKey(frameType)) return false;
        return framesByType.get(frameType).containsKey(frameDirection);
    }

    public void putFramesByTypeAndDirection(EnemyComponent.FrameType frameType, EnemyAnimation<TextureRegion> animation) {
        putFramesByTypeAndDirection(frameType, EnemyComponent.FacingDirection.FRONT, animation);
    }

    public void putFramesByTypeAndDirection(EnemyComponent.FrameType frameType,
                                            EnemyComponent.FacingDirection frameDirection,
                                            EnemyAnimation<TextureRegion> animation) {
        if (!framesByType.containsKey(frameType)) {
            framesByType.put(frameType, new HashMap<EnemyComponent.FacingDirection, EnemyAnimation<TextureRegion>>());
        }
        framesByType.get(frameType).put(frameDirection, animation);
    }

    public EnemyAnimation<TextureRegion> getFramesByTypeAndDirection(EnemyComponent.FrameType type,
                                                                     EnemyComponent.FacingDirection facingName) {
        if (!framesByType.containsKey(type)) return null;
        return framesByType.get(type).get(facingName);
    }
}
