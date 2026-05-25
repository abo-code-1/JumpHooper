package com.starbots.starjump.physics;

import com.starbots.starjump.model.Player;
import com.starbots.starjump.model.platform.Platform;

/**
 * Landing detection, ported verbatim from the original {@code collide()}.
 *
 * <p>A landing registers only while the player is falling and their feet are
 * within ±10px of the platform's top, with horizontal overlap.</p>
 */
public final class CollisionSystem {
    private CollisionSystem() {}

    public static boolean lands(Player c, Platform p) {
        return c.bottom() + 10 >= p.y          // feet + 10 >= platform top
            && c.bottom() - 10 <= p.y          // feet - 10 <= platform top
            && c.x + c.width >= p.x             // right edge >= platform left
            && c.x <= p.x + p.width;            // left edge <= platform right
    }
}
