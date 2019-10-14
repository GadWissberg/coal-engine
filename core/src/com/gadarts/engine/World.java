package com.gadarts.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.PlayerComponent;
import com.gadarts.engine.components.VelocityComponent;
import com.gadarts.engine.components.bullets.BulletDefinitionComponent;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.enemies.EnemyDefinitionComponent;
import com.gadarts.engine.components.model.ModelDefinition;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.components.model.instance.ModelInstancePool;
import com.gadarts.engine.components.pickups.PickupComponent;
import com.gadarts.engine.components.pickups.PickupDefinitionComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.*;
import com.gadarts.engine.entities.bullets.BulletDefinitionEntity;
import com.gadarts.engine.entities.enemies.EnemyDefinitionEntity;
import com.gadarts.engine.entities.pickups.PickupDefinitionEntity;
import com.gadarts.engine.exceptions.GameFailureException;
import com.gadarts.engine.input.interfaces.GameInputProcessor;
import com.gadarts.engine.modelers.RegionAttributesValues;
import com.gadarts.engine.modelers.SurfaceModeler;
import com.gadarts.engine.modelers.WallModeler;
import com.gadarts.engine.properties.EnemyDefinitionProperties;
import com.gadarts.engine.properties.PickupDefinitionProperties;
import com.gadarts.engine.properties.PlayerProperties;
import com.gadarts.engine.systems.PickupSystem;
import com.gadarts.engine.systems.SystemsHandler;
import com.gadarts.engine.systems.player.PlayerController;
import com.gadarts.engine.systems.player.PlayerSystem;
import com.gadarts.engine.systems.render.RenderSystem;
import com.gadarts.engine.systems.render.RenderSystemState;
import com.gadarts.engine.systems.velocity.MovementForce;
import com.gadarts.engine.utils.C;
import com.gadarts.engine.utils.C.Errors.LevelRelated;
import com.gadarts.engine.utils.C.LevelKeys;
import com.gadarts.engine.utils.C.ShaderRelated.RegionAttributes.RegionUvAttributes;
import com.gadarts.engine.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elements.LevelElementDataStructure;
import elements.texture.TextureDefinition;
import elements.texture.WallTextureDefinition;
import utils.SharedC;
import utils.SharedUtils;

import java.io.BufferedReader;
import java.util.*;

public class World {
    private PooledEngine engine;
    private PlayerController playerController;
    private Level level;
    private Entity playerEntity;
    private ActorFactory actorFactory = new ActorFactory();
    private SystemsHandler systemsHandler = new SystemsHandler();

    public World() {
        createEngine();
    }

    public static void raiseError(String message, Object... params) throws GameFailureException {
        throw new GameFailureException(String.format(message, params));
    }

    private void createEngine() {
        engine = new PooledEngine();
    }

    public void init(float gravity, SkyBoxTextures skyBoxTextures, TextureAtlas surfaceTextures)
            throws GameFailureException {
        systemsHandler.createSystems(engine);
        createLevel(gravity, skyBoxTextures, surfaceTextures);
        systemsHandler.initializeSystems(level);
    }


    public void setInputProcessor(GameInputProcessor processor) {
        if (EngineSettings.SPECTATOR_MODE) {
            initializeSpectatorInputProcessor();
        } else {
            processor.subscribeForMouseMoved(systemsHandler.getRenderSystem());
            Gdx.input.setInputProcessor(processor);
        }
    }

    private void initializeSpectatorInputProcessor() {
        RenderSystem renderSystem = systemsHandler.getRenderSystem();
        CameraInputController controller = ComponentsMapper.camera.get(playerEntity).createCameraInputController();
        renderSystem.setDebugInput(controller);
        Gdx.input.setInputProcessor(controller);
    }

