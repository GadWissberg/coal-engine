package com.gadarts.engine.systems.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.camera.CameraComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.components.position.PositionEventsSubscriber;
import com.gadarts.engine.systems.velocity.ForceEventSubscriber;
import com.gadarts.engine.systems.velocity.MovementForce;
import com.gadarts.engine.utils.C.Camera;

import static com.gadarts.engine.utils.Utils.applyDeltaOnStep;

public class PlayerSystem extends EntitySystem implements PositionEventsSubscriber, ForceEventSubscriber {
    private Entity player;
    private MovementForce strafeForce;
    private Vector3 auxVector = new Vector3();
    private PlayerController playerController;


    public void setStrafeForce(MovementForce strafeForce) {
        this.strafeForce = strafeForce;
        strafeForce.subscribeForForceEvents(this);
    }

    @Override
    public void update(float deltaTime) {
        PositionComponent playerPositionComponent = ComponentsMapper.position.get(player);
        handleCameraMotion(playerPositionComponent, deltaTime);
        handleCameraTilt(deltaTime);
        if (ComponentsMapper.player.get(player).isJumping()) {
            playerController.jump();
        }
        handleZoom();
    }

    private void handleZoom() {
        CameraComponent cameraComponent = ComponentsMapper.camera.get(player);
        float initialFov = cameraComponent.getInitialFov();
        float currentFov = cameraComponent.getFov();
        if (currentFov < initialFov) {
            zoom(cameraComponent, currentFov < initialFov - 0.5f, currentFov + Camera.ZOOM_SPEED);
        } else if (currentFov > initialFov) {
            zoom(cameraComponent, currentFov > initialFov + 0.5f, currentFov - Camera.ZOOM_SPEED);
        }
    }

    private void zoom(CameraComponent cameraComponent, boolean hasReachedInitial, float newFov) {
        cameraComponent.setFov(hasReachedInitial ? newFov : cameraComponent.getInitialFov());
        cameraComponent.updateCamera();
    }

    private void handleCameraTilt(float deltaTime) {
        HeadMotion headMotion = ComponentsMapper.camera.get(player).getHeadMotion();
        CameraComponent camComp = ComponentsMapper.camera.get(player);
        float tiltDelta = applyDeltaOnStep(headMotion.getTiltDelta(), deltaTime);
        Vector3 up = auxVector.set(camComp.getCameraUpX(), camComp.getCameraUpY(), camComp.getCameraUpZ());
        Vector3 left = up.crs(camComp.getCameraDirectionX(), camComp.getCameraDirectionY(), camComp.getCameraDirectionZ());
        float dot = left.dot(Vector3.Z);
        if (headMotion.isTilting()) tiltCamera(tiltDelta, camComp, dot);
        else fixCameraFromTilt(tiltDelta, camComp, left, dot);
    }

    private void fixCameraFromTilt(float tiltDelta, CameraComponent cameraComponent, Vector3 left, float dot) {
        if (!left.isPerpendicular(Vector3.Z, 0.01f)) {
            float cameraDirectionX = cameraComponent.getCameraDirectionX();
            float cameraDirectionY = cameraComponent.getCameraDirectionY();
            if (dot > 0) cameraComponent.rotateCamera(-tiltDelta, cameraDirectionX, cameraDirectionY, 0);
            else cameraComponent.rotateCamera(tiltDelta, cameraDirectionX, cameraDirectionY, 0);
            cameraComponent.updateCamera();
        }
    }

    private void tiltCamera(float tiltDelta, CameraComponent camComp, float dot) {
        float cameraDirectionX = camComp.getCameraDirectionX();
        float cameraDirectionY = camComp.getCameraDirectionY();
        if (strafeForce.getSpeed() < 0) {
            if (dot < Camera.TILT_MAX_DOT_PROD) camComp.rotateCamera(tiltDelta, cameraDirectionX, cameraDirectionY, 0);
        } else if (dot > -Camera.TILT_MAX_DOT_PROD)
            camComp.rotateCamera(-tiltDelta, cameraDirectionX, cameraDirectionY, 0);
        camComp.updateCamera();
    }

    private void handleCameraMotion(PositionComponent playerPc, float deltaTime) {
        MovementForce frontForce = ComponentsMapper.velocity.get(player).getFrontForce();
        CameraComponent cameraComponent = ComponentsMapper.camera.get(player);
        if (isForceAffectsPosition(frontForce) || isForceAffectsPosition(strafeForce))
            cameraComponent.setHeadMotion(-Camera.WALKING_SPEED, Camera.WALKING_MIN_Z, true, false);
        else cameraComponent.resetHeadMotion(cameraComponent);
        if (cameraComponent.getHeadMotion().isInMotion())
            moveHeadByMotion(playerPc, cameraComponent, deltaTime);
        else if (cameraComponent.getHeadRelativeZ() < playerPc.getBodyAltitude() - Camera.EYES_TO_TOP_OFFSET)
            handleReset(deltaTime);
    }

