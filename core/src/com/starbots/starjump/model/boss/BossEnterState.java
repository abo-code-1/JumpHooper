package com.starbots.starjump.model.boss;

import com.starbots.starjump.model.World;

/** Phase 1: the boss descends from above the screen to its hover line. */
public final class BossEnterState implements BossState {

    private static final float DESCENT_SPEED = 110f;

    @Override
    public void update(Boss b, World world, float dt) {
        b.y += DESCENT_SPEED * dt;
        if (b.y >= b.hoverY) {
            b.y = b.hoverY;
            b.entered = true;
            b.setState(new BossAttackState());
        }
    }

    @Override public String name() { return "ENTER"; }
}
