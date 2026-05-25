package com.starbots.starjump.model.enemy;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.Player;

/** Strategy: a hunter that homes toward the player's column and creeps downward. */
public final class DiverBehavior implements EnemyBehavior {
    public static final DiverBehavior INSTANCE = new DiverBehavior();
    private DiverBehavior() {}

    private static final float HOMING_SPEED = 42f;
    private static final float DESCENT = 12f;

    @Override
    public void update(Enemy e, Player player, float dt) {
        float dir = Math.signum(player.x - e.x);
        e.x += dir * HOMING_SPEED * dt;
        e.x = Math.max(0, Math.min(Config.WORLD_WIDTH - e.width, e.x));
        e.y += DESCENT * dt; // extra downward drift on top of world scroll
    }
}