    public void initializePlayer(PlayerProperties properties) {
        initializePlayerComponent(properties, playerEntity);
        PositionComponent positionComponent = initializePlayerPositionComponent(properties, playerEntity);
        initializeCameraComponent(positionComponent, playerEntity, properties);
        initializePlayerVelocityComponent(properties, playerEntity);
        playerController = new PlayerController(playerEntity, engine.getSystem(PlayerSystem.class));
        engine.getSystem(RenderSystem.class).setPlayerController(playerController);
    }

    private void initializeCameraComponent(PositionComponent positionComponent, Entity playerEntity,
                                           PlayerProperties properties) {
        CameraComponent cameraComponent = ComponentsMapper.camera.get(playerEntity);
        cameraComponent.setHeadRelativeZ(properties.getBodyAltitude() - C.Camera.EYES_TO_TOP_OFFSET);
        cameraComponent.setInitialFov(properties.getFov());
        cameraComponent.setFov(properties.getFov());
        cameraComponent.updateCamera();
        positionComponent.positionChanged(Gdx.graphics.getDeltaTime());
    }

    private void initializePlayerVelocityComponent(PlayerProperties properties, Entity playerEntity) {
        VelocityComponent velocityComponent = ComponentsMapper.velocity.get(playerEntity);
        initializePlayerForces(properties, velocityComponent);
        velocityComponent.setMaxStepAltitude(properties.getMaxStepAltitude());
        velocityComponent.setRaiseSpeed(properties.getRaiseSpeed());
    }

    private void initializePlayerForces(PlayerProperties properties, VelocityComponent velocityComponent) {
        float maxSpeed = properties.getMaxSpeed(), minSpeed = properties.getMinSpeed();
        initializePlayerFrontForce(velocityComponent, maxSpeed, minSpeed);
        MovementForce strafeForce = Pools.obtain(MovementForce.class);
        strafeForce.init(0, 1, 0, minSpeed, maxSpeed, false);
        strafeForce.setAccelerateOnGroundOnly(true);
        strafeForce.setName(C.PLAYER_STRAFE_FORCE_NAME);
        velocityComponent.addForce(strafeForce);
        engine.getSystem(PlayerSystem.class).setStrafeForce(strafeForce);
    }

    private void initializePlayerFrontForce(VelocityComponent velocityComponent, float maxSpeed, float minSpeed) {
        velocityComponent.getFrontForce().setMaxSpeed(maxSpeed);
        velocityComponent.getFrontForce().setMinSpeed(minSpeed);
        velocityComponent.getFrontForce().setAffectingZ(false);
        velocityComponent.getFrontForce().setAccelerateOnGroundOnly(true);
    }

