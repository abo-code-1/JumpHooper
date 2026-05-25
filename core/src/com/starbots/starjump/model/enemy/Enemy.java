package com.starbots.starjump.model.enemy;

import com.starbots.starjump.model.Player;

/**
 * A flying hazard. Like {@link com.starbots.starjump.model.platform.Platform},
 * it is a dumb data holder whose movement is delegated to an
 * {@link EnemyBehavior} (Strategy). Position is the top-left corner in y-down
 * world space.
 */
public final class Enemy {
    public float x, y, width, height;
    public float vx, vy;
    public float phase;           // used by oscillating behaviours
    public EnemyType type = EnemyType.DRONE;

    private EnemyBehavior behavior;

    public void setBehavior(EnemyBehavior behavior) { this.behavior = behavior; }

    public void update(Player player, float dt) {
        if (behavior != null) behavior.update(this, player, dt);
    }

    public float centerX() { return x + width / 2f; }
    public float centerY() { return y + height / 2f; }
    public float bottom()  { return y + height; }
    public float right()   { return x + width; }
}
