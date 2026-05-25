package com.starbots.starjump.model;

import com.badlogic.gdx.utils.Pool;

/** A boss energy shot. Pooled to avoid per-shot allocation. y-down world space. */
public final class Projectile implements Pool.Poolable {
    public float x, y, vx, vy;
    public float radius = 11f;

    @Override
    public void reset() {
        x = y = vx = vy = 0f;
        radius = 11f;
    }
}