    private PositionComponent initializePlayerPositionComponent(PlayerProperties properties, Entity playerEntity) {
        PositionComponent positionComponent = ComponentsMapper.position.get(playerEntity);
        positionComponent.setRadius(properties.getRadius());
        positionComponent.setBodyAltitude(properties.getBodyAltitude());
        positionComponent.setZ(positionComponent.getCurrentFloorAltitude());
        return positionComponent;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    private void initializePlayerComponent(PlayerProperties properties, Entity playerEntity) {
        PlayerComponent playerComponent = ComponentsMapper.player.get(playerEntity);
        playerComponent.setMaxStepAltitude(properties.getRadius());
        playerComponent.setMaxMovementAltitudeSpeed(properties.getMaxMovementAltitudeSpeed());
        playerComponent.setJumpSpeed(properties.getJumpSpeed());
    }

    public void initializePickups(float radius, Color pickupColor) {
        PickupSystem.setRadius(radius);
        PickupSystem.setPickupColor(pickupColor);
    }


    public void render(float deltaTime) throws GameFailureException {
        engine.update(deltaTime);
        RenderSystemState state = systemsHandler.getRenderSystem().getState();
        if (state.getState() != RenderSystemState.RenderSystemStates.NORMAL) {
            World.raiseError(state.getMessage());
        }
    }

    private void createLevel(float gravity, SkyBoxTextures skyBoxTextures, TextureAtlas surfaceTextures) throws GameFailureException {
        BufferedReader bufferedReader = new BufferedReader(Gdx.files.internal("test.json").reader());
        JsonObject levelJsonObj = (new Gson()).fromJson(bufferedReader, JsonObject.class);
        level = new Level(gravity, "test.json", skyBoxTextures);
        inflateElements(levelJsonObj);
        ModelInstance levelModelInstance = createLevelModel(surfaceTextures);
        playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
        level.initialize(levelModelInstance, playerEntity);
    }

    private void inflateElements(JsonObject levelJsonObject) throws GameFailureException {
        JsonObject elementsJsonObj = levelJsonObject.getAsJsonObject(LevelKeys.ELEMENTS);
        inflateActors(elementsJsonObj);
        inflateLines(elementsJsonObj);
        inflateSectors(elementsJsonObj);
    }

    private void inflateActors(JsonObject elementsJsonObj) throws GameFailureException {
        JsonObject actorsJson = elementsJsonObj.getAsJsonObject(LevelKeys.ACTORS).getAsJsonObject(LevelKeys.HASHMAP);
        Set<Map.Entry<String, JsonElement>> list = actorsJson.entrySet();
        for (Map.Entry<String, JsonElement> entry : list) {
            inflateActor(entry);
        }
        String message = String.format(LevelRelated.NO_PLAYER_WAS_FOUND, level.getName());
        if (engine.getEntitiesFor(Family.one(PlayerComponent.class).get()).size() == 0)
            throw new GameFailureException(message);
    }

    private void inflateActor(Map.Entry<String, JsonElement> entry) throws GameFailureException {
        JsonObject actorJsonObject = entry.getValue().getAsJsonObject();
        Entity actor = actorFactory.inflateActorFromJson(actorJsonObject, engine, level);
        engine.addEntity(actor);
    }

    private ModelInstance createLevelModel(TextureAtlas surfaceTextures) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        ArrayList<RegionAttributesValues> wallRegionAttributesValues = createWallsModels(modelBuilder, surfaceTextures);
        ArrayList<RegionAttributesValues> surfaceRegionAttributesValues = createCeilAndFloorModels(modelBuilder, surfaceTextures);
        wallRegionAttributesValues.addAll(surfaceRegionAttributesValues);
        Model levelModel = modelBuilder.end();
        fillCustom(levelModel.meshes.get(0), wallRegionAttributesValues);
        return new ModelInstance(levelModel);
    }

    private void fillCustom(Mesh mesh, ArrayList<RegionAttributesValues> regionAttributesValues) {
        int numVertices = mesh.getNumVertices();
        int vertexSize = mesh.getVertexAttributes().vertexSize / 4;
        VertexAttribute customAttribute = mesh.getVertexAttribute(RegionUvAttributes.ATTRIBUTE_USAGE);
        int offset = customAttribute.offset / 4;
        float[] verticesData = new float[numVertices * vertexSize];
        mesh.getVertices(verticesData);
        for (int i = 0; i < numVertices; ++i) {
            int index = i * vertexSize + offset;
            Vector2 uv = regionAttributesValues.get(i).getUv();
            Vector2 size = regionAttributesValues.get(i).getSize();
            verticesData[index] = uv.x;
            verticesData[index + 1] = uv.y;
            verticesData[index + 2] = size.x;
            verticesData[index + 3] = size.y;
        }
        mesh.updateVertices(0, verticesData);
    }

    private ArrayList<RegionAttributesValues> createCeilAndFloorModels(ModelBuilder modelBuilder, TextureAtlas surfaceTextures) {
        SurfaceModeler surfaceModeler = new SurfaceModeler(modelBuilder);
        List<Sector> allSectors = level.getSectors().getQuadTree().queryAll();
        for (Sector sector : allSectors) modelSectorSurface(surfaceModeler, sector, surfaceTextures);
        return surfaceModeler.getRegionAttributesValues();
    }

