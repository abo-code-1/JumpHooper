package com.starbots.starjump.model.platform;

import com.starbots.starjump.model.Player;

/**
 * Strategy: a deadly lava platform. It does not move, and landing on it ends
 * the run — matching the original {@code collide()} branch
 * {@code if (effects == 'lava') inGame = false}.
 */
public final class LavaBehavior implements PlatformBehavior {

    public static final LavaBehavior INSTANCE = new LavaBehavior();

    private LavaBehavior() {}

    @Override
    public void update(Platform platform) {
        // No movement.
    }

    @Override
    public boolean onLand(Platform platform, Player player) {
        return true; // fatal
    }
}
