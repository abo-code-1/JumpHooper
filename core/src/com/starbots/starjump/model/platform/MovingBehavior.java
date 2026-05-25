package com.starbots.starjump.model.platform;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.Player;

/**
 * Strategy: a platform that slides left/right and bounces off the screen edges.
 *
 * <p>Ported from the original {@code animateEffects} 'moving' case: it starts
 * by moving right at {@code +1.5}, reverses to {@code -1.5} when its right edge
 * gets within 10px of the wall, and reverses back when its left edge gets
 * within 10px of the wall.</p>
 */
public final class MovingBehavior implements PlatformBehavior {

    public static final MovingBehavior INSTANCE = new MovingBehavior();

    private MovingBehavior() {}

    @Override
    public void update(Platform p) {
        if (p.velocityX > 0) {
            if (p.width + p.x + 10 > Config.WORLD_WIDTH) {
                p.velocityX = -Config.MOVING_SPEED;
            }
        } else if (p.velocityX < 0) {
            if (p.x - 10 < 0) {
                p.velocityX = Config.MOVING_SPEED;
            }
        } else {
            p.velocityX = Config.MOVING_SPEED; // first frame: start moving right
        }
        p.x += p.velocityX;
    }

    @Override
    public boolean onLand(Platform platform, Player player) {
        return false; // moving platforms are still safe to land on
    }
}
