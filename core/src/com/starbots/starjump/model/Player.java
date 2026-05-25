package com.starbots.starjump.model;

import com.starbots.starjump.Config;

/**
 * The astronaut. Position is the top-left corner in y-down world space, exactly
 * like the original character entity ({@code position}, {@code size},
 * {@code speed}, {@code side}).
 */
public final class Player {
    public float x, y;
    public final float width = Config.PLAYER_W;
    public final float height = Config.PLAYER_H;

    /** Vertical velocity. Positive = falling, negative = rising. */
    public float speed;

    /** true => facing/flipped left (original "180deg"); false => facing right ("0deg"). */
    public boolean facingLeft;

    public void reset() {
        x = Config.WORLD_WIDTH / 2f - 20f;   // original: width/2 - 20
        y = Config.WORLD_HEIGHT / 2f;        // original: height/2
        speed = Config.PLAYER_START_SPEED;   // -10
        facingLeft = false;
    }

    public float bottom() {
        return y + height;
    }
}
