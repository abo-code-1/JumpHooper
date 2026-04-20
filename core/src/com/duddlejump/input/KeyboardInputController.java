package com.duddlejump.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public final class KeyboardInputController implements InputController {

    @Override
    public float getHorizontal() {
        float x = 0f;
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            x -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            x += 1f;
        }
        return x;
    }

    @Override
    public boolean isPauseRequested() {
        return Gdx.input.isKeyJustPressed(Keys.P) || Gdx.input.isKeyJustPressed(Keys.ESCAPE);
    }
}
