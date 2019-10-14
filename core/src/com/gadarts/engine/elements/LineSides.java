package com.gadarts.engine.elements;

import elements.texture.WallTextureDefinition;

public class LineSides {
    long backSectorId;
    long frontSectorId;
    WallTextureDefinition frontTexture;
    WallTextureDefinition backTexture;

    public LineSides() {
    }

    public long getFrontSectorId() {
        return frontSectorId;
    }

    public long getBackSectorId() {
        return backSectorId;
    }

    public void setFrontSectorId(long l) {
        frontSectorId = l;
    }

    public void setBackSectorId(long l) {
        backSectorId = l;
    }

    public WallTextureDefinition getFrontTexture() {
        return frontTexture;
    }

    public void setFrontTexture(WallTextureDefinition s) {
        frontTexture = s;
    }

    public WallTextureDefinition getBackTexture() {
        return backTexture;
    }

    public void setBackTexture(WallTextureDefinition s) {
        backTexture = s;
    }
}