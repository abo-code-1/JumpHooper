package com.starbots.starjump.model.platform;

/**
 * Information a {@link PlatformFactory} needs to decide what kind of platform
 * to produce. Kept tiny and reused to avoid per-spawn allocation.
 */
public final class SpawnContext {
    /** Current run score — drives effect probability, exactly like the original. */
    public int score;

    /**
     * Whether a lava platform is allowed to spawn right now. The original code
     * intended "at most one lava platform on screen at a time" (its {@code count}
     * guard); the World sets this to false while a lava platform already exists.
     */
    public boolean lavaAllowed;

    public SpawnContext set(int score, boolean lavaAllowed) {
        this.score = score;
        this.lavaAllowed = lavaAllowed;
        return this;
    }
}
