package com.gadarts.engine.elements;

public class SubSector {
    private final float[] points;
    private final long containerId;

    public SubSector(float[] points, long containerId) {
        this.points = points;
        this.containerId = containerId;
    }

    public long getContainerId() {
        return containerId;
    }

    public float[] getPoints() {
        return points;
    }
}
