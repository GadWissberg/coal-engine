package com.gadarts.engine.input.interfaces;

import com.badlogic.gdx.InputProcessor;

public interface GameInputProcessor extends InputProcessor {
    void subscribeForMouseMoved(MouseMovedSubscriber subscriber);
}
