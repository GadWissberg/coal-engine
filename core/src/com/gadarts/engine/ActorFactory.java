package com.gadarts.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.collision.events.*;
import com.gadarts.engine.components.*;
import com.gadarts.engine.components.bullets.BulletComponent;
import com.gadarts.engine.components.bullets.BulletDefinitionComponent;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.components.enemies.EnemyDefinitionComponent;
import com.gadarts.engine.components.model.ModelDefinition;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.components.model.instance.ModelInstancePool;
import com.gadarts.engine.components.pickups.PickupComponent;
import com.gadarts.engine.components.pickups.PickupDefinitionComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.entities.enemies.EnemyDefinitionEntity;
import com.gadarts.engine.entities.pickups.PickupDefinitionEntity;
import com.gadarts.engine.exceptions.GameFailureException;
import com.gadarts.engine.systems.EffectsSystem;
import com.gadarts.engine.systems.EnemySystem;
import com.gadarts.engine.systems.render.RenderSystem;
import com.gadarts.engine.systems.velocity.MovementForce;
import com.gadarts.engine.utils.C;
import com.google.gson.JsonObject;
import elements.actor.Type;
import utils.SharedC;

public class ActorFactory {
    private Vector3 auxVector3_1 = new Vector3();
    private Vector3 auxVector3_2 = new Vector3();

    public Entity inflateActorFromJson(JsonObject actorJsonObject, PooledEngine engine, Level level)
            throws GameFailureException {
        Type type = Type.valueOf(actorJsonObject.get(C.LevelKeys.TYPE).getAsString());
        Entity result = null;
        switch (type) {
            case PLAYER:
                result = inflatePlayer(actorJsonObject, engine, level);
                break;

            case PICKUP:
                result = inflatePickup(actorJsonObject, engine, level);
                break;

            case ENEMY:
                result = inflateEnemy(actorJsonObject, engine, level);
                break;
        }
        return result;
    }

    private static Entity inflateEnemy(JsonObject actorJsonObject, final PooledEngine engine, Level level) throws GameFailureException {
        ImmutableArray<Entity> defs = engine.getEntitiesFor(Family.all(EnemyDefinitionComponent.class).get());
        final Entity enemyEntity = engine.createEntity();
        for (Entity entity : defs) {
            EnemyDefinitionEntity enemyDefinitionEntity = (EnemyDefinitionEntity) entity;
            EnemyDefinitionComponent enemyDefComponent = ComponentsMapper.enemyDefinition.get(enemyDefinitionEntity);
            if (enemyDefComponent.getName().equals("enemy_test")) {


                final ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
                ModelDefinition modelDefinition = createEnemyBillboard(enemyDefComponent.getAtlas());
                modelInstanceComponent.setModelInstance(ModelInstancePool.obtain(modelDefinition), modelDefinition);
                modelInstanceComponent.setBillboard(true);

                VelocityComponent velocityComponent = engine.createComponent(VelocityComponent.class);
                velocityComponent.setMaxStepAltitude(0.5f);
                velocityComponent.setRaiseSpeed(2);
                MovementForce frontForce = velocityComponent.getFrontForce();
                frontForce.setEnabled(false);
                frontForce.setAcceleration(0.1f);
                frontForce.setMaxSpeed(enemyDefComponent.getSpeed());

                EnemyComponent enemyComponent = engine.createComponent(EnemyComponent.class);
                enemyComponent.setDefinitionComponent(enemyDefComponent);
                enemyComponent.subscribeForDirectionChange(frontForce);
                enemyComponent.setDirection(actorJsonObject.get(C.LevelKeys.DIRECTION).getAsFloat());
                final EnemySystem enemySystem = engine.getSystem(EnemySystem.class);
                enemyComponent.setEventOnSwitchToHurtingFrame(new Runnable() {
                    @Override
                    public void run() {
                        enemySystem.onSwitchToHurtingFrame(enemyEntity);
                    }
                });
                enemyComponent.setHP(enemyDefComponent.getHp());

                ActorComponent actorComponent = obtainActorComponent(Type.ENEMY, actorJsonObject.get(C.LevelKeys.ID).getAsLong(), engine);

                PositionComponent positionComponent = inflatePositionComponent(actorJsonObject, level, actorComponent, engine);
                positionComponent.subscribeForPositionEvents(modelInstanceComponent);
                positionComponent.setRadius(enemyDefComponent.getRadius());
                positionComponent.setBodyAltitude(1);

                CollisionsEventsComponent collisionsEventsComponent = engine.createComponent(CollisionsEventsComponent.class);
                collisionsEventsComponent.add(Type.ENEMY, new BlockCollisionEvent());
                collisionsEventsComponent.defineCollisionWithBlockingLine(new EnemyLineCollisionEvent());
                enemyEntity.add(positionComponent);
                enemyEntity.add(velocityComponent);
                enemyEntity.add(modelInstanceComponent);
                enemyEntity.add(enemyComponent);
                enemyEntity.add(actorComponent);
                enemyEntity.add(collisionsEventsComponent);
                break;
            }
        }
        return enemyEntity;
    }

