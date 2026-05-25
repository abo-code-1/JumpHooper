package com.starbots.starjump.model.boss;

import com.starbots.starjump.Config;
import com.starbots.starjump.model.World;

/**
 * The boss. A context for the {@link BossState} machine; it hovers near the top
 * of the arena, sways, and rains projectiles. The player damages it by bouncing
 * into it (each contact = one hit, with a short cooldown) while dodging shots.
 */
public final class Boss {
    public float x, y;
    public final float width = 130f;
    public final float height = 100f;

    public int hp;
    public int maxHp;

    public float hoverY;                  // target hover line (y-down)
    public final float baseCenterX = Config.WORLD_WIDTH / 2f;
    public float swayPhase;
    public float fireTimer;

    public float hitCooldown;             // so one bounce = one hit
    public float flash;                   // >0 => render bright (just got hit)
    public boolean entered;               // finished the entrance, now vulnerable

    private BossState state;

    public Boss(int hp, float hoverY) {
        this.hp = this.maxHp = hp;
        this.hoverY = hoverY;
        this.x = baseCenterX - width / 2f;
        this.y = -height;                 // start above the screen
        setState(new BossEnterState());
    }

    public void setState(BossState s) {
        this.state = s;
        if (s != null) s.onEnter(this);
    }

    public BossState getState() { return state; }

    public void update(World world, float dt) {
        if (hitCooldown > 0) hitCooldown -= dt;
        if (flash > 0) flash -= dt;
        if (state != null) state.update(this, world, dt);
    }

    public float centerX() { return x + width / 2f; }
    public float centerY() { return y + height / 2f; }
    public float bottom()  { return y + height; }
    public float right()   { return x + width; }
}
