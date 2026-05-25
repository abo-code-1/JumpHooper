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

    /**
     * Virtual world size. Width is fixed; <b>height adapts to the real device
     * aspect ratio</b> at startup (see {@link #adaptToScreen}) so the game fills
     * the screen with no letterbox bars. The 432×768 defaults match the original
     * and are what the headless tests and desktop fall back to. These are
     * intentionally <i>not</i> {@code final}: a compile-time constant would be
     * inlined into every call site and could never be retuned for the device.
     */
    public static final float WORLD_WIDTH  = 432f;
    public static       float WORLD_HEIGHT = 768f;

    /**
     * Stretch {@link #WORLD_HEIGHT} to match the device's aspect ratio so a tall
     * phone screen is filled top-to-bottom instead of letterboxed. Width stays
     * pinned at {@link #WORLD_WIDTH}; every gameplay and UI position is expressed
     * relative to these two values, so a taller world simply reveals more sky and
     * pushes the death line / bottom-anchored UI down to the true screen edge —
     * nothing needs to move. Clamped so unusual screens stay laid out sanely.
     *
     * <p>Call once at startup, before any screen or {@code World} is built. Left
     * uncalled (headless tests), the 768 default keeps behaviour identical.</p>
     *
     * @param screenW drawing-buffer width in px
     * @param screenH drawing-buffer height in px
     */
    public static void adaptToScreen(int screenW, int screenH) {
        if (screenW <= 0 || screenH <= 0) return;
        float h = WORLD_WIDTH * ((float) screenH / (float) screenW);
        // Never shorter than the original (keeps wide/desktop windows unchanged);
        // cap extra-tall phones so vertical spacing doesn't stretch absurdly.
        WORLD_HEIGHT = Math.max(768f, Math.min(h, 1024f));
        SCROLL_THRESHOLD = WORLD_HEIGHT * 0.45f;
    }

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
    /**
     * Uniform vertical gap between platforms, used both for the opening layout
     * and when recycling. Kept comfortably below the jump apex so the next
     * platform is always reachable. (The original recycled to a fixed point,
     * which let an oversized ~300px gap circulate through the stack.)
     */
    public static final float PLATFORM_SPACING = 88f;
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
    public static float SCROLL_THRESHOLD = WORLD_HEIGHT * 0.45f;

    /** Lava platforms only start appearing once the score passes this. */
    public static final int LAVA_UNLOCK_SCORE = 600;

    // --- power-ups & lives ----------------------------------------------------
    /** Spring bounce impulse (~2x a normal jump, per Doodle Jump). */
    public static final float SPRING_SPEED = -19f;
    /** Steady upward speed while the jetpack is firing. */
    public static final float JETPACK_SPEED = -15f;
    /** Jetpack flight time in seconds (~5s in Doodle Jump). */
    public static final float JETPACK_DURATION = 4f;
    /** Brief invulnerability after taking a hit, so one touch costs one life. */
    public static final float INVULN_TIME = 1.3f;

    public static final int START_LIVES = 3;
    public static final int MAX_LIVES = 3;   // hearts top you back up to 3, never beyond
}
