package com.gadarts.engine.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Color;
import com.gadarts.engine.utils.C;

public class EffectsSystem extends EntitySystem {
    private static Color screenColorMultiplier = new Color();

    public static Color getScreenColorMultiplier() {
        return screenColorMultiplier;
    }

    public static void setScreenColorMultiplier(float r, float g, float b) {
        screenColorMultiplier.set(r, g, b, 1);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (screenColorMultiplier.r != 0 || screenColorMultiplier.g != 0 || screenColorMultiplier.b != 0) {
            float r = Math.max(screenColorMultiplier.r - C.ScreenFade.PICKUP_SCREEN_FADE_PACE, 0);
            float g = Math.max(screenColorMultiplier.g - C.ScreenFade.PICKUP_SCREEN_FADE_PACE, 0);
            float b = Math.max(screenColorMultiplier.b - C.ScreenFade.PICKUP_SCREEN_FADE_PACE, 0);
            screenColorMultiplier.set(r, g, b, 1);
        }
    }
}
