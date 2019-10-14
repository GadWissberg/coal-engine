package com.gadarts.engine.components.model.instance;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.gadarts.engine.components.camera.CameraEventsSubscriber;
import com.gadarts.engine.components.model.ModelDefinition;
import com.gadarts.engine.components.position.PositionEventsSubscriber;

public class ModelInstanceComponent implements PositionEventsSubscriber, CameraEventsSubscriber, Component, Pool.Poolable {
    private static Vector3 auxVector = new Vector3();
    private ModelInstance modelInstance;
    private boolean billboard;
    private ModelDefinition modelDef;

    public void setModelInstance(ModelInstance modelInstance, ModelDefinition modelDefinition) {
        this.modelInstance = modelInstance;
        this.modelDef = modelDefinition;
    }

    public void rotate(int i) {
        modelInstance.transform.rotate(Vector3.Z, i);
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    @Override
    public void reset() {
        ModelInstancePool.free(modelInstance, modelDef.getName());
        modelInstance = null;
        modelDef = null;
        billboard = false;
    }

    @Override
    public void onPositionChanged(float x, float y, float z, float delta) {
        Vector3 translation = auxVector;
        Matrix4 transform = modelInstance.transform;
        transform.getTranslation(translation);
        transform.trn(x - translation.x, y - translation.y, z - translation.z);
    }

    @Override
    public void onCollisionWithNonPassableLine() {

    }

    @Override
    public void onLanding(float altitudeDelta) {

    }

    @Override
    public void onCeilingCollision() {

    }

    public void setBillboard(boolean b) {
        billboard = b;
    }

    @Override
    public void onCameraPositionChange(float x, float y, float z) {

    }
}
