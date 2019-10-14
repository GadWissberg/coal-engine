package com.gadarts.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.camera.CameraEventsSubscriber;
import com.gadarts.engine.systems.spatial.Spatial;
import elements.LevelElementDataStructure;

public class Level implements CameraEventsSubscriber {
    private static float gravity;
    private final String name;
    private final ModelInstance skyBox;
    private Spatial spatialHash;
    private LevelElementDataStructure lines = new LevelElementDataStructure();
    private LevelElementDataStructure sectors = new LevelElementDataStructure();
    private ModelInstance levelModelInstance;
    private Environment environment;

    public Level(float gravity, String name, SkyBoxTextures skyBoxTextures) {
        this.skyBox = createSkyBox(skyBoxTextures);
        this.name = name;
        Level.gravity = gravity;
        spatialHash = new Spatial();
    }

    private ModelInstance createSkyBox(SkyBoxTextures skyBoxTextures) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelSides(skyBoxTextures, modelBuilder);
        Model skyBox = modelBuilder.end();
        return new ModelInstance(skyBox);
    }

    private void modelSides(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        createSkyBoxTopSide(skyBoxTextures, modelBuilder);
        createSkyBoxNorthSide(skyBoxTextures, modelBuilder);
        createSkyBoxEastSide(skyBoxTextures, modelBuilder);
        createSkyBoxSouthSide(skyBoxTextures, modelBuilder);
        createSkyBoxWestSide(skyBoxTextures, modelBuilder);
    }

    private void createSkyBoxTopSide(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        Texture top = skyBoxTextures.getTop();
        MeshPartBuilder meshBuilder = createMeshBuilderForSkyBox(modelBuilder, top, "top");
        float length = 9;
        meshBuilder.rect(new Vector3(-length, length, length),
                new Vector3(length, length, length), new Vector3(length, -length, length),
                new Vector3(-length, -length, length), new Vector3(0, 0, 0));
    }

    private void createSkyBoxNorthSide(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        Texture north = skyBoxTextures.getNorth();
        MeshPartBuilder meshBuilder = createMeshBuilderForSkyBox(modelBuilder, north, "north");
        meshBuilder.setUVRange(1, 1, 0, 0);
        float length = 9;
        meshBuilder.rect(new Vector3(length, length, length),
                new Vector3(-length, length, length), new Vector3(-length, length, -length),
                new Vector3(length, length, -length), new Vector3(0, 0, 0));
    }


    private void createSkyBoxEastSide(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        Texture east = skyBoxTextures.getEast();
        MeshPartBuilder meshBuilder = createMeshBuilderForSkyBox(modelBuilder, east, "east");
        meshBuilder.setUVRange(1, 1, 0, 0);
        float length = 9;
        meshBuilder.rect(new Vector3(length, -length, length),
                new Vector3(length, length, length), new Vector3(length, length, -length),
                new Vector3(length, -length, -length), new Vector3(0, 0, 0));
    }

    private void createSkyBoxSouthSide(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        Texture south = skyBoxTextures.getSouth();
        MeshPartBuilder meshBuilder = createMeshBuilderForSkyBox(modelBuilder, south, "south");
        meshBuilder.setUVRange(1, 1, 0, 0);
        float length = 9;
        meshBuilder.rect(new Vector3(-length, -length, length), new Vector3(length, -length, length),
                new Vector3(length, -length, -length), new Vector3(-length, -length, -length),
                new Vector3(0, 0, 0));
    }

    private void createSkyBoxWestSide(SkyBoxTextures skyBoxTextures, ModelBuilder modelBuilder) {
        Texture west = skyBoxTextures.getWest();
        MeshPartBuilder meshBuilder = createMeshBuilderForSkyBox(modelBuilder, west, "west");
        meshBuilder.setUVRange(1, 1, 0, 0);
        float length = 9;
        meshBuilder.rect(new Vector3(-length, length, length), new Vector3(-length, -length, length),
                new Vector3(-length, -length, -length), new Vector3(-length, length, -length),
                new Vector3(0, 0, 0));
    }


    private MeshPartBuilder createMeshBuilderForSkyBox(ModelBuilder modelBuilder, Texture top, String partName) {
        top.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Material material = new Material(TextureAttribute.createDiffuse(top));
        material.set(new DepthTestAttribute(0, false));
        int attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        MeshPartBuilder meshBuilder = modelBuilder.part(partName, GL20.GL_TRIANGLES, attributes, material);
        return meshBuilder;
    }

    public static float getGravity() {
        return gravity;
    }

    public LevelElementDataStructure getSectors() {
        return sectors;
    }

    public LevelElementDataStructure getLines() {
        return lines;
    }

    private void createEnvironment() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(1, 0, 0);
        directionalLight.set(0.4f, 0.4f, 0.4f, 1, 0, 0);
        environment.add(directionalLight);
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0, 0, 0, 1f));
    }

    public Spatial getSpatial() {
        return spatialHash;
    }

    public ModelInstance getModel() {
        return levelModelInstance;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void initialize(ModelInstance levelModelInstance, Entity playerEntity) {
        this.levelModelInstance = levelModelInstance;
        createEnvironment();
        CameraComponent camComp = ComponentsMapper.camera.get(playerEntity);
        camComp.subscribeForCameraEvents(this);
        onCameraPositionChange(camComp.getCameraPositionX(), camComp.getCameraPositionY(), camComp.getCameraPositionZ());
    }

    public String getName() {
        return name;
    }

    public ModelInstance getSkyBox() {
        return skyBox;
    }

    @Override
    public void onCameraPositionChange(float x, float y, float z) {
        skyBox.transform.setToTranslation(x, y, z);
    }
}
