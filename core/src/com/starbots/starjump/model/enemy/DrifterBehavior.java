package com.starbots.starjump.model.enemy;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.Player;

/** Strategy: drifts horizontally, bouncing off the screen edges. */
public final class DrifterBehavior implements EnemyBehavior {
    public static final DrifterBehavior INSTANCE = new DrifterBehavior();
    private DrifterBehavior() {}

    @Override
    public void update(Enemy e, Player player, float dt) {
        e.x += e.vx * dt;
        if (e.x < 0) { e.x = 0; e.vx = -e.vx; }
        else if (e.right() > Config.WORLD_WIDTH) { e.x = Config.WORLD_WIDTH - e.width; e.vx = -e.vx; }
    }
}