    private static ModelDefinition createEnemyBillboard(TextureAtlas atlas) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        int attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        TextureAtlas.AtlasRegion region = atlas.findRegion("running_f");
        TextureAttribute diffuse = TextureAttribute.createDiffuse(region);
        Material material = new Material("enemy_texture", diffuse);
        MeshPartBuilder meshBuilder = modelBuilder.part("enemy", GL20.GL_TRIANGLES, attributes, material);
        float width = region.getRegionWidth() / SharedC.WORLD_UNIT;
        float height = region.getRegionHeight() / SharedC.WORLD_UNIT;
        meshBuilder.rect(
                new Vector3(0, -width / 2, 0),
                new Vector3(0, width / 2, 0),
                new Vector3(0, width / 2, height),
                new Vector3(0, -width / 2, height),
                new Vector3(1, 0, 0));
        Model model = modelBuilder.end();
        ModelDefinition modelDefinition = new ModelDefinition(model, "enemy");
        return modelDefinition;
    }

    private Entity inflatePickup(JsonObject actorJsonObj, PooledEngine engine, Level level) throws GameFailureException {
        String name = "gun"; //TEMP

        ImmutableArray<Entity> puDefinitions = engine.getEntitiesFor(Family.all(PickupDefinitionComponent.class).get());
        Entity pickupEntity = engine.createEntity();
        for (Entity entity : puDefinitions) {
            PickupDefinitionEntity pickupDefinitionEntity = (PickupDefinitionEntity) entity;
            PickupDefinitionComponent pickupDefComponent = ComponentsMapper.pickupDefinition.get(pickupDefinitionEntity);
            if (pickupDefComponent.getName().equals(name)) {
                ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
                ModelDefinition modelDefinition = pickupDefComponent.getModelDefinition();
                modelInstanceComponent.setModelInstance(ModelInstancePool.obtain(modelDefinition), modelDefinition);

                PickupComponent pickupComponent = engine.createComponent(PickupComponent.class);
                pickupComponent.setDefinitionComponent(pickupDefComponent);

                VelocityComponent velocityComponent = engine.createComponent(VelocityComponent.class);

                ActorComponent actorComponent = obtainActorComponent(Type.PICKUP, actorJsonObj.get(C.LevelKeys.ID).getAsLong(), engine);

                PositionComponent positionComponent = inflatePositionComponent(actorJsonObj, level, actorComponent, engine);
                positionComponent.subscribeForPositionEvents(modelInstanceComponent);
                positionComponent.setRadius(pickupDefComponent.getRadius());
                positionComponent.setBodyAltitude(pickupDefComponent.getBodyAltitude());

                pickupEntity.add(positionComponent);
                pickupEntity.add(velocityComponent);
                pickupEntity.add(modelInstanceComponent);
                pickupEntity.add(pickupComponent);
                pickupEntity.add(actorComponent);
                break;
            }
        }
        return pickupEntity;
    }

    private static Entity inflatePlayer(JsonObject actorJsonObj, final PooledEngine engine, Level level) throws GameFailureException {
        Entity playerEntity = engine.createEntity();
        CollisionsEventsComponent collisionsEventsComponent = engine.createComponent(CollisionsEventsComponent.class);
        collisionsEventsComponent.add(Type.PICKUP, new ActorCollisionEvent() {
            @Override
            public boolean run(Entity entity, Entity other, Vector3 nextPositionVector) {
                PickupComponent pickupComponent = ComponentsMapper.pickup.get(other);
                pickupComponent.getDefinitionComponent().getOnPickupEvent().run();
                engine.removeEntity(other);
                EffectsSystem.setScreenColorMultiplier(0, 1, 0);
                return true;
            }
        });
        final EnemySystem enemySystem = engine.getSystem(EnemySystem.class);
        collisionsEventsComponent.add(Type.ENEMY, new PlayerCollisionWithEnemyEvent(enemySystem));

        ActorComponent actorComponent = obtainActorComponent(Type.PLAYER, actorJsonObj.get(C.LevelKeys.ID).getAsLong(), engine);

        CameraComponent cameraComponent = engine.createComponent(CameraComponent.class);
        PerspectiveCamera cam = new PerspectiveCamera(cameraComponent.getInitialFov(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 40;
        float direction = actorJsonObj.get(C.LevelKeys.DIRECTION).getAsFloat();
        cam.rotate(Vector3.X, 90);
        cam.rotate(Vector3.Z, -90);
        cam.rotate(Vector3.Z, direction);
        cam.update();
        cameraComponent.setCamera(cam);
        engine.getSystem(RenderSystem.class).setCamera(cam);

        playerEntity.add(cameraComponent);
        playerEntity.add(inflatePositionComponent(actorJsonObj, level, actorComponent, engine));
        playerEntity.add(engine.createComponent(PlayerComponent.class));

        VelocityComponent velocityComponent = engine.createComponent(VelocityComponent.class);
        playerEntity.add(velocityComponent);

        playerEntity.add(actorComponent);
        playerEntity.add(collisionsEventsComponent);
        return playerEntity;
    }

    private static ActorComponent obtainActorComponent(Type type, long id, PooledEngine engine) {
        ActorComponent actorComponent = engine.createComponent(ActorComponent.class);
        actorComponent.setType(type);
        actorComponent.setId(id);
        return actorComponent;
    }

    private static PositionComponent inflatePositionComponent(JsonObject actorJsonObj, Level level,
                                                              ActorComponent actorComponent, PooledEngine engine) throws GameFailureException {
        long curSecId = actorJsonObj.get(C.LevelKeys.CURRENT_SECTOR_ID).getAsLong();
        Long id = actorComponent.getId();
        String message = String.format(C.Errors.LevelRelated.ACTOR_IS_OUTSIDE, level.getName(), id);
        if (curSecId < 0) throw new GameFailureException(message);
        float x = actorJsonObj.get(C.LevelKeys.X).getAsInt() / SharedC.WORLD_UNIT;
        float y = actorJsonObj.get(C.LevelKeys.Y).getAsInt() / SharedC.WORLD_UNIT;
        message = String.format(C.Errors.LevelRelated.NEG_COORDINATE, level.getName(), "Actor", id);
        if (x < 0 || y < 0) throw new GameFailureException(message);
        float floorAltitude = actorJsonObj.get(C.LevelKeys.CURRENT_FLOOR_ALTITUDE).getAsFloat();
        float ceilingAltitude = actorJsonObj.get(C.LevelKeys.CURRENT_CEILING_ALTITUDE).getAsFloat();
        return generatePositionComponent(x, y, floorAltitude, floorAltitude,
                ceilingAltitude, curSecId, engine);
    }

    private static PositionComponent generatePositionComponent(float x, float y, float z, float floor, float ceiling,
                                                               long currSecId, PooledEngine engine) {
        PositionComponent positionComponent = engine.createComponent(PositionComponent.class);
        positionComponent.setX(x);
        positionComponent.setY(y);
        positionComponent.setZ(z);
        positionComponent.setCurrentFloorAltitude(floor);
        positionComponent.setCurrentCeilingAltitude(ceiling);
        positionComponent.setCurrentSectorId(currSecId);
        return positionComponent;
    }

    public Entity generateBullet(PositionComponent parentActorPositionComponent,
                                 BulletDefinitionComponent defComponent, Vector3 direction, final PooledEngine engine) {
        final Entity bullet = engine.createEntity();

        bullet.add(obtainActorComponent(Type.BULLET, -1, engine));

        CollisionsEventsComponent collisionsEventsComponent = engine.createComponent(CollisionsEventsComponent.class);
        collisionsEventsComponent.defineCollisionWithBlockingLine(new BlockingLineCollisionEvent() {
            @Override
            public void run(Line other, Entity entity) {
                engine.removeEntity(bullet);
            }
        });
        collisionsEventsComponent.defineCollisionWithBlockingLine(new SurfaceCollisionEvent() {
            @Override
            public void run() {
                engine.removeEntity(bullet);
            }
        });

        final EnemySystem enemySystem = engine.getSystem(EnemySystem.class);
        collisionsEventsComponent.add(Type.ENEMY, new ActorCollisionEvent() {
            @Override
            public boolean run(Entity entity, Entity other, Vector3 nextPositionVector) {
                if (!ComponentsMapper.enemy.get(other).getStatus().equals(EnemyComponent.Status.DEAD)) {
                    engine.removeEntity(bullet);
                    enemySystem.enemyTakeDamage(other, bullet);
                    return true;
                }
                return false;
            }

        });
        bullet.add(collisionsEventsComponent);

        float currentFloorAltitude = parentActorPositionComponent.getCurrentFloorAltitude();
        float currentCeilingAltitude = parentActorPositionComponent.getCurrentCeilingAltitude();
        long currentSectorId = parentActorPositionComponent.getCurrentSectorId();
        direction.nor();
        float x = parentActorPositionComponent.getX();
        float y = parentActorPositionComponent.getY();
        float z = parentActorPositionComponent.getZ() + parentActorPositionComponent.getBodyAltitude() / 3 * 2;
        PositionComponent pc = generatePositionComponent(x, y, z, currentFloorAltitude, currentCeilingAltitude, currentSectorId, engine);
        pc.setRadius(defComponent.getRadius());
        bullet.add(pc);

        BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
        bulletComponent.setDefinitionComponent(defComponent);
        bullet.add(bulletComponent);

        ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
        ModelInstance modelInstance = ModelInstancePool.obtain(defComponent.getModelDefinition());
        modelInstanceComponent.setModelInstance(modelInstance, defComponent.getModelDefinition());
        modelInstance.transform.setToTranslation(x, y, z);
        Vector3 axis = auxVector3_1.set(1, 0, 0).crs(direction);
        float angle = (float) (Math.acos(auxVector3_2.set(1, 0, 0).dot(direction)));
        float randomness = 0.005f;
        direction.x += MathUtils.random(-randomness, randomness);
        direction.y += MathUtils.random(-randomness, randomness);
        direction.z += MathUtils.random(-randomness, randomness);
        modelInstance.transform.rotateRad(axis, angle);
        pc.subscribeForPositionEvents(modelInstanceComponent);
        bullet.add(modelInstanceComponent);

        VelocityComponent velocityComponent = engine.createComponent(VelocityComponent.class);
        velocityComponent.getGravityForce().setEnabled(false);
        MovementForce frontForce = velocityComponent.getFrontForce();
        frontForce.setDirection(direction);
        frontForce.setSpeed(defComponent.getSpeed());
        bullet.add(velocityComponent);

        return bullet;
    }
}
