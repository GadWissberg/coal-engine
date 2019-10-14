package com.gadarts.engine.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.Level;
import com.gadarts.engine.components.ComponentsMapper;
import com.gadarts.engine.components.enemies.EnemyComponent;
import com.gadarts.engine.components.model.instance.ModelInstanceComponent;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.input.interfaces.MouseMovedSubscriber;
import com.gadarts.engine.shaders.GameShaderProvider;
import com.gadarts.engine.systems.player.PlayerController;
import com.gadarts.engine.systems.render.RenderSystemState.RenderSystemStates;
import com.gadarts.engine.utils.C.Errors.CodeRelated;
import com.gadarts.engine.utils.C.ShaderRelated.FragmentShaderKeys;

public class RenderSystem extends EntitySystem implements MouseMovedSubscriber {
    private Vector2 auxVector1 = new Vector2();
    private Vector3 auxVector2 = new Vector3();
    private PlayerController playerController;
    private Level level;
    private CameraInputController debugInput;
    private ModelBatch modelBatch;
    private RenderSystemState state = new RenderSystemState();
    private ImmutableArray<Entity> modelsInstances;
    private PerspectiveCamera camera;
    private FrameBuffer blurTargetA;
    private SpriteBatch spriteBatch = new SpriteBatch();
    private GameShaderProvider shaderProvider;
    private int blurDirectionLocation;
    private Vector2 blurMag = new Vector2();
    private int blurRadiusLocation;
    private int blurResolutionLocation;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        Gdx.gl20.glStencilMask(0x00);
        shaderProvider = new GameShaderProvider();
        modelBatch = new ModelBatch(shaderProvider);
        initializeBlur();
    }

    private void initializeBlur() {
        blurTargetA = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        ShaderProgram blurShaderProgram = shaderProvider.getBlurShaderProgram();
        spriteBatch.setShader(blurShaderProgram);
        blurRadiusLocation = blurShaderProgram.getUniformLocation(FragmentShaderKeys.BLUR_RADIUS);
        blurDirectionLocation = blurShaderProgram.getUniformLocation(FragmentShaderKeys.BLUR_DIRECTION);
        blurResolutionLocation = blurShaderProgram.getUniformLocation(FragmentShaderKeys.BLUR_RESOLUTION);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        assertPlayerController();
        if (debugInput != null) debugInput.update();
        if (blurMag.len2() > 75) {
            blurredRender(deltaTime);
        } else {
            regularRender(deltaTime);
        }
    }

    private void blurredRender(float deltaTime) {
        blurTargetA.begin();
        regularRender(deltaTime);
        blurTargetA.end();
        applyBlur();
    }

    private void applyBlur() {
        initializeDisplay();
        spriteBatch.getProjectionMatrix().idt();
        spriteBatch.begin();
        setBlurUniforms();
        spriteBatch.draw(blurTargetA.getColorBufferTexture(), -1, 1, 2, -2);
        spriteBatch.end();
    }

    private void setBlurUniforms() {
        ShaderProgram blurShaderProgram = shaderProvider.getBlurShaderProgram();
        blurShaderProgram.setUniformf(blurRadiusLocation, blurMag.len2());
        blurMag.nor();
        blurShaderProgram.setUniformf(blurDirectionLocation, blurMag.x, blurMag.y);
        blurMag.setZero();
        blurShaderProgram.setUniformf(blurResolutionLocation, ((float) Gdx.graphics.getWidth()));
    }

    private void regularRender(float deltaTime) {
        initializeDisplay();
        Gdx.gl.glClearColor(1, 0, 0, 1);
        modelBatch.begin(camera);
        modelBatch.render(level.getSkyBox());
        modelBatch.flush();
        renderLevel();
        renderModels(deltaTime);
        modelBatch.end();
    }


    private void renderLevel() {
        modelBatch.render(level.getModel(), level.getEnvironment());
    }

    private void renderModels(float deltaTime) {
        for (Entity entity : modelsInstances) {
            ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
            ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
            if (ComponentsMapper.enemy.has(entity)) {
                handleBillboards(entity, modelInstance, deltaTime);
            }
            modelBatch.render(modelInstance);
        }
    }

    private void assertPlayerController() {
        if (playerController == null) {
            state.setState(RenderSystemStates.FAIL);
            state.setMessage(CodeRelated.PLAYER_NOT_INITIALIZED);
        }
    }

    private void initializeDisplay() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        int coverageSampling = Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | coverageSampling);
    }

    public void setDebugInput(CameraInputController cameraInputController) {
        this.debugInput = cameraInputController;
    }

    private void handleBillboards(Entity entity, ModelInstance modelInstance, float deltaTime) {
        EnemyComponent enemyComponent = ComponentsMapper.enemy.get(entity);
        PositionComponent enemyPos = ComponentsMapper.position.get(entity);
        Vector3 camPos = camera.position;
        float directionEnemyToCamera = auxVector1.set(camPos.x, camPos.y).sub(enemyPos.getX(), enemyPos.getY()).angle();
        handleEnemyFacingAnimation(directionEnemyToCamera, enemyComponent);
        handleBillboardAnimation(entity, modelInstance, deltaTime);
        handleBillboardTransformation(modelInstance, directionEnemyToCamera);
    }

    private void handleBillboardTransformation(ModelInstance modelInstance, float directionEnemyToCamera) {
        modelInstance.transform.getTranslation(auxVector2);
        modelInstance.transform.setToRotation(Vector3.Z, directionEnemyToCamera);
        modelInstance.transform.trn(auxVector2);
    }

    private void handleEnemyFacingAnimation(float dirEnemyToCamera, EnemyComponent enemyComponent) {
        auxVector1.setAngle(enemyComponent.getDirection() - dirEnemyToCamera);
        float angleDiff = auxVector1.angle();
        if ((angleDiff >= 0 && angleDiff <= 22.5) || (angleDiff > 337.5 && angleDiff <= 360)) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.FRONT);
        } else if (angleDiff > 22.5 && angleDiff <= 67.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.FRONT_RIGHT);
        } else if (angleDiff > 67.5 && angleDiff <= 112.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.RIGHT);
        } else if (angleDiff > 112.5 && angleDiff <= 157.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.BACK_RIGHT);
        } else if (angleDiff > 157.5 && angleDiff <= 202.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.BACK);
        } else if (angleDiff > 202.5 && angleDiff <= 247.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.BACK_LEFT);
        } else if (angleDiff > 247.5 && angleDiff <= 292.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.LEFT);
        } else if (angleDiff > 292.5 && angleDiff <= 337.5) {
            enemyComponent.setFacingDirection(EnemyComponent.FacingDirection.FRONT_LEFT);
        }
    }

    private void handleBillboardAnimation(Entity entity, ModelInstance modelInstance, float deltaTime) {
        Material material = modelInstance.getMaterial("enemy_texture");
        TextureAttribute attribute = material.get(TextureAttribute.class, TextureAttribute.Diffuse);
        EnemyComponent enemyComponent = ComponentsMapper.enemy.get(entity);
        TextureRegion currentFrame = enemyComponent.getCurrentFrame(deltaTime);
        if (currentFrame != null) {
            attribute.set(currentFrame);
        }
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }

    public RenderSystemState getState() {
        return state;
    }

    public void init(Level level, ImmutableArray<Entity> entitiesWithModelsInstances) {
        this.level = level;
        modelsInstances = entitiesWithModelsInstances;
    }

    public void setCamera(PerspectiveCamera cam) {
        camera = cam;
    }

    @Override
    public void mouseMoved(int magX, int magY) {
        if (magX == 0 && magY == 0) return;
        blurMag.set(magX, magY);
    }
}
