package com.gadarts.engine.components.pickups;

import com.gadarts.engine.components.BaseDefinitionComponent;
import com.gadarts.engine.components.model.ModelDefinition;
import com.gadarts.engine.properties.PickupDefinitionProperties;

public class PickupDefinitionComponent extends BaseDefinitionComponent {
    private ModelDefinition modelDefinition;
    private Runnable onPickup;
    private float radius;
    private float bodyAltitude;


    public ModelDefinition getModelDefinition() {
        return modelDefinition;
    }

    public void setModelDefinition(ModelDefinition modelDefinition) {
        this.modelDefinition = modelDefinition;
    }

    public void setOnPickupEvent(Runnable onPickup) {
        this.onPickup = onPickup;
    }

    public Runnable getOnPickupEvent() {
        return onPickup;
    }

    public void setBodyAltitude(float bodyAltitude) {
        this.bodyAltitude = bodyAltitude;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public float getBodyAltitude() {
        return bodyAltitude;
    }

    public void applyProperties(PickupDefinitionProperties properties) {
        setName(properties.getName());
        setModelDefinition(properties.getModelDefinition());
        setBodyAltitude(properties.getBodyAltitude());
        setOnPickupEvent(properties.getOnPickup());
        setRadius(properties.getRadius());
    }
}
