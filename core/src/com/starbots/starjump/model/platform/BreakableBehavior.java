package com.starbots.starjump.model.platform;

import com.starbots.starjump.model.Player;

/**
 * Strategy: a cracked platform. It gives one normal bounce and then shatters —
 * the World removes/recycles it on landing and fires a break event for FX. The
 * cracked texture telegraphs that it will not hold.
 */
public final class BreakableBehavior implements PlatformBehavior {

    public static final BreakableBehavior INSTANCE = new BreakableBehavior();

    private BreakableBehavior() {}

    @Override
    public void update(Platform platform) {
        // No movement.
    }

    @Override
    public boolean onLand(Platform platform, Player player) {
        return false; // not fatal — you still bounce
    }

    @Override
    public boolean breaksOnLand() {
        return true;
    }
}
