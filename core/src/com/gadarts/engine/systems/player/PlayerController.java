package com.gadarts.engine.systems.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.ActorComponent;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.VelocityComponent;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.components.position.PositionEventsSubscriber;
import com.gadarts.engine.systems.velocity.MovementForce;
import com.gadarts.engine.utils.C;

import java.util.ArrayList;

public class PlayerController implements PositionEventsSubscriber {
    private final Entity playerEntity;
    private final VelocityComponent velocityComponent;
    private final PositionComponent positionComponent;
    private MovementForce strafeForce;
    private final ActorComponent actorComponent;
    private CameraComponent cameraComponent;
    private Vector2 auxVector = new Vector2();
    private ArrayList<PositionEventsSubscriber> positionEventsSubscribers = new ArrayList<PositionEventsSubscriber>();

    public PlayerController(Entity playerEntity, PlayerSystem system) {
        this.playerEntity = playerEntity;
        this.cameraComponent = ComponentsMapper.camera.get(playerEntity);
        this.velocityComponent = ComponentsMapper.velocity.get(playerEntity);
        this.positionComponent = ComponentsMapper.position.get(playerEntity);
        this.actorComponent = ComponentsMapper.actor.get(playerEntity);
        initialize(system);
    }

    public void initiateZoomSequence(float zoom) {
        float initialFov = cameraComponent.getInitialFov();
        cameraComponent.setFov(MathUtils.clamp(cameraComponent.getFov() + zoom, initialFov - 6, initialFov + 6));
    }

    public float getCameraUpX() {
        return cameraComponent.getCameraUpX();
    }

    public float setCameraUpX(float x) {
        return cameraComponent.setCameraUpX(x);
    }

    public float setCameraUpY(float y) {
        return cameraComponent.setCameraUpY(y);
    }

    public void rotateCamera(Vector3 axis, float angle) {
        cameraComponent.rotateCamera(axis, angle);
    }

    public void updateCamera() {
        cameraComponent.updateCamera();
    }

    public float setCameraUpZ(float z) {
        return cameraComponent.setCameraUpZ(z);
    }

    public float getCameraDirectionX() {
        return cameraComponent.getCameraDirectionX();
    }

    public float getCameraDirectionY() {
        return cameraComponent.getCameraDirectionY();
    }

    public Vector3 getCameraDirection(Vector3 result) {
        return cameraComponent.getCameraDirection(result);
    }

    public float getCameraDirectionZ() {
        return cameraComponent.getCameraDirectionZ();
    }

    public float getCameraUpY() {
        return cameraComponent.getCameraUpY();
    }

    public float getCameraUpZ() {
        return cameraComponent.getCameraUpZ();
    }

    private void initialize(PlayerSystem system) {
        system.setPlayerController(this);
        strafeForce = velocityComponent.getForce(C.PLAYER_STRAFE_FORCE_NAME);
        positionComponent.subscribeForPositionEvents(this);
        updateRunningDirection();
    }


    public void subscribeForPositionEvents(PositionEventsSubscriber subscriber) {
        if (!positionEventsSubscribers.contains(subscriber)) {
            positionEventsSubscribers.add(subscriber);
        }
    }

    public void unsubscribeForPositionEvents(PositionEventsSubscriber subscriber) {
        positionEventsSubscribers.remove(subscriber);
    }

    public Long getId() {
        return actorComponent.getId();
    }

    public boolean isOnGround() {
        return positionComponent.isOnGround();
    }

    public void setJumping(boolean jumping) {
        ComponentsMapper.player.get(playerEntity).setJumping(jumping);
    }

    public void jump() {
        if (isOnGround() && !cameraComponent.isInForcedMotion()) {
            MovementForce gravityForce = velocityComponent.getGravityForce();
            gravityForce.setSpeed(-ComponentsMapper.player.get(playerEntity).getJumpSpeed());
            gravityForce.setAcceleration((float) (0.2/*Will be replaced by gravity&weight-dependent value*/));
        }
    }

    public float getMovementAltitudeSpeed() {
        return ComponentsMapper.player.get(playerEntity).getMovementAltitudeSpeed();
    }

    public float getZ() {
        return positionComponent.getZ();
    }

    public Vector3 getPosition(Vector3 vector3) {
        return positionComponent.getPosition(vector3);
    }

    public boolean isRunning() {
        return velocityComponent.getFrontForce().isEnabled();
    }

    public boolean isStrafing() {
        return strafeForce.isEnabled();
    }

    public void setRunning(boolean v) {
        velocityComponent.getFrontForce().setEnabled(v);
    }

    public void setStrafing(boolean v) {
        strafeForce.setEnabled(v);
    }

    public void setRunningAcceleration(float v) {
        velocityComponent.getFrontForce().setAcceleration(v);
    }

    public void setStrafingAcceleration(float v) {
        strafeForce.setAcceleration(v);
    }

    public float getRunningAcceleration() {
        return velocityComponent.getFrontForce().getAcceleration();
    }

    public float getStrafingAcceleration() {
        return strafeForce.getAcceleration();
    }

    public void updateRunningDirection() {
        Vector2 twoDimDirection = auxVector;
        float cameraDirectionX = cameraComponent.getCameraDirectionX();
        float cameraDirectionY = cameraComponent.getCameraDirectionY();
        twoDimDirection.set(cameraDirectionX, cameraDirectionY);
        twoDimDirection.nor();
        velocityComponent.getFrontForce().setDirection(twoDimDirection.x, twoDimDirection.y, 0);
        strafeForce.setDirection(cameraDirectionX, cameraDirectionY, 0);
        strafeForce.getDirection().rotate(Vector3.Z, 90);
    }

    @Override
    public void onPositionChanged(float x, float y, float z, float delta) {
        for (PositionEventsSubscriber subscriber : positionEventsSubscribers) {
            subscriber.onPositionChanged(x, y, z, delta);
        }
    }

    @Override
    public void onCollisionWithNonPassableLine() {
        for (PositionEventsSubscriber subscriber : positionEventsSubscribers) {
            subscriber.onCollisionWithNonPassableLine();
        }
    }

    @Override
    public void onLanding(float fallingSpeedOnLanding) {
        for (PositionEventsSubscriber subscriber : positionEventsSubscribers) {
            subscriber.onLanding(fallingSpeedOnLanding);
        }
    }

    @Override
    public void onCeilingCollision() {
        for (PositionEventsSubscriber subscriber : positionEventsSubscribers) {
            subscriber.onCeilingCollision();
        }
    }
}
