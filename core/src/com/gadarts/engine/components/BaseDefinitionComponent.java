package com.gadarts.engine.components;

import com.badlogic.ashley.core.Component;

public abstract class BaseDefinitionComponent implements Component {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
