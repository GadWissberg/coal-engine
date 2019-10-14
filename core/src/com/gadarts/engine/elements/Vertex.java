package com.gadarts.engine.elements;


import elements.VertexElement;

public class Vertex extends NonActorElement implements VertexElement {

    public Vertex(float x, float y) {
        setX(x);
        setY(y);
    }


    @Override
    public boolean equals(VertexElement vertexElement) {
        return false;
    }


    @Override
    public float getWidth() {
        return 1;
    }

    @Override
    public float getHeight() {
        return 1;
    }

}
