package com.gadarts.engine.modelers;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.elements.Sector;
import com.gadarts.engine.utils.Utils;
import elements.LevelElementDataStructure;
import elements.VertexElement;
import elements.texture.TextureDefinition;
import elements.texture.WallTextureDefinition;
import utils.SharedC;

public class WallModeler extends LevelModeler {
    private final Vector2 aux;
    private final LevelElementDataStructure sectors;
    private final TextureAtlas surfaceTextures;
    Vector3 corner00, corner10, corner11, corner01, normal;


    public WallModeler(LevelElementDataStructure sectors, TextureAtlas surfaceTextures) {
        this.sectors = sectors;
        this.surfaceTextures = surfaceTextures;
        corner00 = new Vector3();
        corner10 = new Vector3();
        corner11 = new Vector3();
        corner01 = new Vector3();
        normal = new Vector3();
        aux = new Vector2();
    }

    public void modelWall(ModelBuilder modelBuilder, Line line) {
        initializeVectorsForModellingWall(line);
        modelFrontSide(modelBuilder, line);
        modelBackSide(modelBuilder, line);
    }

    private void modelBackSide(ModelBuilder modelBuilder, Line line) {
        Sector fSector = (Sector) sectors.get(line.getFrontSectorId());
        Sector bSector = (Sector) sectors.get(line.getBackSectorId());
        if (bSector == null) return;
        WallTextureDefinition backTexture = line.getBackTexture();
        TextureDefinition backTop = backTexture.getTop();
        String backTopName = backTop.getName();
        if (backTopName != null && !backTopName.isEmpty()) {
            TextureRegion textureRegion = new TextureRegion(surfaceTextures.findRegion(backTopName));
            MeshPartBuilder backTopMeshBuilder = createMeshBuilder(modelBuilder, backTop, textureRegion);
            modelBackSideTop(backTopMeshBuilder, bSector, fSector, line, textureRegion);
        }
        TextureDefinition backMiddle = backTexture.getMiddle();
        String backMiddleName = backMiddle.getName();
        if (backMiddleName != null && !backMiddleName.isEmpty()) {
            TextureRegion textureRegion = new TextureRegion(surfaceTextures.findRegion(backMiddleName));
            MeshPartBuilder backMiddleMeshBuilder = createMeshBuilder(modelBuilder, backMiddle, textureRegion);
            modelBackSideMiddle(backMiddleMeshBuilder, line, fSector, bSector, textureRegion);
        }
        TextureDefinition backBottom = backTexture.getBottom();
        String backBottomName = backBottom.getName();
        if (backBottomName != null && !backBottomName.isEmpty()) {
            TextureRegion textureRegion = new TextureRegion(surfaceTextures.findRegion(backBottomName));
            MeshPartBuilder backBottomMeshBuilder = createMeshBuilder(modelBuilder, backBottom, textureRegion);
            modelBackSideBottom(backBottomMeshBuilder, bSector, fSector, line, textureRegion);
        }
    }

    private void modelFrontSide(ModelBuilder modelBuilder, Line line) {
        Sector fSector = (Sector) sectors.get(line.getFrontSectorId());
        Sector bSector = (Sector) sectors.get(line.getBackSectorId());
        WallTextureDefinition frontTexture = line.getFrontTexture();
        TextureDefinition frontTop = frontTexture.getTop();
        String name = frontTop.getName();
        if (bSector != null && name != null && !name.isEmpty()) {
            TextureRegion frontTopTextureObject = new TextureRegion(surfaceTextures.findRegion(frontTop.getName()));
            MeshPartBuilder frontTopMeshBuilder = createMeshBuilder(modelBuilder, frontTop, frontTopTextureObject);
            modelFrontSideTop(frontTopMeshBuilder, bSector, fSector, line, frontTopTextureObject);
        }
        TextureDefinition frontMiddle = frontTexture.getMiddle();
        String frontMiddleName = frontMiddle.getName();
        if (frontMiddleName != null && !frontMiddleName.isEmpty()) {
            TextureRegion frontMiddleTextureObject = new TextureRegion(surfaceTextures.findRegion(frontMiddle.getName()));
            MeshBuilder frontMiddleMeshBuilder = createMeshBuilder(modelBuilder, frontMiddle, frontMiddleTextureObject);
            modelFrontSideMiddle(frontMiddleMeshBuilder, line, fSector, bSector, frontMiddleTextureObject);
        }
        TextureDefinition frontBottom = frontTexture.getBottom();
        String bottomName = frontBottom.getName();
        if (bSector != null && bottomName != null && !bottomName.isEmpty()) {
            TextureRegion frontBottomTextureObject = new TextureRegion(surfaceTextures.findRegion(frontBottom.getName()));
            MeshPartBuilder frontBottomMeshBuilder = createMeshBuilder(modelBuilder, frontBottom, frontBottomTextureObject);
            modelFrontSideBottom(frontBottomMeshBuilder, bSector, fSector, line, frontBottomTextureObject);
        }
    }

    private void modelBackSideBottom(MeshPartBuilder meshBuilder, Sector backSector, Sector frontSector, Line line,
                                     TextureRegion backBottomTextureObject) {
        if (backSector.getFloorAltitude() < frontSector.getFloorAltitude()) {
            float altitude = frontSector.getFloorAltitude() - backSector.getFloorAltitude();
            setTextureCoords(meshBuilder, line, backBottomTextureObject, line.getBackTexture().getTop(), altitude);
            initializeVectorsForSide(backSector.getFloorAltitude(), frontSector.getFloorAltitude());
            normal.set(aux.set(normal.x, normal.y).rotate(180), normal.z);
            meshBuilder.rect(corner10, corner00, corner01, corner11, normal);
            addRegionAttributes(backBottomTextureObject, 4);
        }
    }

