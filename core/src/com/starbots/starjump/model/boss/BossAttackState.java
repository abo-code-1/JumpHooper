package com.starbots.starjump.model.boss;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.model.World;

/** Phase 2: hover and sway, lobbing single aimed shots. Enrages on its last HP. */
public final class BossAttackState implements BossState {

    private static final float SWAY_SPEED = 1.6f;
    private static final float SWAY_AMP = 90f;      // narrower sway -> easier to line up a bounce
    private static final float FIRE_INTERVAL = 1.9f; // gentler fire so it can be approached

    @Override
    public void onEnter(Boss b) {
        b.fireTimer = 0.8f;
    }

    @Override
    public void update(Boss b, World world, float dt) {
        b.swayPhase += SWAY_SPEED * dt;
        b.x = b.baseCenterX + MathUtils.sin(b.swayPhase) * SWAY_AMP - b.width / 2f;

        b.fireTimer -= dt;
        if (b.fireTimer <= 0) {
            BossState.fireAtPlayer(b, world, 1, 150f, 0f);
            b.fireTimer = FIRE_INTERVAL;
        }

        // Only enrage on the final HP (integer divide: 3 -> 1), so most of the
        // fight stays in this calmer phase.
        if (b.hp <= b.maxHp / 2) {
            b.setState(new BossEnrageState());
        }
    }

    @Override public String name() { return "ATTACK"; }
}
