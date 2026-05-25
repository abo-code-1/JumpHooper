package com.starbots.starjump.model.boss;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.model.World;

/** Phase 3 (low HP): faster sway, faster three-shot spreads. */
public final class BossEnrageState implements BossState {

    private static final float SWAY_SPEED = 2.6f;
    private static final float SWAY_AMP = 150f;
    private static final float FIRE_INTERVAL = 0.8f;

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
            BossState.fireAtPlayer(b, world, 3, 190f, 16f);
            b.fireTimer = FIRE_INTERVAL;
        }
    }

    @Override public String name() { return "ENRAGE"; }
}
