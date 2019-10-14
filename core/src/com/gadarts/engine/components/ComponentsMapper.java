package com.gadarts.engine.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.gadarts.engine.components.bullets.BulletComponent;
import com.gadarts.engine.components.bullets.BulletDefinitionComponent;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.components.enemies.EnemyDefinitionComponent;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.components.pickups.PickupComponent;
import com.gadarts.engine.components.pickups.PickupDefinitionComponent;
import com.gadarts.engine.components.position.PositionComponent;

public final class ComponentsMapper {
    public static final ComponentMapper<PositionComponent> position = ComponentMapper.getFor(PositionComponent.class);
    public static final ComponentMapper<CameraComponent> camera = ComponentMapper.getFor(CameraComponent.class);
    public static final ComponentMapper<VelocityComponent> velocity = ComponentMapper.getFor(VelocityComponent.class);
    public static final ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
    public static final ComponentMapper<PickupDefinitionComponent> pickupDefinition = ComponentMapper.getFor(PickupDefinitionComponent.class);
    public static final ComponentMapper<EnemyDefinitionComponent> enemyDefinition = ComponentMapper.getFor(EnemyDefinitionComponent.class);
    public static final ComponentMapper<BulletDefinitionComponent> bulletDefinition = ComponentMapper.getFor(BulletDefinitionComponent.class);
    public static final ComponentMapper<PickupComponent> pickup = ComponentMapper.getFor(PickupComponent.class);
    public static final ComponentMapper<BulletComponent> bullet = ComponentMapper.getFor(BulletComponent.class);
    public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
    public static final ComponentMapper<CollisionsEventsComponent> collisionsEvents = ComponentMapper.getFor(CollisionsEventsComponent.class);
    public static final ComponentMapper<ActorComponent> actor = ComponentMapper.getFor(ActorComponent.class);
    public static final ComponentMapper<EnemyComponent> enemy = ComponentMapper.getFor(EnemyComponent.class);
}
