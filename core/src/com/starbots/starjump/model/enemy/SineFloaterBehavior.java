package com.starbots.starjump.model.enemy;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.Player;

/** Strategy: drifts horizontally while bobbing up and down on a sine wave. */
public final class SineFloaterBehavior implements EnemyBehavior {
    public static final SineFloaterBehavior INSTANCE = new SineFloaterBehavior();
    private SineFloaterBehavior() {}

    private static final float BOB_SPEED = 4f;
    private static final float BOB_AMOUNT = 28f; // px/s peak vertical speed

    @Override
    public void update(Enemy e, Player player, float dt) {
        e.x += e.vx * dt;
        if (e.x < 0) { e.x = 0; e.vx = -e.vx; }
        else if (e.right() > Config.WORLD_WIDTH) { e.x = Config.WORLD_WIDTH - e.width; e.vx = -e.vx; }

        e.phase += BOB_SPEED * dt;
        e.y += MathUtils.sin(e.phase) * BOB_AMOUNT * dt;
    }
}
