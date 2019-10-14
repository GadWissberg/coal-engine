package com.gadarts.engine.systems.velocity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.Level;
import com.gadarts.engine.components.ActorComponent;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.VelocityComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.elements.Sector;
import com.gadarts.engine.utils.Utils;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

import static com.gadarts.engine.utils.Utils.applyDeltaOnStep;

public class VelocitySystem extends EntitySystem {
    private Level currentLevel;
    private ImmutableArray<Entity> movingEntities;
    private Vector3 nextPositionVector = new Vector3();
    private Envelope auxEnvelope = new Envelope();
    private CollisionCalculator collisionCalculator = new CollisionCalculator();
    private Vector2 auxVector2_1 = new Vector2();
    private Vector2 auxVector2_2 = new Vector2();
    private ImmutableArray<Entity> actors;
    private Vector2 displacement = new Vector2();

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        movingEntities = engine.getEntitiesFor(Family.all(VelocityComponent.class).get());
        actors = engine.getEntitiesFor(Family.all(ActorComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : movingEntities) {
            VelocityComponent velocityComponent = ComponentsMapper.velocity.get(entity);
            PositionComponent positionComponent = ComponentsMapper.position.get(entity);
            handleForcesMovement(entity, deltaTime, positionComponent, velocityComponent);
        }
    }

    private void handleForcesMovement(Entity entity, float deltaTime, PositionComponent positionComponent,
                                      VelocityComponent velocityComponent) {
        handleDefaultForces(deltaTime, positionComponent, velocityComponent, entity);
        ArrayList<MovementForce> forces = velocityComponent.getOtherForces();
        if (forces != null) {
            for (int i = 0; i < forces.size(); i++) {
                MovementForce force = forces.get(i);
                handleForceMovement(deltaTime, force, entity);
            }
        }
    }

    private void handleDefaultForces(float deltaTime, PositionComponent positionComponent,
                                     VelocityComponent velocityComponent, Entity entity) {
        handleForceMovement(deltaTime, velocityComponent.getFrontForce(), entity);
        MovementForce gravityForce = velocityComponent.getGravityForce();
        handleForceMovement(deltaTime, gravityForce, entity);
        setGravityAcceleration(positionComponent, gravityForce);
    }

    private void setGravityAcceleration(PositionComponent positionComponent, MovementForce gravityForce) {
        if (!gravityForce.isEnabled() || positionComponent.isOnGround()) {
            gravityForce.setAcceleration(0);
            gravityForce.setSpeed(0);
        } else {
            gravityForce.setAcceleration(0.4f/*Will be replaced by gravity&weight-dependent value*/);
        }
    }

    private void handleForceMovement(float deltaTime, MovementForce force, Entity entity) {
        float stepSize = calculateStepSize(force, entity);
        force.setSpeed(stepSize);
        if (stepSize == 0) return;
        takeStep(applyDeltaOnStep(stepSize, deltaTime), force, entity, deltaTime);
    }

