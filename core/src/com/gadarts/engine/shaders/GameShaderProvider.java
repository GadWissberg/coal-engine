package com.gadarts.engine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GameShaderProvider extends DefaultShaderProvider {
    private final ShaderProgram blurShaderProgram;
    private DefaultShader.Config mainShaderConfig;

    public GameShaderProvider() {
        mainShaderConfig = new DefaultShader.Config();
        mainShaderConfig.vertexShader = Gdx.files.internal("vertex_main.glsl").readString();
        mainShaderConfig.fragmentShader = Gdx.files.internal("fragment_main.glsl").readString();
        String blurFragmentShader = Gdx.files.internal("fragment_blur.glsl").readString();
        String blurVertexShader = Gdx.files.internal("vertex_blur.glsl").readString();
        blurShaderProgram = new ShaderProgram(blurVertexShader, blurFragmentShader);
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        return new GameShader(renderable, mainShaderConfig);
    }

    public ShaderProgram getBlurShaderProgram() {
        return blurShaderProgram;
    }
}
