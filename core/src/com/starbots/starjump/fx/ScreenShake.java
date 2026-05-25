package com.starbots.starjump.fx;

import com.badlogic.gdx.math.MathUtils;

/**
 * A decaying camera shake. Trigger it with {@link #shake} (usually from a
 * gameplay event), advance it each frame, and read {@link #getX()}/{@link #getY()}
 * to offset the camera before rendering.
 */
public final class ScreenShake {

    private float trauma;       // 0..1
    private float duration;
    private float magnitude;
    private float offsetX, offsetY;

    /** @param magnitude max pixel offset, @param duration seconds. */
    public void shake(float magnitude, float duration) {
        this.magnitude = Math.max(this.magnitude, magnitude);
        this.duration = Math.max(this.duration, duration);
        this.trauma = 1f;
    }

    public void update(float dt) {
        if (trauma <= 0f) {
            offsetX = offsetY = 0f;
            return;
        }
        trauma = Math.max(0f, trauma - dt / Math.max(0.0001f, duration));
        float amount = magnitude * trauma * trauma;
        offsetX = MathUtils.random(-amount, amount);
        offsetY = MathUtils.random(-amount, amount);
    }

    public float getX() { return offsetX; }
    public float getY() { return offsetY; }
}
