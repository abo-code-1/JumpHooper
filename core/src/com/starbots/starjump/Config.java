package com.starbots.starjump;

/**
 * Central tuning constants.
 *
 * <p>All gameplay numbers are ported 1:1 from the original React-Native
 * {@code systems.js} so the physics feel identical. The original ran in
 * device-independent-pixel ("dp") space using {@code Dimensions.get('window')};
 * here we pin that to a fixed virtual resolution and let a {@code FitViewport}
 * scale it to any real window, which keeps the simulation deterministic.</p>
 *
 * <p>Coordinate convention (also ported from the original): <b>y grows
 * downward</b>, {@code y = 0} is the top of the screen. The render layer flips
 * y when drawing so libGDX's y-up camera shows it correctly.</p>
 */
public final class Config {
    private Config() {}

    /** Virtual world size. Roughly a portrait phone; the original used live device dp. */
    public static final float WORLD_WIDTH  = 432f;
    public static final float WORLD_HEIGHT = 768f;

    /** Simulation runs at a fixed 60 Hz step — the original constants are per-frame. */
    public static final float STEP = 1f / 60f;

    /** Number of recycled platforms on screen at once (original: {@code n = 9}). */
    public static final int PLATFORM_COUNT = 9;

    // --- Player (original character entity) -----------------------------------
    public static final float PLAYER_W = 43.8f;
    public static final float PLAYER_H = 79f;
    public static final float PLAYER_START_SPEED = -10f;   // moving up at spawn

    // --- Platforms ------------------------------------------------------------
    public static final float PLATFORM_W = 60f;
    public static final float PLATFORM_H = 14.8f;
    public static final float PLATFORM_SPACING = 60f;      // initial vertical gap
    public static final float MOVING_SPEED = 1.5f;         // |velocityX| of moving platforms

    // --- Physics (ported verbatim) -------------------------------------------
    public static final float GRAVITY = 0.25f;
    /**
     * Base jump impulse; effective jump = JUMP_BASE - score/6000 (faster as you
     * climb). Slightly gentler than the original {@code -13.6} so the world
     * scrolls a touch slower and hazards are easier to read.
     */
    public static final float JUMP_BASE = -12.2f;
    /** Multiplier applied to scroll the world down while the player ascends. */
    public static final float SCROLL_FACTOR = -1.2f;
    /** Horizontal speed multiplier applied to the tilt value: x += tilt * MOVE_FACTOR. */
    public static final float MOVE_FACTOR = -20f;

    /**
     * Player must be above this line (and ascending) before the world scrolls.
     * The original used {@code height/3} (player parked in the top third); we
     * keep the player lower (~45% down) so there is much more sky above to see
     * incoming enemies — a readability tweak, not a physics one.
     */
    public static final float SCROLL_THRESHOLD = WORLD_HEIGHT * 0.45f;

    /** Lava platforms only start appearing once the score passes this. */
    public static final int LAVA_UNLOCK_SCORE = 600;
}
