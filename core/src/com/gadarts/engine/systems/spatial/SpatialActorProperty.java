package com.gadarts.engine.systems.spatial;

public class SpatialActorProperty {
    private int bottomLeftId;
    private int bottomRightId;
    private int topRightId;
    private int topLeftId;

    private int centerId;

    public void clear() {
        bottomLeftId = 0;
        bottomRightId = 0;
        topRightId = 0;
        topLeftId = 0;
    }

    public int getBottomRightId() {
        return bottomRightId;
    }

    public int getTopRightId() {
        return topRightId;
    }

    public int getTopLeftId() {
        return topLeftId;
    }

    public int getBottomLeftId() {
        return bottomLeftId;
    }

    public void setBottomLeftId(int bottomLeftId) {
        this.bottomLeftId = bottomLeftId;
    }

    public void setBottomRightId(int bottomRightId) {
        this.bottomRightId = bottomRightId;
    }

    public void setTopRightId(int topRightId) {
        this.topRightId = topRightId;
    }

    public void setTopLeftId(int topLeftId) {
        this.topLeftId = topLeftId;
    }

    public void setCenterId(int centerId) {
        this.centerId = centerId;
    }

    public int getCenterId() {
        return centerId;
    }
}
