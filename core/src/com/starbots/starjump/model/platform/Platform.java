package com.starbots.starjump.model.platform;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.Player;

/**
 * A single platform. Its per-frame movement and its landing response are
 * delegated to a {@link PlatformBehavior} (Strategy pattern), so the platform
 * itself stays a dumb data holder.
 */
public final class Platform {
    public float x, y;
    public final float width = Config.PLATFORM_W;
    public final float height = Config.PLATFORM_H;

    /** Horizontal velocity used by the moving behaviour. */
    public float velocityX;

    /** Which sprite to draw (also reflects the lava/effect state). */
    public PlatformKind kind = PlatformKind.GRASS;

    /** The Strategy in charge of this platform's behaviour. */
    private PlatformBehavior behavior;

    public void setBehavior(PlatformBehavior behavior) {
        this.behavior = behavior;
    }

    public PlatformBehavior getBehavior() {
        return behavior;
    }

    /** Per-frame update, delegated to the current strategy. */
    public void update() {
        if (behavior != null) behavior.update(this);
    }

    /**
     * Called when the player lands on this platform.
     * @return {@code true} if the landing is fatal (lava).
     */
    public boolean onLand(Player player) {
        return behavior != null && behavior.onLand(this, player);
    }

    public float right() {
        return x + width;
    }
}
