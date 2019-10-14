package com.gadarts.engine.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.gadarts.engine.systems.EffectsSystem;
import com.gadarts.engine.utils.C;

public class GameShader extends DefaultShader {
    private int colorMultiplierLocation;

    public GameShader(Renderable renderable, Config shaderConfig) {
        super(renderable, shaderConfig);
    }

    @Override
    public void init() {
        super.init();
        colorMultiplierLocation = program.getUniformLocation(C.ShaderRelated.FragmentShaderKeys.COLOR_MULTIPLIER);
        if (program.getLog().length() != 0)
            System.out.println(program.getLog());
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);
        program.setUniformf(colorMultiplierLocation, EffectsSystem.getScreenColorMultiplier());
    }

}
