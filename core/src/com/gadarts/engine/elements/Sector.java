package com.gadarts.engine.elements;


import elements.SectorElement;
import elements.texture.TextureDefinition;

import java.util.ArrayList;

public class Sector extends NonActorElement implements SectorElement {
    private final ArrayList<SubSector> subSectors;
    private float floorAltitude;
    private float ceilAltitude;
    private TextureDefinition floorTexture;
    private TextureDefinition ceilingTexture;

    public Sector(float floorAltitude, float ceilAltitude, ArrayList<SubSector> subSectors) {
        this.floorAltitude = floorAltitude;
        this.ceilAltitude = ceilAltitude;
        this.subSectors = subSectors;
    }


    @Override
    public void setCeilingAltitude(float i) {
        ceilAltitude = i;
    }

    @Override
    public float getCeilingAltitude() {
        return ceilAltitude;
    }

    @Override
    public void setFloorAltitude(float i) {
        floorAltitude = i;
    }


    @Override
    public float getFloorAltitude() {
        return floorAltitude;
    }

    @Override
    public TextureDefinition getFloorTexture() {
        return floorTexture;
    }

    @Override
    public void setFloorTexture(TextureDefinition s) {
        floorTexture = s;
    }

    @Override
    public TextureDefinition getCeilingTexture() {
        return ceilingTexture;
    }

    @Override
    public void setCeilingTexture(TextureDefinition s) {
        ceilingTexture = s;
    }

    public ArrayList<SubSector> getSubSectors() {
        return subSectors;
    }

}
