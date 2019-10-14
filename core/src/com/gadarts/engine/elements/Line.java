package com.gadarts.engine.elements;

import elements.LineElement;
import elements.VertexElement;
import elements.texture.WallTextureDefinition;
import utils.SharedUtils;

public class Line extends NonActorElement implements LineElement {

    private final Vertex src;
    private final Vertex dst;
    private final LineSides LineSides = new LineSides();
    private float normalDirection;
    private boolean solid;
    private float length;

    public Line(Vertex src, Vertex dst) {
        this.src = src;
        this.dst = dst;
        double rad = Math.atan2(dst.getY() - src.getY(), dst.getX() - src.getX()) + Math.PI / 2;
        normalDirection = SharedUtils.clampAngle((float) Math.toDegrees(rad));
    }

    @Override
    public boolean equals(LineElement lineElement) {
        return false;
    }

    @Override
    public VertexElement getSrc() {
        return src;
    }

    @Override
    public VertexElement getDst() {
        return dst;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public void setSolid(boolean b) {
        solid = b;
    }

    @Override
    public long getFrontSectorId() {
        return LineSides.getFrontSectorId();
    }

    @Override
    public long getBackSectorId() {
        return LineSides.getBackSectorId();
    }

    @Override
    public void setFrontSectorId(long l) {
        LineSides.setFrontSectorId(l);
    }

    @Override
    public void setBackSectorId(long l) {
        LineSides.setBackSectorId(l);
    }

    @Override
    public float getNormalDirection() {
        return normalDirection;
    }

    public float getLength() {
        return length;
    }

    @Override
    public WallTextureDefinition getFrontTexture() {
        return LineSides.getFrontTexture();
    }

    @Override
    public void setFrontTexture(WallTextureDefinition s) {
        LineSides.setFrontTexture(s);
    }

    @Override
    public WallTextureDefinition getBackTexture() {
        return LineSides.getBackTexture();
    }

    @Override
    public void setBackTexture(WallTextureDefinition s) {
        LineSides.setBackTexture(s);
    }

    @Override
    public float getX() {
        return Math.min(src.getX(), dst.getX());
    }

    @Override
    public float getY() {
        return Math.min(src.getY(), dst.getY());
    }


    @Override
    public float getWidth() {
        float width = Math.max(src.getX(), dst.getX()) - getX();
        return Math.max(width, 1);
    }

    @Override
    public float getHeight() {
        float height = Math.max(src.getY(), dst.getY()) - getY();
        return Math.max(height, 1);
    }


    public void setLineLength(float length) {
        this.length = length;
    }
}
