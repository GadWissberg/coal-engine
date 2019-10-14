package com.gadarts.engine.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.gadarts.engine.EngineSettings;
import com.gadarts.engine.elements.Line;
import elements.VertexElement;
import elements.texture.TextureDefinition;

public final class Utils {

    public static Material materializeTexture(TextureRegion texture, TextureDefinition textureDefinitionForBlending) {
        if (texture == null) return null;
        Material material = new Material(TextureAttribute.createDiffuse(texture));
        if (textureDefinitionForBlending != null && textureDefinitionForBlending.getOpacity() != 1) {
            float opacity = textureDefinitionForBlending.getOpacity();
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, opacity));
        }
        return material;
    }

    public static VertexElement determineRightVertexOfLine(Line line) {
        VertexElement src = line.getSrc();
        VertexElement dst = line.getDst();
        float srcX = src.getX();
        float dstX = dst.getX();
        float centerX = Math.min(srcX, dstX) + Math.abs(srcX - dstX) / 2;
        float srcY = src.getY();
        float dstY = dst.getY();
        float centerY = Math.min(srcY, dstY) + Math.abs(srcY - dstY) / 2;
        float normalDirection = line.getNormalDirection();
        float testX = (float) (centerX + Math.cos(Math.toRadians(normalDirection)));
        float testY = (float) (centerY + Math.sin(Math.toRadians(normalDirection)));
        float d = (srcX - centerX) * (testY - centerY) - (srcY - centerY) * (testX - centerX);
        return d < 0 ? dst : src;
    }

    public static float calculateLineLength(Line line) {
        return (float) Math.sqrt(Math.pow((line.getSrc().getX() - line.getDst().getX()), 2)
                + Math.pow((line.getSrc().getY() - line.getDst().getY()), 2));
    }

    public static float applyDeltaOnStep(float speed, float deltaTime) {
        return EngineSettings.MULTIPLY_DELTA ? speed * deltaTime : speed * (EngineSettings.SLOW_DOWN_IF_NO_DELTA ? 0.02f : 1);
    }

}
