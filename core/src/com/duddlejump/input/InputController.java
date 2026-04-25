package com.duddlejump.input;

public interface InputController {
    float getHorizontal();

    default boolean isPauseRequested() {
        return false;
    }
}
