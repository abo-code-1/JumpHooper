package com.starbots.starjump.model.platform;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.Config;

/**
 * Concrete creator: produces platforms when they recycle from the bottom to the
 * top of the screen. The effect roll is ported from the original {@code up()}:
 *
 * <pre>
 *   effect = round(rand * 10) + max(round(score/1000), 4)
 *   effect <= 7  -> static
 *   otherwise    -> moving
 * </pre>
 *
 * <p>Lava is layered on top to reproduce the original's <i>intent</i> (its
 * literal {@code jump()} lava code was a single-{@code =} bug that only ever
 * affected platform {@code p1}, guarded by a "spawn lava only if none exists"
 * check). We honour that intent: once the score passes {@link
 * Config#LAVA_UNLOCK_SCORE} and no lava is currently on screen
 * ({@link SpawnContext#lavaAllowed}), a recycled platform may roll into lava.</p>
 */
public final class EffectPlatformFactory extends PlatformFactory {

    /** Chance a freshly opened "lava slot" becomes lava (only one at a time). */
    private static final float LAVA_CHANCE = 0.35f;

    @Override
    protected Platform create(SpawnContext ctx) {
        Platform p = new Platform();

        // Lava first (at most one on screen, only past the unlock score).
        if (ctx.lavaAllowed
                && ctx.score > Config.LAVA_UNLOCK_SCORE
                && MathUtils.random() < LAVA_CHANCE) {
            p.kind = PlatformKind.LAVA;
            p.setBehavior(LavaBehavior.INSTANCE);
            return p;
        }

        // Otherwise replicate the static/moving roll exactly.
        int sum = Math.max(Math.round(ctx.score / 1000f), 4);
        int effect = Math.round(MathUtils.random() * 10f) + sum;

        if (effect > 7) {
            // Moving platforms stay intact-looking.
            p.kind = randomSolidGround();
            p.setBehavior(MovingBehavior.INSTANCE);
        } else {
            // Static slot: a cracked texture means it actually breaks.
            p.kind = randomGround();
            p.setBehavior(isCracked(p.kind) ? BreakableBehavior.INSTANCE : StaticBehavior.INSTANCE);
        }
        return p;
    }
}