    private boolean isForceAffectsPosition(MovementForce force) {
        return force.isEnabled() && force.getSpeed() != 0;
    }

    private void handleReset(float deltaTime) {
        CameraComponent camComponent = ComponentsMapper.camera.get(player);
        PositionComponent playerPc = ComponentsMapper.position.get(player);
        float speedByFps = applyDeltaOnStep(Math.abs(camComponent.getHeadMotion().getSpeed()), deltaTime);
        float maxZ = playerPc.getBodyAltitude() - Camera.EYES_TO_TOP_OFFSET;
        float nextZ = Math.min(camComponent.getHeadRelativeZ() + speedByFps, maxZ);
        camComponent.setHeadRelativeZ(nextZ);
        onPositionChanged(playerPc.getX(), playerPc.getY(), playerPc.getZ(), deltaTime);
    }

    private void moveHeadByMotion(PositionComponent positionComponent, CameraComponent cameraComponent, float deltaTime) {
        HeadMotion headMotion = cameraComponent.getHeadMotion();
        float speed = headMotion.getSpeed();
        if (speed == 0) return;
        float relativeMinZ = headMotion.getRelativeTargetZ();
        float relativeMaxZ = positionComponent.getBodyAltitude() - Camera.EYES_TO_TOP_OFFSET;
        changeHeadZbyMotionWithBoundaries(cameraComponent, relativeMinZ, relativeMaxZ, deltaTime);
        onPositionChanged(positionComponent.getX(), positionComponent.getY(), positionComponent.getZ(), deltaTime);
    }

    private void changeHeadZbyMotionWithBoundaries(CameraComponent cameraComponent, float relativeMinZ, float relativeMaxZ,
                                                   float deltaTime) {
        float speedByFps = applyDeltaOnStep(cameraComponent.getHeadMotion().getSpeed(), deltaTime);
        float nextZ = cameraComponent.getHeadRelativeZ() + speedByFps;
        if (nextZ <= relativeMinZ) handleReverse(cameraComponent, relativeMinZ);
        else if (nextZ >= relativeMaxZ) handleReverse(cameraComponent, relativeMaxZ);
        else cameraComponent.setHeadRelativeZ(nextZ);
    }

    private void handleReverse(CameraComponent cameraComponent, float zBoundary) {
        cameraComponent.setHeadRelativeZ(zBoundary);
        if (cameraComponent.getHeadMotion().shouldAddReverse()) {
            if (!cameraComponent.getHeadMotion().isInReverse()) reverseCameraMotion(cameraComponent.getHeadMotion());
            else {
                cameraComponent.disableForcedMotion();
                cameraComponent.getHeadMotion().setInMotion(false);
            }
        }
    }

    private void reverseCameraMotion(HeadMotion headMotion) {
        headMotion.setInReverseState(true);
        headMotion.setSpeed(-headMotion.getSpeed());
    }

    @Override
    public void onPositionChanged(float x, float y, float z, float delta) {
        CameraComponent cameraComponent = ComponentsMapper.camera.get(player);
        cameraComponent.setCameraPosition(x, y, z + cameraComponent.getHeadRelativeZ());
        cameraComponent.updateCamera();
    }

    @Override
    public void onCollisionWithNonPassableLine() {

    }

    @Override
    public void onLanding(float fallingSpeedOnLanding) {
        if (fallingSpeedOnLanding > 3) {
            ComponentsMapper.camera.get(player).setHeadMotion(-Camera.LANDING_SPEED, Camera.LANDING_MIN_Z, true, true);
        }
        if (strafeForce.isEnabled()) {
            HeadMotion headMotion = ComponentsMapper.camera.get(player).getHeadMotion();
            headMotion.tilt(Camera.TILT_DEGREES_DELTA);
        }
    }

    @Override
    public void onCeilingCollision() {
        ComponentsMapper.velocity.get(player).getGravityForce().setSpeed(0);
    }

    @Override
    public void onForceStateChange(boolean status) {
        HeadMotion headMotion = ComponentsMapper.camera.get(player).getHeadMotion();
        if (status && ComponentsMapper.position.get(player).isOnGround()) {
            headMotion.tilt(Camera.TILT_DEGREES_DELTA);
        } else {
            headMotion.stopTilt();
        }
    }

    public void setPlayer(Entity playerEntity) {
        player = playerEntity;
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }
}
