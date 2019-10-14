package com.gadarts.engine.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.gadarts.engine.utils.C;

public class BlurShader extends DefaultShader {

    public BlurShader(Renderable renderable, Config shaderConfig) {
        super(renderable, shaderConfig);
    }

    @Override
    public void init() {
        super.init();
        if (program.getLog().length() != 0)
            System.out.println(program.getLog());
    }

    private void initializeBlurUniforms() {
        int blurDirectionLocation = program.getUniformLocation(C.ShaderRelated.FragmentShaderKeys.BLUR_DIRECTION);
        if (blurDirectionLocation != -1) {
            program.setUniformf(blurDirectionLocation, 1.0f, 0f);
        }
        setUniformInt(C.ShaderRelated.FragmentShaderKeys.BLUR_RESOLUTION, 2048);
        setUniformInt(C.ShaderRelated.FragmentShaderKeys.BLUR_RADIUS, 500);
    }

    private void setUniformInt(String uniformName, int i) {
        int location = program.getUniformLocation(uniformName);
        if (location != -1) {
            program.setUniformf(location, i);
        }
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);
        initializeBlurUniforms();
    }

}
