package com.starbots.starjump.model.platform;

import com.starbots.starjump.model.Player;

/**
 * Strategy: a plain platform. It never moves and gives a normal bounce.
 * Stateless, so a single shared instance is enough (lightweight flyweight).
 */
public final class StaticBehavior implements PlatformBehavior {

    public static final StaticBehavior INSTANCE = new StaticBehavior();

    private StaticBehavior() {}

    @Override
    public void update(Platform platform) {
        // No movement.
    }

    @Override
    public boolean onLand(Platform platform, Player player) {
        return false; // safe bounce
    }
}