    private void modelSectorSurface(SurfaceModeler surfaceModeler, Sector sector, TextureAtlas surfaceTextures) {
        ArrayList<SubSector> subSectors = sector.getSubSectors();
        TextureRegion ceilingTexture = null;
        try {
            ceilingTexture = new TextureRegion(surfaceTextures.findRegion(sector.getCeilingTexture().getName()));
        } catch (Exception e) {

        }
        TextureRegion floorTexture = null;
        try {
            floorTexture = new TextureRegion(surfaceTextures.findRegion(sector.getFloorTexture().getName()));
        } catch (Exception e) {

        }
        surfaceModeler.initializeTextures(ceilingTexture, floorTexture);
        for (int i = 0; i < subSectors.size(); i++) {
            SubSector subSector = subSectors.get(i);
            if (subSector.getContainerId() == -1) {
                surfaceModeler.modelSurface(sector, i, subSector);
            }
        }
    }

    private ArrayList<RegionAttributesValues> createWallsModels(ModelBuilder modelBuilder, TextureAtlas surfaceTextures) {
        LevelElementDataStructure lines = level.getLines();
        WallModeler wallModeler = new WallModeler(level.getSectors(), surfaceTextures);
        List<Line> allLines = lines.getQuadTree().queryAll();
        for (Line line : allLines) createWallModel(modelBuilder, wallModeler, line);
        return wallModeler.getRegionAttributesValues();
    }

    private void createWallModel(ModelBuilder modelBuilder, WallModeler wallModeler, Line line) {
        wallModeler.modelWall(modelBuilder, line);
    }

    private void inflateLines(JsonObject levelJsonObj) throws GameFailureException {
        JsonObject linesJsonObject = levelJsonObj.getAsJsonObject(LevelKeys.LINES);
        Set<Map.Entry<String, JsonElement>> lines = linesJsonObject.getAsJsonObject(LevelKeys.HASHMAP).entrySet();
        for (Map.Entry<String, JsonElement> entry : lines) {
            inflateLine(entry);
        }
    }

    private void inflateSectors(JsonObject levelJsonObj) throws GameFailureException {
        JsonObject sectorsJsonObject = levelJsonObj.getAsJsonObject(LevelKeys.SECTORS);
        Set<Map.Entry<String, JsonElement>> sectors = sectorsJsonObject.getAsJsonObject(LevelKeys.HASHMAP).entrySet();
        for (Map.Entry<String, JsonElement> entry : sectors) {
            inflateSector(entry);
        }
    }

    private void inflateLine(Map.Entry<String, JsonElement> entry) throws GameFailureException {
        Line line = createLine(entry);
        LevelElementDataStructure lines = level.getLines();
        long id = line.getId();
        if (!lines.contains(id)) lines.put(id, line);
        else {
            raiseError(LevelRelated.TWO_ELEMENTS_WITH_SAME_ID, "lines", id);
            return;
        }
    }

    private void inflateSector(Map.Entry<String, JsonElement> entry) throws GameFailureException {
        Sector sector = createSector(entry);
        LevelElementDataStructure sectors = level.getSectors();
        long id = sector.getId();
        if (!sectors.contains(id)) sectors.put(id, sector);
        else {
            raiseError(LevelRelated.TWO_ELEMENTS_WITH_SAME_ID, "sectors", id);
        }
    }

    private Line createLine(Map.Entry<String, JsonElement> entry) throws GameFailureException {
        JsonObject lineJsonObject = entry.getValue().getAsJsonObject();
        Vertex src = createVertex(lineJsonObject.getAsJsonObject(LevelKeys.SRC));
        Vertex dst = createVertex(lineJsonObject.getAsJsonObject(LevelKeys.DST));
        Line line = new Line(src, dst);
        defineLine(lineJsonObject, line);
        if (src.equals(dst)) raiseError(LevelRelated.LINE_WITH_EQUAL_VERTICES, level.getName(), line.getId());
        return line;
    }

