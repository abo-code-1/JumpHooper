package com.starbots.starjump.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.GameSettings;

/**
 * Adapter (mobile): adapts the device accelerometer to {@link TiltControl}.
 *
 * <p>The default mobile control mode is touch because it is deterministic on
 * every iPhone: hold the left half to move left, right half to move right.
 * Motion tilt remains available as an optional mode in settings.</p>
 */
public final class AccelerometerTiltAdapter implements TiltControl {

    private static final float GRAVITY = 9.81f;
    private static final float DEAD_ZONE = 0.06f;
    private static final float RESPONSE_CURVE = 1.35f;
    private static final float SMOOTHING = 0.22f;

    private final GameSettings settings = GameSettings.INSTANCE;
    private float smoothed;

    @Override
    public float getTilt() {
        if (settings.getControlMode() == GameSettings.CONTROL_TOUCH) {
            return touchTilt();
        }
        if (settings.getControlMode() == GameSettings.CONTROL_HYBRID && Gdx.input.isTouched()) {
            return touchTilt();
        }

        return motionTilt();
    }

    private float touchTilt() {
        if (!Gdx.input.isTouched()) return 0f;
        int width = Math.max(1, Gdx.graphics.getWidth());
        float x = Gdx.input.getX() / (float) width;
        float edge = x < 0.5f ? 1f : -1f; // positive tilt moves left in World.
        float mag = MathUtils.clamp(0.62f * settings.getTiltSensitivity(), 0.35f, 1f);
        return edge * mag;
    }

    private float motionTilt() {
        float raw = rawAxis(settings.getTiltAxis()) - settings.getTiltCenter();
        if (settings.isTiltInverted()) raw = -raw;

        raw = MathUtils.clamp(raw * settings.getTiltSensitivity(), -1f, 1f);
        float mag = Math.abs(raw);
        raw = Math.signum(raw) * (float) Math.pow(mag, RESPONSE_CURVE);
        smoothed += (raw - smoothed) * SMOOTHING;
        if (Math.abs(smoothed) < DEAD_ZONE) return 0f;
        return smoothed;
    }

    public static float rawAxis(int axis) {
        switch (axis) {
            case GameSettings.AXIS_Y:
                return MathUtils.clamp(Gdx.input.getAccelerometerY() / GRAVITY, -1f, 1f);
            case GameSettings.AXIS_Z:
                return MathUtils.clamp(Gdx.input.getAccelerometerZ() / GRAVITY, -1f, 1f);
            case GameSettings.AXIS_ROLL:
                return MathUtils.clamp(Gdx.input.getRoll() / 45f, -1f, 1f);
            case GameSettings.AXIS_PITCH:
                return MathUtils.clamp(Gdx.input.getPitch() / 45f, -1f, 1f);
            default:
                return MathUtils.clamp(Gdx.input.getAccelerometerX() / GRAVITY, -1f, 1f);
        }
    }
}
