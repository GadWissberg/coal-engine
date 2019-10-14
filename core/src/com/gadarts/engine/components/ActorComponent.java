package com.gadarts.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import elements.actor.Type;

public class ActorComponent implements Component, Pool.Poolable {
    private Type type;
    private Long id = -1l;

    @Override
    public void reset() {

    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
