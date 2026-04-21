package com.duddlejump.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public final class AccelerometerInputController implements InputController {

    private static final float TILT_SENSITIVITY = 5f;

    @Override
    public float getHorizontal() {
        float tilt = Gdx.input.getAccelerometerX();
        return MathUtils.clamp(-tilt / TILT_SENSITIVITY, -1f, 1f);
    }

    @Override
    public boolean isPauseRequested() {
        return Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK);
    }
}
