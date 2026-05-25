package com.starbots.starjump.model.platform;

import com.starbots.starjump.model.Player;

/**
 * Strategy interface for platform behaviour. Each variant decides how the
 * platform moves every frame and what happens when the player lands on it.
 */
public interface PlatformBehavior {
    /** Per-frame movement (e.g. oscillation for moving platforms). */
    void update(Platform platform);

    /**
     * @return {@code true} if landing on this platform kills the player
     *         (i.e. lava); {@code false} for a normal bounce.
     */
    boolean onLand(Platform platform, Player player);

    /**
     * @return {@code true} if the platform shatters after one bounce (cracked
     *         platforms). The World handles the actual break.
     */
    default boolean breaksOnLand() {
        return false;
    }
}
