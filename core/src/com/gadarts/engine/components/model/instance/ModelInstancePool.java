package com.gadarts.engine.components.model.instance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Queue;
import com.gadarts.engine.components.model.ModelDefinition;

import java.util.HashMap;

public class ModelInstancePool {
    private static HashMap<String, Queue<ModelInstance>> modelInstances = new HashMap<String, Queue<ModelInstance>>();

    public static ModelInstance obtain(ModelDefinition def) {
        ModelInstance result;
        String name = def.getName();
        if (modelInstances.containsKey(name)) {
            Queue<ModelInstance> modelInstances = ModelInstancePool.modelInstances.get(name);
            if (modelInstances.isEmpty()) {
                result = createNewInstance(def, name);
            } else {
                result = modelInstances.removeFirst();
                Gdx.app.debug("ModelInstancePool", "Obtained a new model instance:" + name + ", left free:" + modelInstances.size);
            }
        } else {
            Queue<ModelInstance> modelInstances = new Queue<ModelInstance>();
            ModelInstancePool.modelInstances.put(name, modelInstances);
            result = createNewInstance(def, name);
        }
        return result;
    }

    private static ModelInstance createNewInstance(ModelDefinition def, String name) {
        ModelInstance result;
        Gdx.app.debug("ModelInstancePool", "Creating a new model instance:" + name);
        result = new ModelInstance(def.getModel());
        return result;
    }

    public static void free(ModelInstance modelInstance, String name) {
        if (modelInstances.containsKey(name)) {
            Queue<ModelInstance> modelInstances = ModelInstancePool.modelInstances.get(name);
            modelInstances.addFirst(modelInstance);
            Gdx.app.debug("ModelInstancePool", "Freed a new model instance:" + name + ", left free:" + modelInstances.size);
        }
    }
}
