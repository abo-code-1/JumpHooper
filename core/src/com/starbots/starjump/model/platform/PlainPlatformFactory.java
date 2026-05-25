package com.starbots.starjump.model.platform;

/**
 * Concrete creator: produces the opening, always-safe platforms (the nine the
 * original lays out at startup with no effects). Static behaviour, random
 * grass/stone texture.
 */
public final class PlainPlatformFactory extends PlatformFactory {

    @Override
    protected Platform create(SpawnContext ctx) {
        Platform p = new Platform();
        p.kind = randomSolidGround();          // opening platforms are always solid
        p.setBehavior(StaticBehavior.INSTANCE);
        return p;
    }
}
