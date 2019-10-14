package com.gadarts.engine.modelers;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.engine.utils.C;
import com.gadarts.engine.utils.Utils;
import elements.texture.TextureDefinition;

import java.util.ArrayList;

public abstract class LevelModeler {
    ArrayList<RegionAttributesValues> regionAttributesValues = new ArrayList<RegionAttributesValues>();

    protected void addRegionAttributes(TextureRegion textureObject, int numOfVertices) {
        RegionAttributesValues attributes = createRegionAttributesValuesObject(textureObject);
        for (int i = 0; i < numOfVertices; i++) {
            regionAttributesValues.add(attributes);
        }
    }

    private RegionAttributesValues createRegionAttributesValuesObject(TextureRegion textureObject) {
        Texture texture = textureObject.getTexture();
        float u = ((float) textureObject.getRegionX()) / texture.getWidth();
        float v = ((float) textureObject.getRegionY()) / texture.getHeight();
        Vector2 uv = new Vector2(u, v);
        float regionWidth = textureObject.getRegionWidth();
        float regionHeight = textureObject.getRegionHeight();
        Vector2 size = new Vector2(regionWidth / texture.getWidth(), regionHeight / texture.getHeight());
        return new RegionAttributesValues(uv, size);
    }

    public ArrayList<RegionAttributesValues> getRegionAttributesValues() {
        return regionAttributesValues;
    }

    protected MeshBuilder createMeshBuilder(ModelBuilder modelBuilder, TextureDefinition textureDefinition,
                                            TextureRegion textureRegion) {
        VertexAttribute regionUvAttribute = new VertexAttribute(C.ShaderRelated.RegionAttributes.RegionUvAttributes.ATTRIBUTE_USAGE, 2,
                C.ShaderRelated.RegionAttributes.RegionUvAttributes.ATTRIBUTE_ALIAS);
        VertexAttribute regionSizeAttribute = new VertexAttribute(C.ShaderRelated.RegionAttributes.RegionSizeAttributes.ATTRIBUTE_USAGE, 2,
                C.ShaderRelated.RegionAttributes.RegionSizeAttributes.ATTRIBUTE_ALIAS);
        VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute
                .Normal(), VertexAttribute.TexCoords(0), regionUvAttribute, regionSizeAttribute);
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part(textureDefinition.getName(), GL20.GL_TRIANGLES,
                vertexAttributes, Utils.materializeTexture(textureRegion, textureDefinition));
        return meshBuilder;
    }
}
