package com.gadarts.engine.components.bullets;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class BulletComponent implements Component, Pool.Poolable {

    private BulletDefinitionComponent definitionComponent;

    @Override
    public void reset() {
        definitionComponent = null;
    }

    public BulletDefinitionComponent getDefinitionComponent() {
        return definitionComponent;
    }

    public void setDefinitionComponent(BulletDefinitionComponent pickupDefinitionEntity) {
        this.definitionComponent = pickupDefinitionEntity;
    }


}
