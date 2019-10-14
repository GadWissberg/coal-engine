package com.gadarts.engine;

import com.badlogic.gdx.graphics.Texture;

public class SkyBoxTextures {
    private Texture top, east, north, west, south;

    public void setTop(Texture texture) {
        top = texture;
    }

    public void setEast(Texture east) {
        this.east = east;
    }

    public void setNorth(Texture north) {
        this.north = north;
    }

    public void setWest(Texture west) {
        this.west = west;
    }

    public void setSouth(Texture south) {
        this.south = south;
    }

    public Texture getTop() {
        return top;
    }

    public Texture getEast() {
        return east;
    }

    public Texture getNorth() {
        return north;
    }

    public Texture getWest() {
        return west;
    }

    public Texture getSouth() {
        return south;
    }
}