    private Sector createSector(Map.Entry<String, JsonElement> entry) throws GameFailureException {
        JsonObject sectorJsonObject = entry.getValue().getAsJsonObject();
        float floorAltitude = sectorJsonObject.get(LevelKeys.FLOOR_ALTITUDE).getAsFloat();
        float ceilAltitude = sectorJsonObject.get(LevelKeys.CEIL_ALTITUDE).getAsFloat();
        Sector sector = new Sector(floorAltitude, ceilAltitude, createSubSectors(sectorJsonObject));
        checkSector(sector);
        defineSector(sectorJsonObject, sector);
        return sector;
    }

    private void checkSector(Sector sector) throws GameFailureException {
        float ceilingAltitude = sector.getCeilingAltitude();
        float floorAltitude = sector.getFloorAltitude();
        String message = String.format(LevelRelated.CEILING_IS_NOT_HIGHER_THAN_FLOOR, level.getName(), sector.getId());
        if (ceilingAltitude <= floorAltitude) throw new GameFailureException(message);
        message = String.format(LevelRelated.NEGATIVE_ALTITUDE, level.getName(), sector.getId());
        if (ceilingAltitude < 0 || floorAltitude < 0) throw new GameFailureException(message);
    }

    private ArrayList<SubSector> createSubSectors(JsonObject sectorJsonObject) {
        JsonArray subSectorsJsonArray = sectorJsonObject.getAsJsonArray(LevelKeys.SUB_SECTORS);
        ArrayList<SubSector> result = new ArrayList<SubSector>();
        Iterator<JsonElement> it = subSectorsJsonArray.iterator();
        while (it.hasNext()) {
            JsonObject subSectorJson = it.next().getAsJsonObject();
            result.add(createSubSector(subSectorJson));
        }
        return result;
    }

    private SubSector createSubSector(JsonObject subSectorJson) {
        JsonArray pointsArray = subSectorJson.get(LevelKeys.POINTS).getAsJsonArray();
        float[] points = new float[pointsArray.size()];
        for (int i = 0; i < pointsArray.size(); i++) {
            points[i] = pointsArray.get(i).getAsFloat();
        }
        return new SubSector(points, subSectorJson.get(LevelKeys.CONTAINER_ID).getAsLong());
    }

    private void defineSector(JsonObject sectorJsonObject, Sector sector) throws GameFailureException {
        sector.setX(sectorJsonObject.get(LevelKeys.X).getAsFloat());
        sector.setY(sectorJsonObject.get(LevelKeys.Y).getAsFloat());
        sector.setWidth(sectorJsonObject.get(LevelKeys.WIDTH).getAsFloat());
        sector.setHeight(sectorJsonObject.get(LevelKeys.HEIGHT).getAsFloat());
        defineSectorTextureRelated(sectorJsonObject, sector);
        defineElement(sectorJsonObject, sector);
    }

    private void defineSectorTextureRelated(JsonObject sectorJsonObject, Sector sector) {
        sector.setFloorTexture(defineTexture(sectorJsonObject.getAsJsonObject(LevelKeys.FLOOR_TEXTURE), false));
        sector.setCeilingTexture(defineTexture(sectorJsonObject.getAsJsonObject(LevelKeys.CEILING_TEXTURE), false));
    }

    private TextureDefinition defineTexture(JsonObject textureJson) {
        return defineTexture(textureJson, true);
    }

    private TextureDefinition defineTexture(JsonObject textureJson, boolean applyDefault) {
        TextureDefinition texDef = new TextureDefinition();
        texDef.setName(SharedUtils.getStringFromJson(textureJson, LevelKeys.NAME, applyDefault ? "ceil" : null));
        texDef.setHorizontalOffset(SharedUtils.getFloatFromJson(textureJson, LevelKeys.HOR_OFFSET, 0));
        texDef.setVerticalOffset(SharedUtils.getFloatFromJson(textureJson, LevelKeys.VER_OFFSET, 0));
        texDef.setOpacity(SharedUtils.getFloatFromJson(textureJson, LevelKeys.OPACITY, 1));
        return texDef;
    }

