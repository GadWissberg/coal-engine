package com.gadarts.engine.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.engine.Level;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.VelocityComponent;
import com.gadarts.engine.components.enemies.EnemyAnimation;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.elements.Sector;
import com.gadarts.engine.systems.enemy.AiOrder;
import com.gadarts.engine.systems.velocity.MovementForce;
import com.gadarts.engine.utils.Utils;
import com.vividsolutions.jts.geom.Envelope;
import elements.VertexElement;
import utils.SharedUtils;

import java.util.List;

public class EnemySystem extends EntitySystem {
    private Entity playerEntity;
    private Level level;
    private ImmutableArray<Entity> enemies;
    private Vector2 auxVector = new Vector2();
    private Envelope auxEnvelope = new Envelope();

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : enemies) {
            EnemyComponent enemyComponent = ComponentsMapper.enemy.get(entity);
            behaveEnemy(entity, enemyComponent, deltaTime);
        }
    }

    private void behaveEnemy(Entity entity, EnemyComponent enemyComponent,
                             float deltaTime) {
        EnemyComponent.Status status = enemyComponent.getStatus();
        if (enemyComponent.getHp() <= 0 && !status.equals(EnemyComponent.Status.DEAD)) {
            die(entity, deltaTime);
        }
        if (status.equals(EnemyComponent.Status.IDLE))
            behaveIdle(entity);
        else if (status.equals(EnemyComponent.Status.RUN_TO_TARGET))
            behaveRunToTarget(entity, deltaTime);
        else if (status.equals(EnemyComponent.Status.SEARCH_TARGET))
            behaveSearchTarget(entity);
        else if (status.equals(EnemyComponent.Status.ATTACK)) {
            behaveAttack(entity);
        } else if (status.equals(EnemyComponent.Status.SUFFER)) {
            behaveSuffer(entity);
        }
    }

    private void behaveSuffer(Entity entity) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        if (ec.getLastSufferTime() + 1500 <= TimeUtils.millis()) {
            startSearchingTarget(entity);
        }
    }

    private void die(Entity enemyEntity, float deltaTime) {
        EnemyComponent ec = ComponentsMapper.enemy.get(enemyEntity);
        ec.setStatus(EnemyComponent.Status.DEAD);
        ec.setCurrentAnimation(EnemyComponent.FrameType.DIE);
        ec.setAnimationLoop(false);
        ec.setStateTime(0);
        MovementForce frontForce = ComponentsMapper.velocity.get(enemyEntity).getFrontForce();
        frontForce.setEnabled(false);
        frontForce.setAcceleration(4);
    }

    private void behaveAttack(Entity entity) {
        EnemyComponent enemyComponent = ComponentsMapper.enemy.get(entity);
        PositionComponent pc = ComponentsMapper.position.get(entity);
        EnemyAnimation<TextureRegion> currentAnimation = enemyComponent.getCurrentAnimation();
        float stateTime = enemyComponent.getStateTime();
        if (currentAnimation.isAnimationFinished(stateTime)) {
            PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
            if (auxVector.set(pc.getX(), pc.getY()).dst2(playerPc.getX(), playerPc.getY()) > pc.getRadius() + playerPc.getRadius()) {
                startRunningToTarget(entity);
            } else {
                if (!isOnSight(calculateDirectionToPlayer(pc), enemyComponent.getDefinitionComponent().getFov() / 2, enemyComponent.getDirection())) {
                    startRunningToTarget(entity);
                }
            }
        }
    }

    private void behaveSearchTarget(Entity entity) {
        PositionComponent positionComponent = ComponentsMapper.position.get(entity);
        float dirToPlayer = calculateDirectionToPlayer(positionComponent);
        EnemyComponent enemyComponent = ComponentsMapper.enemy.get(entity);
        float fov = enemyComponent.getDefinitionComponent().getFov();
        if (isTargetOnSight(entity, dirToPlayer, fov, enemyComponent.getDirection()))
            startRunningToTarget(entity);
        else handleSearchingTurning(enemyComponent);
    }

    private void handleSearchingTurning(EnemyComponent enemyComponent) {
        long millis = TimeUtils.millis();
        if (millis > enemyComponent.getLastSearchingTurnTime() + ((long) (2000 * MathUtils.random()))) {
            float turn = (MathUtils.random() > 0.5 ? 1 : -1) * MathUtils.random() * 90;
            enemyComponent.setDirection(enemyComponent.getDirection() + turn);
            enemyComponent.setLastSearchingTurnTime(millis);
        }
    }

    private void behaveRunToTarget(Entity entity, float deltaTime) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        if (ec.isOrderInProcess()) {
            AiOrder currentOrder = ec.getCurrentOrder();
            if (TimeUtils.millis() - currentOrder.getBeganTime() >= currentOrder.getDuration()) {
                ec.finishOrder();
            }
        } else {
            Queue<AiOrder> orders = ec.getOrders();
            if (orders.isEmpty()) {
                PositionComponent pc = ComponentsMapper.position.get(entity);
                float dirToPlayer = calculateDirectionToPlayer(pc);
                boolean cantSeeTarget = !isTargetOnSight(entity, dirToPlayer, ec.getDefinitionComponent().getFov(), ec.getDirection());
                if (cantSeeTarget) behaveRunToTargetIfCantSeeIt(entity, deltaTime, dirToPlayer);
                else behaveRunToTargetIfSeesIt(entity, deltaTime, dirToPlayer);
            } else {
                Gdx.app.log("!", "!");
                AiOrder order = orders.removeFirst();
                ec.beginOrderProcess(order);
            }
        }
    }

    private void behaveRunToTargetIfSeesIt(Entity entity, float deltaTime, float dirToPlayer) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        Vector2 lastTargetPos = ComponentsMapper.enemy.get(entity).getLastTargetPosition();
        float distanceToTarget = auxVector.set(pc.getX(), pc.getY()).dst2(lastTargetPos.x, lastTargetPos.y);
        PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
        if (distanceToTarget > 1.5f) {
            runToTarget(entity, deltaTime, dirToPlayer, false);
        } else if (distanceToTarget <= 1.5f && distanceToTarget > pc.getRadius() + playerPc.getRadius()) {
            runToTarget(entity, deltaTime, dirToPlayer, true);
        } else {
            startAttack(entity, dirToPlayer);
        }
    }

    private void startAttack(Entity entity, float dirToPlayer) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        ec.setDirection(dirToPlayer);
        ec.setStatus(EnemyComponent.Status.ATTACK);
        ec.setFrameDuration(0.2f);
        VelocityComponent vc = ComponentsMapper.velocity.get(entity);
        vc.getFrontForce().setEnabled(false);
        ec.setCurrentAnimation(EnemyComponent.FrameType.ATTACK);
        ec.setStateTime(0);
        ec.setAnimationLoop(true);
    }

    private void behaveRunToTargetIfCantSeeIt(Entity entity, float deltaTime, float dirToPlayer) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        Vector2 lastTargetPos = ec.getLastTargetPosition();
        boolean isNearLastTargetPos = auxVector.set(pc.getX(), pc.getY()).dst2(lastTargetPos.x, lastTargetPos.y) < 0.5f;
        if (isNearLastTargetPos) startSearchingTarget(entity);
        else runToTarget(entity, deltaTime, dirToPlayer, true);
    }

    private void runToTarget(Entity entity, float deltaTime, float dirToPlayer, boolean directly) {
        updateLastTargetPositionIfNeeded(entity, dirToPlayer);
        takeStep(entity, deltaTime, directly);
    }

    private float calculateDirectionToPlayer(PositionComponent pc) {
        PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
        return auxVector.set(playerPc.getX(), playerPc.getY()).sub(pc.getX(), pc.getY()).nor().angle();
    }

    private void takeStep(Entity entity, float deltaTime, boolean directly) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        PositionComponent pc = ComponentsMapper.position.get(entity);
        Vector2 lastTargetPos = ec.getLastTargetPosition();
        float man = !directly ? updateLastManeuverTimeIfNeeded(ec) : 0;
        float dirToLastPosition = auxVector.set(lastTargetPos).sub(pc.getX(), pc.getY()).angle() + (!directly ? man : 0);
        rotateAccordingToStep(deltaTime, directly, ec, dirToLastPosition);
    }

    private void rotateAccordingToStep(float deltaTime, boolean directly, EnemyComponent ec, float dirToLastPosition) {
        float enemyDirRad = ec.getDirection() * MathUtils.degreesToRadians;
        float dirToTargetRad = dirToLastPosition * MathUtils.degreesToRadians;
        float progressRad = MathUtils.lerpAngle(enemyDirRad, dirToTargetRad, Utils.applyDeltaOnStep(2, deltaTime));
        ec.setDirection(directly ? MathUtils.radiansToDegrees * progressRad : dirToLastPosition);
    }

    private float updateLastManeuverTimeIfNeeded(EnemyComponent ec) {
        long millis = TimeUtils.millis();
        float man = ec.getManeuverAngle();
        if (millis > ec.getLastManeuverTime() + 2000 + ((long) (MathUtils.random() * 3000))) {
            man = updateLastManeuverTime(ec, millis);
        }
        return man;
    }

    private float updateLastManeuverTime(EnemyComponent ec, long millis) {
        float man;
        ec.setLastManeuverTime(millis);
        float fov = ec.getDefinitionComponent().getFov();
        man = (MathUtils.random() > 0.5 ? 1 : -1) * (MathUtils.random() * fov / 2);
        ec.setManeuverAngle(man);
        return man;
    }

    private void startSearchingTarget(Entity entity) {
        VelocityComponent vc = ComponentsMapper.velocity.get(entity);
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        ec.setCurrentAnimation(EnemyComponent.FrameType.RUNNING);
        vc.getFrontForce().setEnabled(false);
        ec.setStatus(EnemyComponent.Status.SEARCH_TARGET);
        ec.setFrameDuration(0);
    }

    private void updateLastTargetPositionIfNeeded(Entity entity, float dirToPlayer) {
        PositionComponent pc = ComponentsMapper.position.get(entity);
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        if (isTargetOnSight(entity, dirToPlayer, ec.getDefinitionComponent().getFov(), ec.getDirection())) {
            PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
            ec.setLastTargetPosition(playerPc.getX(), playerPc.getY());
        }
    }

    private void behaveIdle(Entity enemyEntity) {
        PositionComponent positionComponent = ComponentsMapper.position.get(enemyEntity);
        EnemyComponent ec = ComponentsMapper.enemy.get(enemyEntity);
        float fov = ec.getDefinitionComponent().getFov();
        if (isTargetOnSight(enemyEntity, calculateDirectionToPlayer(positionComponent), fov, ec.getDirection()))
            startRunningToTarget(enemyEntity);
    }

    private boolean isTargetOnSight(Entity enemyEntity, float dirToTarget, float fov, float direction) {
        return isOnSight(dirToTarget, fov, direction) && !doLinesBlockSight(enemyEntity);
    }

    private boolean isOnSight(float dirToTarget, float fov, float direction) {
        boolean result = (direction - fov < 0 && (dirToTarget >= SharedUtils.clampAngle(direction - fov)
                || dirToTarget <= SharedUtils.clampAngle(direction + fov)))
                || (dirToTarget >= SharedUtils.clampAngle(direction - fov)
                && dirToTarget <= SharedUtils.clampAngle(direction + fov));
        return result;
    }

    private boolean doLinesBlockSight(Entity enemyEntity) {
        PositionComponent pc = ComponentsMapper.position.get(enemyEntity);
        float radius = pc.getRadius() * 2;
        //The whole thing will be changed using spatial hash.
        auxEnvelope.init(pc.getX() - radius, pc.getX() + radius, pc.getY() - radius, pc.getY() + radius);
        List<Line> nearbyLines = level.getLines().getQuadTree().query(auxEnvelope);
        for (Line line : nearbyLines) {
            if (doesLineBlockSight(enemyEntity, line)) return true;
        }
        return false;
    }

    private boolean doesLineBlockSight(Entity enemyEntity, Line line) {
        PositionComponent pc = ComponentsMapper.position.get(enemyEntity);
        VertexElement src = line.getSrc();
        VertexElement dst = line.getDst();
        PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
        boolean clipping = Intersector.intersectSegments(pc.getX(), pc.getY(), playerPc.getX(), playerPc.getY(),
                src.getX(), src.getY(), dst.getX(), dst.getY(), null);
        return clipping && (line.getBackSectorId() < 0 || doSectorsAltitudesBlockSight(line, pc));
    }

    private boolean doSectorsAltitudesBlockSight(Line line, PositionComponent pc) {
        int lineSide = Intersector.pointLineSide(line.getSrc().getX(), line.getSrc().getY(),
                line.getDst().getX(), line.getDst().getY(), pc.getX(), pc.getY());
        Sector frontSector = (Sector) level.getSectors().get(line.getFrontSectorId());
        Sector backSector = (Sector) level.getSectors().get(line.getBackSectorId());
        float headZ = pc.getZ() + pc.getBodyAltitude();
        if (lineSide == 1) return backSector.getFloorAltitude() >= headZ || backSector.getCeilingAltitude() <= headZ;
        else return frontSector.getFloorAltitude() >= headZ || frontSector.getCeilingAltitude() <= headZ;
    }

    public void startRunningToTarget(Entity enemyEntity) {
        EnemyComponent ec = ComponentsMapper.enemy.get(enemyEntity);
        ec.setAnimationLoop(true);
        ec.setStateTime(0);
        ec.setStatus(EnemyComponent.Status.RUN_TO_TARGET);
        ec.setFrameDuration(0.11f);
        PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
        ec.setLastTargetPosition(playerPc.getX(), playerPc.getY());
        ec.setCurrentAnimation(EnemyComponent.FrameType.RUNNING);
        activateFrontForce(ec, ComponentsMapper.velocity.get(enemyEntity));
    }

    private void activateFrontForce(EnemyComponent ec, VelocityComponent velocityComponent) {
        velocityComponent.getFrontForce().setEnabled(true);
        velocityComponent.getFrontForce().setSpeed(ec.getDefinitionComponent().getSpeed());
    }

    public void onSwitchToHurtingFrame(Entity enemyEntity) {
        PositionComponent pc = ComponentsMapper.position.get(enemyEntity);
        PositionComponent playerPc = ComponentsMapper.position.get(playerEntity);
        float distanceToPlayer = auxVector.set(pc.getX(), pc.getY()).dst2(playerPc.getX(), playerPc.getY());
        if (distanceToPlayer <= pc.getRadius() + playerPc.getRadius()) {
            damagePlayer(pc);
        }
    }

    private void damagePlayer(PositionComponent pc) {
    }

    public void enemyTakeDamage(Entity enemy, Entity bullet) {
        EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
        enemyComponent.setHP(enemyComponent.getHp() - 1);
        if (!enemyComponent.getStatus().equals(EnemyComponent.Status.DEAD)) {
            if (enemyComponent.getDefinitionComponent().getPainChance() >= MathUtils.random() * 100)
                setEnemyToPain(enemy);
            else if (enemyComponent.getStatus().equals(EnemyComponent.Status.IDLE))
                enemyComponent.setStatus(EnemyComponent.Status.SEARCH_TARGET);
        }
    }

    private void setEnemyToPain(Entity entity) {
        EnemyComponent ec = ComponentsMapper.enemy.get(entity);
        ec.setCurrentAnimation(EnemyComponent.FrameType.PAIN);
        ec.setStatus(EnemyComponent.Status.SUFFER);
        ec.setLastSufferTime(TimeUtils.millis());
        ComponentsMapper.velocity.get(entity).getFrontForce().setEnabled(false);
    }

    public void init(Level level, Entity playerEntity) {
        this.level = level;
        this.playerEntity = playerEntity;
    }
}
