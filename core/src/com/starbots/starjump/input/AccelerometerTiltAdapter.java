package com.starbots.starjump.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

/**
 * Adapter (android): adapts the device accelerometer to {@link TiltControl}.
 *
 * <p>This is the closest match to the original Expo build, which read
 * {@code accelerometerData.x} directly. libGDX reports accelerometer X in
 * m/s² (≈ ±9.8 at full tilt); we normalise to roughly ±1 and flip the sign so
 * tilting the phone right moves the astronaut right — matching the desktop
 * adapter's behaviour. A small dead-zone removes resting jitter.</p>
 */
public final class AccelerometerTiltAdapter implements TiltControl {

    private static final float GRAVITY = 9.81f;
    private static final float DEAD_ZONE = 0.05f;
    private static final float SENSITIVITY = 1.4f;

    @Override
    public float getTilt() {
        // Portrait phone: tilting left/right changes the X axis.
        float raw = Gdx.input.getAccelerometerX() / GRAVITY; // ≈ [-1, 1]
        raw *= SENSITIVITY;
        if (Math.abs(raw) < DEAD_ZONE) return 0f;
        return MathUtils.clamp(raw, -1f, 1f);
    }
}
