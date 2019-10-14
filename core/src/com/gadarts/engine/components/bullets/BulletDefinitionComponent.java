package com.gadarts.engine.components.bullets;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.engine.components.BaseDefinitionComponent;
import com.gadarts.engine.components.model.ModelDefinition;

public class BulletDefinitionComponent extends BaseDefinitionComponent {
    private ModelDefinition modelDefinition;
    private float speed;
    private BoundingBox boundingBox;
    private float radius;
    private Model bulletHoleModel;

    public Model getBulletHoleModel() {
        return bulletHoleModel;
    }

    public void setBulletHoleModel(Model bulletHoleModel) {
        this.bulletHoleModel = bulletHoleModel;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public ModelDefinition getModelDefinition() {
        return modelDefinition;
    }

    public void setModelDefinition(ModelDefinition modelDefinition) {
        this.modelDefinition = modelDefinition;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void calculateBoundingBox() {
        this.boundingBox = new BoundingBox();
        modelDefinition.getModel().calculateBoundingBox(boundingBox);
    }
}
