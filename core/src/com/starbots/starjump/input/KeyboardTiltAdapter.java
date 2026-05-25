package com.starbots.starjump.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Adapter (desktop): adapts the libGDX keyboard API to {@link TiltControl}.
 *
 * <p>Left / right keys produce a tilt value with the same sign convention as a
 * phone accelerometer, so the shared world code is byte-for-byte identical
 * across platforms.</p>
 *
 * <ul>
 *   <li>Right / D pressed -> move right -> {@code x += tilt * (-20)} must be
 *       positive -> tilt is <b>negative</b>.</li>
 *   <li>Left / A pressed  -> move left  -> tilt is <b>positive</b>.</li>
 * </ul>
 */
public final class KeyboardTiltAdapter implements TiltControl {

    /** Magnitude chosen so a key press ≈ a firm phone tilt (~10 px/frame). */
    private static final float TILT = 0.5f;

    @Override
    public float getTilt() {
        boolean left  = Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A);
        boolean right = Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D);

        if (left == right) return 0f;   // both or neither
        return left ? TILT : -TILT;
    }
}
