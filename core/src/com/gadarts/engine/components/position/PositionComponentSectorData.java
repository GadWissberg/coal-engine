package com.gadarts.engine.components.position;

public class PositionComponentSectorData {
    private float currentFloorAltitude;
    private float currentCeilingAltitude;
    private long currentSectorId;

    public void clear() {
        currentCeilingAltitude = 0;
        currentCeilingAltitude = 0;
        currentSectorId = 0;
    }

    public void setCurrentSectorId(long currentSectorId) {
        this.currentSectorId = currentSectorId;
    }

    public long getCurrentSectorId() {
        return currentSectorId;
    }

    public float getCurrentFloorAltitude() {
        return currentFloorAltitude;
    }

    public float getCurrentCeilingAltitude() {
        return currentCeilingAltitude;
    }

    public void setCurrentCeilingAltitude(float currentCeilingAltitude) {
        this.currentCeilingAltitude = currentCeilingAltitude;
    }

    public void setCurrentFloorAltitude(float v) {
        this.currentFloorAltitude = v;
    }
}
