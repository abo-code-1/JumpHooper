package com.starbots.starjump.model.boss;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.model.World;

/** Phase 3 (final HP): faster sway, quicker two-shot spreads. */
public final class BossEnrageState implements BossState {

    private static final float SWAY_SPEED = 2.6f;
    private static final float SWAY_AMP = 100f;
    private static final float FIRE_INTERVAL = 1.2f;

    @Override
    public void onEnter(Boss b) {
        b.fireTimer = 0.4f;
    }

    @Override
    public void update(Boss b, World world, float dt) {
        b.swayPhase += SWAY_SPEED * dt;
        b.x = b.baseCenterX + MathUtils.sin(b.swayPhase) * SWAY_AMP - b.width / 2f;

        b.fireTimer -= dt;
        if (b.fireTimer <= 0) {
            BossState.fireAtPlayer(b, world, 2, 165f, 18f);
            b.fireTimer = FIRE_INTERVAL;
        }
    }

    @Override public String name() { return "ENRAGE"; }
}