    private void takeStep(float stepSize, MovementForce force, Entity entity, float deltaTime) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        pc.getPosition(nextPositionVector);
        calculateNextPosition(stepSize, force);
        handleStepXYZ(force, entity, pc, deltaTime);
    }

    private void handleStepXYZ(MovementForce force, Entity entity, PositionComponent pc, float deltaTime) {
        if (nextPositionVector.x == pc.getX() && nextPositionVector.y == pc.getY() && nextPositionVector.z == pc.getZ()) {
            force.setSpeed(0);
        } else {
            handleCollisions(entity, force, deltaTime);
            float prevZ = pc.getZ();
            applyNextStep(entity, deltaTime, prevZ);
        }
    }

    private void handleCollisions(Entity entity, MovementForce force, float deltaTime) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        if (ComponentsMapper.collisionsEvents.has(entity)) {
            handleCollisionsWithActors(entity, pc);
        }
        handleCollisionsWithLines(entity, pc, force, deltaTime);
    }

    private void handleCollisionsWithActors(Entity entity, PositionComponent pc) {
        for (Entity other : actors) {
            if (entity == other) continue;
            PositionComponent otherPc = ComponentsMapper.position.get(other);
            if (collisionCalculator.checkVelocityVectorCollision(pc, nextPositionVector, otherPc)
                    || collisionCalculator.checkPositionCollision(nextPositionVector, pc.getRadius(), otherPc)) {
                ComponentsMapper.collisionsEvents.get(entity).executeActorCollisionEvent(entity, other, nextPositionVector);
            }
        }
    }

    private void handleCollisionsWithLines(Entity entity, PositionComponent pc, MovementForce force,
                                           float deltaTime) {
        float speed = Utils.applyDeltaOnStep(force.getSpeed(), deltaTime);
        float x = pc.getX(), y = pc.getY(), movementRadius = pc.getRadius() + Math.abs(speed);
        auxEnvelope.init(x - movementRadius, x + movementRadius, y - movementRadius, y + movementRadius);
        List<Line> nearestLines = currentLevel.getLines().getQuadTree().query(auxEnvelope);
        float angleDestToCurrentRad = MathUtils.atan2(nextPositionVector.y - pc.getY(), nextPositionVector.x - pc.getX());
        Line collided = null;
        for (Line line : nearestLines) {
            if (!isOkToMoveToNextSector(line, entity)) {
                Line result = handleVelocityVectorCollisionWithNonPassableLine(entity, line, angleDestToCurrentRad);
                collided = result != null ? result : collided;
            }
        }

        for (Line line : nearestLines) {
            if (!isOkToMoveToNextSector(line, entity)) {
                Line result = handleRadiusCollisionWithNonPassableLine(pc, line);
                collided = result != null ? result : collided;
            }
        }

        if (collided != null) {
            pc.collisionWithNonPassableLine();
            if (ComponentsMapper.collisionsEvents.has(entity)) {
                ComponentsMapper.collisionsEvents.get(entity).executeBlockingLineCollisionEvent(collided, entity);
            }
        }

        float maxDistance = 0;
        Line furthestCollidedPassableLine = null;
        for (Line line : nearestLines) {
            if (isOkToMoveToNextSector(line, entity)) {
                float distance = handleVelocityVectorCollisionWithPassableLine(entity, line);
                if (distance >= 0 && distance > maxDistance) {
                    maxDistance = distance;
                    furthestCollidedPassableLine = line;
                }
            }
        }
        if (furthestCollidedPassableLine != null) {
            if (Intersector.pointLineSide(furthestCollidedPassableLine.getSrc().getX(), furthestCollidedPassableLine.getSrc().getY(),
                    furthestCollidedPassableLine.getDst().getX(), furthestCollidedPassableLine.getDst().getY(), nextPositionVector.x, nextPositionVector.y) == 1) {
                pc.setCurrentSectorId(furthestCollidedPassableLine.getFrontSectorId());
            } else {
                pc.setCurrentSectorId(furthestCollidedPassableLine.getBackSectorId());
            }
        }
        float maxFloorAltitude = -1;
        float minCeilingAltitude = Float.MAX_VALUE;
        for (Line line : nearestLines) {
            if (isOkToMoveToNextSector(line, entity)) {
                float distance = collisionCalculator.checkPositionCollisionWithMtv(line, nextPositionVector, pc.getRadius(), displacement);
                if (distance < Float.POSITIVE_INFINITY) {
                    Sector backSector = (Sector) currentLevel.getSectors().get(line.getBackSectorId());
                    Sector frontSector = (Sector) currentLevel.getSectors().get(line.getFrontSectorId());
                    maxFloorAltitude = Math.max(backSector.getFloorAltitude(), maxFloorAltitude);
                    maxFloorAltitude = Math.max(frontSector.getFloorAltitude(), maxFloorAltitude);
                    minCeilingAltitude = Math.min(frontSector.getCeilingAltitude(), minCeilingAltitude);
                    minCeilingAltitude = Math.min(backSector.getCeilingAltitude(), minCeilingAltitude);
                }
            }
        }
        if (maxFloorAltitude >= 0) {
            pc.setCurrentFloorAltitude(maxFloorAltitude);
        } else {
            Sector currentSector = (Sector) currentLevel.getSectors().get(pc.getCurrentSectorId());
            pc.setCurrentFloorAltitude(currentSector.getFloorAltitude());
        }
        if (minCeilingAltitude < Float.MAX_VALUE) {
            pc.setCurrentCeilingAltitude(minCeilingAltitude);
        } else {
            Sector currentSector = (Sector) currentLevel.getSectors().get(pc.getCurrentSectorId());
            pc.setCurrentCeilingAltitude(currentSector.getCeilingAltitude());
        }
    }

    private Line handleRadiusCollisionWithNonPassableLine(PositionComponent pc, Line line) {
        float distance = collisionCalculator.checkPositionCollisionWithMtv(line, nextPositionVector, pc.getRadius(), displacement);
        if (distance < Float.POSITIVE_INFINITY) {
            nextPositionVector.add(displacement.x * distance, displacement.y * distance, 0);
            return line;
        }
        return null;
    }

    private Line handleVelocityVectorCollisionWithNonPassableLine(Entity entity, Line line, float angleCurrentToDestRad) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        Vector2 intersection = this.auxVector2_1;
        if (collisionCalculator.checkVelocityVectorCollision(pc, nextPositionVector, line, intersection)) {
            auxVector2_2.setAngleRad(angleCurrentToDestRad);
            intersection.add(auxVector2_2.x * pc.getRadius(), auxVector2_2.y * pc.getRadius());
            nextPositionVector.set(intersection, nextPositionVector.z);
            return line;
        }
        return null;
    }

    private float handleVelocityVectorCollisionWithPassableLine(Entity entity, Line line) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        Vector2 intersection = this.auxVector2_1;
        if (collisionCalculator.checkVelocityVectorCollision(pc, nextPositionVector, line, intersection)) {
            return intersection.dst(pc.getX(), pc.getY());
        }
        return -1;
    }

    private boolean isOkToMoveToNextSector(Line line, Entity entity) {
        if (line.getBackSectorId() < 0 || line.isSolid()) return false;
        Sector frontSector = (Sector) currentLevel.getSectors().get(line.getFrontSectorId());
        Sector backSector = (Sector) currentLevel.getSectors().get(line.getBackSectorId());
        VelocityComponent vc = ComponentsMapper.velocity.get(entity);
        PositionComponent pc = ComponentsMapper.position.get(entity);
        if (Intersector.pointLineSide(line.getSrc().getX(), line.getSrc().getY(),
                line.getDst().getX(), line.getDst().getY(), pc.getX(), pc.getY()) == 1)
            return (backSector.getFloorAltitude() - vc.getMaxStepAltitude() < pc.getZ() && backSector.getCeilingAltitude() > pc.getZ() + pc.getBodyAltitude());
        else
            return pc.getZ() > frontSector.getFloorAltitude() - vc.getMaxStepAltitude() && pc.getZ() + pc.getBodyAltitude() < frontSector.getCeilingAltitude();
    }

    private void applyNextStep(Entity entity, float deltaTime, float prevZ) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        VelocityComponent velocityComponent = ComponentsMapper.velocity.get(entity);
        float currentFloorAltitude = pc.getCurrentFloorAltitude();
        if (prevZ > currentFloorAltitude && nextPositionVector.z < currentFloorAltitude) {
            pc.landed(velocityComponent.getGravityForce().getSpeed());
            if (ComponentsMapper.collisionsEvents.has(entity)) {
                ComponentsMapper.collisionsEvents.get(entity).executeSurfaceCollisionEvent();
            }
        }
        float nextZ;
        if (nextPositionVector.z < currentFloorAltitude - velocityComponent.getMaxStepAltitude()) {
            nextZ = currentFloorAltitude;
        } else if (nextPositionVector.z < currentFloorAltitude) {
            float verticalStep = applyDeltaOnStep(velocityComponent.getRaiseSpeed(), deltaTime);
            float newZ = prevZ + verticalStep;
            nextZ = currentFloorAltitude < newZ ? currentFloorAltitude : newZ;
        } else {
            nextZ = nextPositionVector.z;
        }
        float bodyAltitude = pc.getBodyAltitude();
        if (nextPositionVector.z + bodyAltitude >= pc.getCurrentCeilingAltitude()) {
            nextZ = pc.getCurrentCeilingAltitude() - bodyAltitude;
            pc.ceilingCollided();
            if (ComponentsMapper.collisionsEvents.has(entity)) {
                ComponentsMapper.collisionsEvents.get(entity).executeSurfaceCollisionEvent();
            }
        }
        pc.setPosition(nextPositionVector.x, nextPositionVector.y, nextZ);
    }


    private void calculateNextPosition(float stepSize, MovementForce force) {
        Vector3 direction = force.getDirection();
        nextPositionVector.add(stepSize * direction.x, stepSize * direction.y, stepSize * direction.z);
    }

    private float calculateStepSize(MovementForce f, Entity entity) {
        float currentSpeed = f.getSpeed(), maxSpeed = f.getMaxSpeed(), acc = f.getAcceleration(), newSpeed = currentSpeed;
        if (f.isEnabled() && acc != 0) {
            if (!f.isAcceleratingOnGroundOnly() || ComponentsMapper.position.get(entity).isOnGround())
                newSpeed = currentSpeed + acc;
        } else {
            newSpeed = calculateStepForDisabledForce(entity, currentSpeed, acc);
        }
        return MathUtils.clamp(newSpeed, -maxSpeed, maxSpeed);
    }

    private float calculateStepForDisabledForce(Entity entity, float currentSpeed, float acc) {
        float newSpeed;
        if (ComponentsMapper.position.get(entity).isOnGround())
            newSpeed = acc > Math.abs(currentSpeed) ? 0 : currentSpeed - (currentSpeed > 0 ? 1 : -1) * acc;
        else newSpeed = currentSpeed;
        return newSpeed;
    }

    public void init(Level level) {
        this.currentLevel = level;
    }
}