    private Vertex createVertex(JsonObject jsonObject) {
        float x = jsonObject.get(LevelKeys.X).getAsFloat() / SharedC.WORLD_UNIT;
        float y = jsonObject.get(LevelKeys.Y).getAsFloat() / SharedC.WORLD_UNIT;
        return new Vertex(x, y);
    }

    private void defineLine(JsonObject lineJsonObject, Line line) throws GameFailureException {
        line.setSolid(lineJsonObject.get(LevelKeys.SOLID).getAsBoolean());
        defineLineSectors(lineJsonObject, line);
        line.setLineLength(Utils.calculateLineLength(line));
        inflateLineTextures(lineJsonObject, line);
        defineElement(lineJsonObject, line);
    }

    private void defineLineSectors(JsonObject lineJsonObject, Line line) {
        line.setFrontSectorId(lineJsonObject.get(LevelKeys.FRONT_SECTOR_ID).getAsLong());
        line.setBackSectorId(lineJsonObject.get(LevelKeys.BACK_SECTOR_ID).getAsLong());
    }

    private void inflateLineTextures(JsonObject lineJsonObject, Line line) {
        line.setFrontTexture(defineWallTexture(lineJsonObject.getAsJsonObject(LevelKeys.FRONT_TEXTURE)));
        line.setBackTexture(defineWallTexture(lineJsonObject.getAsJsonObject(LevelKeys.BACK_TEXTURE)));
    }

    private WallTextureDefinition defineWallTexture(JsonObject textureJsonObject) {
        WallTextureDefinition wallTextureDefinition = new WallTextureDefinition();
        wallTextureDefinition.setTop(defineTexture(textureJsonObject.getAsJsonObject(LevelKeys.TOP), false));
        wallTextureDefinition.setMiddle(defineTexture(textureJsonObject.getAsJsonObject(LevelKeys.MIDDLE), false));
        wallTextureDefinition.setBottom(defineTexture(textureJsonObject.getAsJsonObject(LevelKeys.BOTTOM), false));
        return wallTextureDefinition;
    }

    private void defineElement(JsonObject jsonObject, NonActorElement nonActorElement) throws GameFailureException {
        long id = jsonObject.get(LevelKeys.ID).getAsLong();
        defineElementPosition(jsonObject, nonActorElement, id);
        nonActorElement.setId(id);
        nonActorElement.setWidth(jsonObject.get(LevelKeys.WIDTH).getAsFloat());
        nonActorElement.setHeight(jsonObject.get(LevelKeys.HEIGHT).getAsFloat());
    }

    private void defineElementPosition(JsonObject jsonObject, NonActorElement element, long id)
            throws GameFailureException {
        float x = jsonObject.get(LevelKeys.X).getAsFloat();
        float y = jsonObject.get(LevelKeys.Y).getAsFloat();
        if (x < 0 || y < 0) {
            raiseError(LevelRelated.NEG_COORDINATE, element.getClass().getSimpleName(), id);
        }
        element.setX(x);
        element.setY(y);
    }

    public void definePickup(PickupDefinitionProperties properties) {
        PickupDefinitionEntity pickupEntity = Pools.obtain(PickupDefinitionEntity.class);
        PickupDefinitionComponent pickupDefinitionComponent = Pools.obtain(PickupDefinitionComponent.class);
        pickupDefinitionComponent.applyProperties(properties);
        pickupEntity.add(pickupDefinitionComponent);
        engine.addEntity(pickupEntity);
    }

    public void createPickup(String name, float x, float y, float z) {
        ImmutableArray<Entity> pickupDefinitions = engine.getEntitiesFor(Family.all(PickupDefinitionComponent.class).get());
        for (Entity entity : pickupDefinitions) {
            PickupDefinitionEntity pickupDefinitionEntity = (PickupDefinitionEntity) entity;
            PickupDefinitionComponent pickupDefinitionComponent = ComponentsMapper.pickupDefinition.get(pickupDefinitionEntity);
            if (pickupDefinitionComponent.getName().equals(name)) {
                addPickupInstance(x, y, z, pickupDefinitionComponent);
                break;
            }
        }
    }

