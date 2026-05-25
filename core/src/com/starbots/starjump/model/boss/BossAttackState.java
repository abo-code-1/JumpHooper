package com.starbots.starjump.model.boss;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.model.World;

/** Phase 2: hover and sway, lobbing single aimed shots. Enrages at half HP. */
public final class BossAttackState implements BossState {

    private static final float SWAY_SPEED = 1.6f;
    private static final float SWAY_AMP = 130f;
    private static final float FIRE_INTERVAL = 1.4f;

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
            BossState.fireAtPlayer(b, world, 1, 175f, 0f);
            b.fireTimer = FIRE_INTERVAL;
        }

        if (b.hp <= Math.ceil(b.maxHp / 2f)) {
            b.setState(new BossEnrageState());
        }
    }

    @Override public String name() { return "ATTACK"; }
}
