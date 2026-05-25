package com.starbots.starjump.model.boss;

import com.starbots.starjump.model.Player;
import com.starbots.starjump.model.World;

/**
 * State pattern for the boss's behaviour phases (enter → attack → enrage). The
 * {@link Boss} is the context; each state decides how it moves and attacks and
 * when to hand off to the next phase.
 */
public interface BossState {
    /** Called once when the boss switches into this state. */
    default void onEnter(Boss boss) {}

    /** Per-frame behaviour. {@code world} lets the state spawn projectiles. */
    void update(Boss boss, World world, float dt);

    /** Short human-readable name (handy for HUD/debug). */
    String name();

    /** Fire {@code count} shots from the boss toward the player, fanned out. */
    static void fireAtPlayer(Boss b, World world, int count, float speed, float spreadDeg) {
        Player p = world.getPlayer();
        float cx = b.centerX();
        float cy = b.bottom(); // shoot from the underside
        float px = p.x + p.width / 2f;
        float py = p.y + p.height / 2f;
        double base = Math.atan2(py - cy, px - cx); // y-down: downward is +y
        double spread = Math.toRadians(spreadDeg);
        for (int i = 0; i < count; i++) {
            double a = base + spread * (i - (count - 1) / 2.0);
            float vx = (float) (Math.cos(a) * speed);
            float vy = (float) (Math.sin(a) * speed);
            world.spawnProjectile(cx, cy, vx, vy);
        }
    }
}
