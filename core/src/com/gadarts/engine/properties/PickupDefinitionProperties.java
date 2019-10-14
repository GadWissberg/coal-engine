package com.gadarts.engine.properties;

import com.gadarts.engine.components.model.ModelDefinition;

public class PickupDefinitionProperties {
    private String name;
    private float radius;
    private ModelDefinition modelDefinition;
    private Runnable onPickup;
    private float bodyAltitude;

    public void setBodyAltitude(float bodyAltitude) {
        this.bodyAltitude = bodyAltitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setModelDefinition(ModelDefinition modelDefinition) {
        this.modelDefinition = modelDefinition;
    }

    public void defineOnPickUp(Runnable onPickup) {
        this.onPickup = onPickup;
    }

    public String getName() {
        return name;
    }

    public float getRadius() {
        return radius;
    }

    public ModelDefinition getModelDefinition() {
        return modelDefinition;
    }

    public Runnable getOnPickup() {
        return onPickup;
    }

    public float getBodyAltitude() {
        return bodyAltitude;
    }
}
