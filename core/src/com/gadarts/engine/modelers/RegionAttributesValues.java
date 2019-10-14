package com.gadarts.engine.modelers;

import com.badlogic.gdx.math.Vector2;

public class RegionAttributesValues {
    private final Vector2 uv;
    private final Vector2 size;

    public RegionAttributesValues(Vector2 uv, Vector2 size) {
        this.uv = uv;
        this.size = size;
    }

    public Vector2 getUv() {
        return uv;
    }

    public Vector2 getSize() {
        return size;
    }
}
