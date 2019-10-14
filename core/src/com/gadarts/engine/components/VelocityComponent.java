package com.gadarts.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.engine.systems.velocity.MovementForce;

import java.util.ArrayList;

public class VelocityComponent implements Component, Pool.Poolable {

    private MovementForce frontForce = Pools.obtain(MovementForce.class);
    private MovementForce gravityForce = Pools.obtain(MovementForce.class);
    private ArrayList<MovementForce> otherForces;
    private float maxStepAltitude;
    private float raiseSpeed;

    public VelocityComponent() {
        super();
        frontForce.init(1, 0, 0);
        gravityForce.init(0, 0, -1);
        initializeGravityForce();
    }

    @Override
    public void reset() {
        if (otherForces != null) otherForces.clear();
        frontForce.reset();
        gravityForce.reset();
        initializeGravityForce();
    }

    private void initializeGravityForce() {
        gravityForce.setAcceleration(0.2f);
        gravityForce.setEnabled(true);
        gravityForce.setMaxSpeed(24);
        gravityForce.setMinSpeed(0);
    }

    public float getRaiseSpeed() {
        return raiseSpeed;
    }

    public MovementForce getFrontForce() {
        return frontForce;
    }

    public float getMaxStepAltitude() {
        return maxStepAltitude;
    }

    public MovementForce getGravityForce() {
        return gravityForce;
    }

    public void addForce(MovementForce force) {
        if (otherForces == null) {
            otherForces = new ArrayList<MovementForce>();
        }
        otherForces.add(force);
    }

    public MovementForce getForce(String name) {
        MovementForce answer = null;
        for (MovementForce force : otherForces) {
            if (force.getName().equals(name)) {
                answer = force;
                break;
            }
        }
        return answer;
    }

    public ArrayList<MovementForce> getOtherForces() {
        return otherForces;
    }

    public void setMaxStepAltitude(float maxStepAltitude) {
        this.maxStepAltitude = maxStepAltitude;
    }

    public void setRaiseSpeed(float raiseSpeed) {
        this.raiseSpeed = raiseSpeed;
    }

    public void removeForce(MovementForce force) {
        otherForces.remove(force);
    }
}