    private void modelBackSideTop(MeshPartBuilder meshBuilder, Sector backSector, Sector frontSector, Line line,
                                  TextureRegion backTopTextureObject) {
        if (frontSector.getCeilingAltitude() < backSector.getCeilingAltitude()) {
            setTextureCoords(meshBuilder, line, backTopTextureObject, line.getBackTexture().getTop(), backSector.getCeilingAltitude() - frontSector.getCeilingAltitude());
            initializeVectorsForSide(frontSector.getCeilingAltitude(), backSector.getCeilingAltitude());
            meshBuilder.rect(corner10, corner00, corner01, corner11, normal);
            addRegionAttributes(backTopTextureObject, 4);
        }
    }

    private void modelFrontSideTop(MeshPartBuilder meshBuilder, Sector backSector, Sector frontSector, Line line,
                                   TextureRegion frontTopTextureObject) {
        if (frontSector.getCeilingAltitude() > backSector.getCeilingAltitude()) {
            float altitude = frontSector.getCeilingAltitude() - backSector.getCeilingAltitude();
            setTextureCoords(meshBuilder, line, frontTopTextureObject, line.getFrontTexture().getTop(), altitude);
            initializeVectorsForSide(backSector.getCeilingAltitude(), frontSector.getCeilingAltitude());
            meshBuilder.rect(corner00, corner10, corner11, corner01, normal);
            addRegionAttributes(frontTopTextureObject, 4);
        }
    }

    private void modelFrontSideBottom(MeshPartBuilder meshBuilder, Sector backSector, Sector frontSector, Line line,
                                      TextureRegion frontBottomTextureObject) {
        if (frontSector.getFloorAltitude() < backSector.getFloorAltitude()) {
            setTextureCoords(meshBuilder, line, frontBottomTextureObject, line.getFrontTexture().getBottom(), backSector.getFloorAltitude() - frontSector.getFloorAltitude());
            initializeVectorsForSide(frontSector.getFloorAltitude(), backSector.getFloorAltitude());
            meshBuilder.rect(corner00, corner10, corner11, corner01, normal);
            addRegionAttributes(frontBottomTextureObject, 4);
        }
    }

    private void initializeVectorsForSide(float lowerAltitude, float higherAltitude) {
        corner00.set(corner00.x, corner00.y, lowerAltitude);
        corner10.set(corner10.x, corner10.y, lowerAltitude);
        corner11.set(corner11.x, corner11.y, higherAltitude);
        corner01.set(corner01.x, corner01.y, higherAltitude);
    }

    private void modelBackSideMiddle(MeshPartBuilder meshBuilder, Line line, Sector frontSector, Sector backSector,
                                     TextureRegion textureObject) {
        initializeVectorsForSide(Math.max(frontSector.getFloorAltitude(), backSector.getFloorAltitude()),
                Math.min(frontSector.getCeilingAltitude(), backSector.getCeilingAltitude()));
        TextureDefinition texture = line.getBackTexture().getMiddle();
        float altitude = frontSector.getCeilingAltitude() - frontSector.getFloorAltitude();
        setTextureCoords(meshBuilder, line, textureObject, texture, altitude);
        aux.set(normal.x, normal.y).rotate(180);
        meshBuilder.rect(corner10, corner00, corner01, corner11, normal.set(aux, 0));
        addRegionAttributes(textureObject, 4);
    }

    private void modelFrontSideMiddle(MeshBuilder meshBuilder, Line line, Sector frontSector, Sector backSector,
                                      TextureRegion textureObject) {
        float fAltitude = frontSector.getFloorAltitude();
        float cAltitude = frontSector.getCeilingAltitude();
        initializeVectorsForSide(backSector != null ? Math.max(fAltitude, backSector.getFloorAltitude()) : fAltitude,
                backSector != null ? Math.min(cAltitude, backSector.getCeilingAltitude()) : cAltitude);
        TextureDefinition texture = line.getFrontTexture().getMiddle();
        float altitude = corner11.z - corner00.z;
        setTextureCoords(meshBuilder, line, textureObject, texture, altitude);
        meshBuilder.rect(corner00, corner10, corner11, corner01, normal);
        addRegionAttributes(textureObject, 4);
    }

    private void setTextureCoords(MeshPartBuilder meshBuilder, Line line, TextureRegion textureObject,
                                  TextureDefinition texture, float altitude) {
        float hor = texture.getHorizontalOffset();
        float ver = texture.getVerticalOffset();
        float u2 = hor + line.getLength() * SharedC.WORLD_UNIT / textureObject.getRegionWidth();
        float v2 = ver + altitude * SharedC.WORLD_UNIT / textureObject.getRegionHeight();
        meshBuilder.setUVRange(hor, ver, u2, v2);
    }

    private void initializeVectorsForModellingWall(Line line) {
        Sector sector = (Sector) sectors.get(line.getFrontSectorId());
        VertexElement rightVertex = Utils.determineRightVertexOfLine(line);
        VertexElement leftVertex = rightVertex == line.getSrc() ? line.getDst() : line.getSrc();
        corner00.set(rightVertex.getX(), rightVertex.getY(), sector.getFloorAltitude());
        corner10.set(leftVertex.getX(), leftVertex.getY(), sector.getFloorAltitude());
        corner11.set(leftVertex.getX(), leftVertex.getY(), sector.getCeilingAltitude());
        corner01.set(rightVertex.getX(), rightVertex.getY(), sector.getCeilingAltitude());
        initializeNormalVector(line);
    }

    private void initializeNormalVector(Line line) {
        aux.set(1, 0);
        aux.setAngle(line.getNormalDirection());
        normal.set(aux, 0);
    }

}
