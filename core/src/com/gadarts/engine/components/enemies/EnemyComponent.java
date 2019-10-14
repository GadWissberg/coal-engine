package com.gadarts.engine.components.enemies;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.gadarts.engine.systems.enemy.AiOrder;

import java.util.ArrayList;

public class EnemyComponent implements Component, Pool.Poolable {


    private long lastManeuverTime;
    private Runnable eventOnSwitchToHurtingFrame;
    private FrameType currentFrameType = FrameType.RUNNING;
    private FacingDirection facingDirection;
    private ArrayList<DirectionChangeSubscriber> subscribersForDirectionChange;
    private Status status = Status.IDLE;
    private float frameDuration;
    private boolean animationLoop;
    private Vector2 lastTargetPosition = new Vector2();
    private long lastSearchingTurnTime;
    private int hp;
    private float maneuverAngle;
    private EnemyDefinitionComponent definitionComponent;
    private Vector2 direction = new Vector2(1, 0);
    private float stateTime;
    private long lastSufferTime;
    private EnemyAnimations animations = new EnemyAnimations();
    private AiOrder currentOrder;
    private Queue<AiOrder> orders = new Queue<AiOrder>();

    @Override
    public void reset() {
    }

    public long getLastManeuverTime() {
        return lastManeuverTime;
    }

    public void setLastManeuverTime(long millis) {
        lastManeuverTime = millis;
    }

    public void setFrameDuration(float v) {
        frameDuration = v;
    }

    public void setLastTargetPosition(float x, float y) {
        lastTargetPosition.set(x, y);
    }

    public Vector2 getLastTargetPosition() {
        return lastTargetPosition;
    }

    public long getLastSearchingTurnTime() {
        return lastSearchingTurnTime;
    }

    public void setLastSearchingTurnTime(long millis) {
        lastSearchingTurnTime = millis;
    }

    public int getHp() {
        return hp;
    }

    public void setHP(int hp) {
        this.hp = hp;
    }

    public float getManeuverAngle() {
        return maneuverAngle;
    }

    public void setManeuverAngle(float v) {
        this.maneuverAngle = v;
    }

    public void setAnimationLoop(boolean b) {
        animationLoop = b;
    }

