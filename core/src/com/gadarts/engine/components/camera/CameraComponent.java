package com.gadarts.engine.components.camera;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.gadarts.engine.systems.player.HeadMotion;

import java.util.ArrayList;

public class CameraComponent implements Component, Pool.Poolable {
    private PerspectiveCamera camera;
    private float headRelativeZ;
    private HeadMotion headMotion = new HeadMotion();
    private boolean forcedMotion;
    private ArrayList<CameraEventsSubscriber> cameraEventsSubscribers;
    private float initialFov;

    public float getInitialFov() {
        return initialFov;
    }

    public void setInitialFov(float initialFov) {
        this.initialFov = initialFov;
    }

    public void setCamera(PerspectiveCamera camera) {
        this.camera = camera;
    }

    public float getHeadRelativeZ() {
        return headRelativeZ;
    }

    public void setHeadRelativeZ(float headRelativeZ) {
        this.headRelativeZ = headRelativeZ;
    }

    @Override
    public void reset() {

    }

    public void setHeadMotion(float speed, float relativeTargetZ, boolean addReverse, boolean force) {
        if (!headMotion.isInMotion() || force) {
            forcedMotion = force;
            headMotion.setInMotion(true);
            headMotion.setSpeed(speed);
            headMotion.setRelativeTargetZ(relativeTargetZ);
            headMotion.setAddReverse(addReverse);
            headMotion.setInReverseState(false);
        }
    }

    public HeadMotion getHeadMotion() {
        return headMotion;
    }

    public void resetHeadMotion(CameraComponent cameraComponent) {
        if (headMotion.isInMotion() && !cameraComponent.isInForcedMotion()) {
            headMotion.setInMotion(false);
        }
    }

    public boolean isInForcedMotion() {
        return forcedMotion;
    }

    public void disableForcedMotion() {
        forcedMotion = false;
    }

    public void subscribeForCameraEvents(CameraEventsSubscriber cameraEventsSubscriber) {
        if (this.cameraEventsSubscribers == null) cameraEventsSubscribers = new ArrayList<CameraEventsSubscriber>();
        if (!cameraEventsSubscribers.contains(cameraEventsSubscriber)) {
            cameraEventsSubscribers.add(cameraEventsSubscriber);
        }
    }

    public void updateCamera() {
        camera.update();
    }

    public void setCameraPosition(float x, float y, float z) {
        Vector3 position = camera.position;
        if (position.x != x || position.y != y || position.z != z) {
            position.set(x, y, z);
            if (cameraEventsSubscribers != null) {
                for (CameraEventsSubscriber subscriber : cameraEventsSubscribers)
                    subscriber.onCameraPositionChange(x, y, z);
            }
        }
    }

    public float getFov() {
        return camera.fieldOfView;
    }

    public void setFov(float fov) {
        camera.fieldOfView = fov;
    }

    public float getCameraUpX() {
        return camera.up.x;
    }

    public float setCameraUpX(float x) {
        return camera.up.x = x;
    }

    public float setCameraUpY(float y) {
        return camera.up.y = y;
    }

    public float setCameraUpZ(float z) {
        return camera.up.z = z;
    }

    public float getCameraUpY() {
        return camera.up.y;
    }

    public float getCameraUpZ() {
        return camera.up.z;
    }

    public float getCameraDirectionX() {
        return camera.direction.x;
    }

    public float getCameraDirectionY() {
        return camera.direction.y;
    }

    public float getCameraDirectionZ() {
        return camera.direction.z;
    }

    public float getCameraPositionX() {
        return camera.position.x;
    }

    public float getCameraPositionY() {
        return camera.position.y;
    }

    public float getCameraPositionZ() {
        return camera.position.z;
    }

    public void rotateCamera(Vector3 axis, float angle) {
        camera.rotate(axis, angle);
    }

    public void rotateCamera(float angle, float axisX, float axisY, float axisZ) {
        camera.rotate(angle, axisX, axisY, axisZ);
    }

    public CameraInputController createCameraInputController() {
        return new CameraInputController(camera);
    }

    public Vector3 getCameraDirection(Vector3 result) {
        return result.set(camera.direction);
    }
}
