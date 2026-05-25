package com.starbots.starjump.model.platform;

import com.badlogic.gdx.math.MathUtils;

/**
 * Creator of the Factory Method pattern.
 *
 * <p>{@link #spawn} is the template that all callers use; the actual product is
 * built by the abstract {@link #create} factory method, which subclasses
 * override:</p>
 * <ul>
 *   <li>{@link PlainPlatformFactory} — used for the opening layout; always a
 *       safe, static platform.</li>
 *   <li>{@link EffectPlatformFactory} — used when recycling; rolls for
 *       moving / lava effects based on the score, like the original.</li>
 * </ul>
 */
public abstract class PlatformFactory {

    /** Common entry point: positions the product and applies shared setup. */
    public final Platform spawn(float x, float y, SpawnContext ctx) {
        Platform p = create(ctx);   // <-- the factory method
        p.x = x;
        p.y = y;
        p.velocityX = 0f;
        return p;
    }

    /** Factory method: subclasses decide the concrete configuration. */
    protected abstract Platform create(SpawnContext ctx);

    /** Shared helper: pick one of the four ground textures, as in renderers.js. */
    protected final PlatformKind randomGround() {
        switch (MathUtils.random(0, 3)) {
            case 0:  return PlatformKind.GRASS;
            case 1:  return PlatformKind.GRASS_BROKEN;
            case 2:  return PlatformKind.STONE;
            default: return PlatformKind.STONE_BROKEN;
        }
    }

    /** Pick an intact (non-cracked) ground texture. */
    protected final PlatformKind randomSolidGround() {
        return MathUtils.randomBoolean() ? PlatformKind.GRASS : PlatformKind.STONE;
    }

    protected static boolean isCracked(PlatformKind kind) {
        return kind == PlatformKind.GRASS_BROKEN || kind == PlatformKind.STONE_BROKEN;
    }
}
