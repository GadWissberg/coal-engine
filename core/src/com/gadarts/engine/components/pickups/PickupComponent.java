package com.gadarts.engine.components.pickups;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PickupComponent implements Component, Pool.Poolable {
    private PickupDefinitionComponent definitionComponent;

    public PickupDefinitionComponent getDefinitionComponent() {
        return definitionComponent;
    }


    @Override
    public void reset() {

    }

    public void setDefinitionComponent(PickupDefinitionComponent pickupDefinitionEntity) {
        this.definitionComponent = pickupDefinitionEntity;
    }

}