    private void informSubscribersForDirectionChange() {
        if (subscribersForDirectionChange != null) {
            for (DirectionChangeSubscriber subscriber : subscribersForDirectionChange) {
                subscriber.onDirectionChange(direction.angle());
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public float getDirection() {
        return direction.angle();
    }

    public void setDirection(float direction) {
        this.direction.setAngle(direction);
        informSubscribersForDirectionChange();
    }

    public long getLastSufferTime() {
        return lastSufferTime;
    }

    public void setLastSufferTime(long millis) {
        lastSufferTime = millis;
    }

    public void subscribeForDirectionChange(DirectionChangeSubscriber subscriber) {
        if (subscribersForDirectionChange == null) {
            subscribersForDirectionChange = new ArrayList<DirectionChangeSubscriber>();
        }
        if (!subscribersForDirectionChange.contains(subscriber)) {
            subscribersForDirectionChange.add(subscriber);
        }
    }

    public EnemyDefinitionComponent getDefinitionComponent() {
        return definitionComponent;
    }

    public void setDefinitionComponent(EnemyDefinitionComponent definitionComponent) {
        this.definitionComponent = definitionComponent;
        Array<TextureAtlas.AtlasRegion> regions = definitionComponent.getAtlas().getRegions();
        for (TextureAtlas.AtlasRegion region : regions) {
            insertFramesIfNeeded(region);
        }
    }

    public void setEventOnSwitchToHurtingFrame(Runnable runnable) {
        eventOnSwitchToHurtingFrame = runnable;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(int i) {
        stateTime = i;
    }

    private void insertFramesIfNeeded(TextureAtlas.AtlasRegion region) {
        try {
            String[] split = region.name.split("_");
            String animationTypeName = split[0];
            FrameType frameType = FrameType.valueOf(animationTypeName.toUpperCase());
            String frameDirectionName = frameType.hasAnimationsForSides ? split[1] : FacingDirection.FRONT.getFileName();
            if (!animations.containsAnimationsOfType(frameType, frameDirectionName)) {
                insertAnimations(region, animationTypeName, frameDirectionName);
            }
        } catch (Exception ignored) {

        }
    }

    private void insertAnimations(TextureAtlas.AtlasRegion region, String frameTypeName, String frameDirectionName) {
        Array<TextureAtlas.AtlasRegion> frames = definitionComponent.getAtlas().findRegions(region.name);
        FrameType type = FrameType.valueOf(frameTypeName.toUpperCase());
        EnemyAnimation<TextureRegion> animation = new EnemyAnimation<TextureRegion>(0, frames,
                PlayMode.NORMAL, type);
        if (type.hasAnimationsForSides)
            animations.putFramesByTypeAndDirection(type, FacingDirection.getByFacingFileName(frameDirectionName), animation);
        else animations.putFramesByTypeAndDirection(type, animation);
    }

    public TextureRegion getCurrentFrame(float deltaTime) {
        EnemyAnimation<TextureRegion> currentAni = getCurrentAnimation();
        int prevFrameIndex = -1;
        if (currentAni.getFrameType().equals(FrameType.ATTACK))
            prevFrameIndex = ((int) (stateTime / currentAni.getFrameDuration())) % currentAni.getKeyFrames().length;
        currentAni.setFrameDuration(frameDuration);
        stateTime += deltaTime;
        checkSwitchToHurtingFrame(currentAni, prevFrameIndex);
        return currentAni.getKeyFrame(stateTime, animationLoop);
    }

    private void checkSwitchToHurtingFrame(EnemyAnimation<TextureRegion> currentAnimation, int prevFrameIndex) {
        if (prevFrameIndex > -1) {
            int currentFrameIndex = (int) (stateTime / currentAnimation.getFrameDuration());
            currentFrameIndex %= currentAnimation.getKeyFrames().length;
            boolean isPrevIsOneBeforeLast = prevFrameIndex == currentAnimation.getKeyFrames().length - 2;
            boolean isCurrentIsLast = currentFrameIndex == currentAnimation.getKeyFrames().length - 1;
            if (isPrevIsOneBeforeLast && isCurrentIsLast) eventOnSwitchToHurtingFrame.run();
        }
    }

    public EnemyAnimation<TextureRegion> getCurrentAnimation() {
        EnemyAnimation<TextureRegion> result;
        if (currentFrameType.hasAnimationsForSides) {
            result = animations.getFramesByTypeAndDirection(currentFrameType, facingDirection);
        } else {
            result = animations.getFramesByTypeAndDirection(currentFrameType, FacingDirection.FRONT);
        }
        return result;
    }

    public void setCurrentAnimation(FrameType type) {
        this.currentFrameType = type;
    }

    public void setFacingDirection(FacingDirection facingDirection) {
        this.facingDirection = facingDirection;
    }

    public void addOrder(AiOrder order) {
        orders.addLast(order);
    }

    public boolean isOrderInProcess() {
        return currentOrder != null;
    }

    public Queue<AiOrder> getOrders() {
        return orders;
    }

    public void beginOrderProcess(AiOrder order) {
        currentOrder = order;
        order.setBeganTime(TimeUtils.millis());
        setDirection(order.getDirection());
    }

    public AiOrder getCurrentOrder() {
        return currentOrder;
    }

    public void finishOrder() {
        clearCurrentOrder();
        currentOrder = null;
    }

    public void forceOrder(AiOrder order) {
        clearCurrentOrder();
        beginOrderProcess(order);
    }

    private void clearCurrentOrder() {
        if (currentOrder != null) {
            Pools.get(AiOrder.class).free(currentOrder);
        }
    }

    public enum Status {
        IDLE, RUN_TO_TARGET, SEARCH_TARGET, ATTACK, DEAD, SUFFER
    }

    public enum FrameType {
        RUNNING, ATTACK, DIE(false), PAIN;

        private final boolean hasAnimationsForSides;

        FrameType() {
            this(true);
        }

        FrameType(boolean hasAnimationsForSides) {
            this.hasAnimationsForSides = hasAnimationsForSides;
        }

    }

    public enum FacingDirection {
        FRONT("f"), FRONT_LEFT("fl"), LEFT("l"), BACK_LEFT("bl"),
        BACK("b"), BACK_RIGHT("br"), RIGHT("r"), FRONT_RIGHT("fr");

        private final String fileName;

        FacingDirection(String fileName) {
            this.fileName = fileName;
        }

        public static FacingDirection getByFacingFileName(String fileName) {
            for (FacingDirection face : FacingDirection.values()) {
                if (face.getFileName().equals(fileName)) return face;
            }
            return null;
        }

        public String getFileName() {
            return fileName;
        }


    }

}
