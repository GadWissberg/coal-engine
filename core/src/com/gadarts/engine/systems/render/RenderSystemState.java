package com.gadarts.engine.systems.render;

public class RenderSystemState {
    private static final String MESSAGE_OK = "Everything's normal";
    private RenderSystemStates state = RenderSystemStates.NORMAL;
    private String message = MESSAGE_OK;

    void setState(RenderSystemStates state) {
        this.state = state;
        if (state == RenderSystemStates.NORMAL) {
            setMessage(MESSAGE_OK);
        }
    }

    public RenderSystemStates getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message, String... params) {
        this.message = String.format(message, (Object) params);
    }

    public enum RenderSystemStates {NORMAL, FAIL}
}