    public BulletDefinitionEntity getBulletDefinition(String name) {
        ImmutableArray<Entity> bulletDefinitions = engine.getEntitiesFor(Family.all(BulletDefinitionComponent.class).get());
        for (Entity entity : bulletDefinitions) {
            BulletDefinitionEntity bulletDefinitionEntity = (BulletDefinitionEntity) entity;
            BulletDefinitionComponent pickupDefinitionComponent = ComponentsMapper.bulletDefinition.get(bulletDefinitionEntity);
            if (pickupDefinitionComponent.getName().equals(name)) {
                return bulletDefinitionEntity;
            }
        }
        return null;
    }

    public void createBullet(BulletDefinitionEntity bullet, PlayerController playerController, Vector3 direction) {
        PositionComponent pc = ComponentsMapper.position.get(playerEntity);
        BulletDefinitionComponent bulletDefinitionComponent = ComponentsMapper.bulletDefinition.get(bullet);
        addBulletInstance(pc, bulletDefinitionComponent, direction);
    }

    private void addBulletInstance(PositionComponent pc, BulletDefinitionComponent defComponent, Vector3 direction) {
        Entity bullet = actorFactory.generateBullet(pc, defComponent, direction, engine);
        engine.addEntity(bullet);
    }

    private void addPickupInstance(float x, float y, float z, PickupDefinitionComponent definitionComponent) {
        Entity pickup = engine.createEntity();
        PickupComponent pickupComponent = engine.createComponent(PickupComponent.class);
        pickupComponent.setDefinitionComponent(definitionComponent);
        pickup.add(pickupComponent);
        VelocityComponent velocityComponent = engine.createComponent(VelocityComponent.class);
        pickup.add(velocityComponent);
        PositionComponent positionComponent = engine.createComponent(PositionComponent.class);
        pickup.add(positionComponent);

        ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
        ModelDefinition modelDefinition = definitionComponent.getModelDefinition();
        modelInstanceComponent.setModelInstance(ModelInstancePool.obtain(modelDefinition), modelDefinition);
        modelInstanceComponent.getModelInstance().transform.setToTranslation(x, y, z);
        pickup.add(modelInstanceComponent);

        engine.addEntity(pickup);
    }

    public void defineBullet(String name, ModelDefinition modelDef, float speed, float radius, Model bulletHoleModel) {
        BulletDefinitionEntity bulletEntity = Pools.obtain(BulletDefinitionEntity.class);
        BulletDefinitionComponent bulletComponent = Pools.obtain(BulletDefinitionComponent.class);
        bulletComponent.setName(name);
        bulletComponent.setRadius(radius);
        bulletComponent.setModelDefinition(modelDef);
        bulletComponent.calculateBoundingBox();
        bulletComponent.setSpeed(speed);
        bulletComponent.setBulletHoleModel(bulletHoleModel);
        bulletEntity.add(bulletComponent);
        Model bulletsHolesModel = new Model();
        engine.addEntity(bulletEntity);
    }

    public void defineEnemy(EnemyDefinitionProperties properties) {
        EnemyDefinitionEntity enemyDefinitionEntity = Pools.obtain(EnemyDefinitionEntity.class);
        EnemyDefinitionComponent enemyDefinitionComponent = Pools.obtain(EnemyDefinitionComponent.class);
        enemyDefinitionComponent.setName(properties.getName());
        enemyDefinitionComponent.setAtlas(properties.getAtlas());
        enemyDefinitionComponent.setAttributes(properties.getAttributes());
        enemyDefinitionEntity.add(enemyDefinitionComponent);
        engine.addEntity(enemyDefinitionEntity);
    }
}
