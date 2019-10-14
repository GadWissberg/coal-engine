package com.gadarts.engine.components.model;

import com.badlogic.gdx.graphics.g3d.Model;

public class ModelDefinition {
    private Model model;
    private String name;

    public ModelDefinition(Model model, String name) {
        this.model = model;
        this.name = name;
    }

    public Model getModel() {
        return model;
    }

    public String getName() {
        return name;
    }
}
