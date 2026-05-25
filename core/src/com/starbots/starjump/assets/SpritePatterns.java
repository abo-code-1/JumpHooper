package com.starbots.starjump.assets;

import com.badlogic.gdx.graphics.Color;

/** Retro pixel grids + palettes for the procedurally generated creatures. */
public final class SpritePatterns {
    private SpritePatterns() {}

    /** Classic invader-style drone. 1 = body, 2 = eye. */
    public static final int[][] DRONE = {
            {0,0,1,0,0,0,0,0,1,0,0},
            {0,0,0,1,0,0,0,1,0,0,0},
            {0,0,1,1,1,1,1,1,1,0,0},
            {0,1,1,2,1,1,1,2,1,1,0},
            {1,1,1,1,1,1,1,1,1,1,1},
            {1,0,1,1,1,1,1,1,1,0,1},
            {1,0,1,0,0,0,0,0,1,0,1},
            {0,0,0,1,1,0,1,1,0,0,0},
    };
    public static final Color[] DRONE_PALETTE = {
            new Color(0x3fe0ffff), // body cyan
            new Color(0x0a0f3cff), // eye navy
    };

    /** Sleeker hunter ship. 1 = body, 2 = cockpit. */
    public static final int[][] HUNTER = {
            {0,0,0,0,1,0,0,0,0},
            {0,0,0,1,1,1,0,0,0},
            {0,0,1,1,2,1,1,0,0},
            {0,1,1,1,1,1,1,1,0},
            {1,1,1,1,1,1,1,1,1},
            {0,1,0,1,1,1,0,1,0},
            {0,0,0,1,0,1,0,0,0},
            {0,0,1,0,0,0,1,0,0},
    };
    public static final Color[] HUNTER_PALETTE = {
            new Color(0xff5db1ff), // body pink
            new Color(0xfff5ffff), // cockpit white
    };

    /** Boss saucer. 1 = hull, 2 = ring, 3 = glow, 4 = core eye. */
    public static final int[][] BOSS = {
            {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
            {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
            {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
            {1,1,1,1,2,2,2,2,2,2,2,1,1,1,1},
            {1,1,1,2,2,3,3,3,3,3,2,2,1,1,1},
            {1,1,1,2,3,3,3,3,3,3,3,2,1,1,1},
            {1,1,1,2,3,3,4,4,4,3,3,2,1,1,1},
            {1,1,1,2,3,3,4,4,4,3,3,2,1,1,1},
            {0,1,1,2,2,3,3,3,3,3,2,2,1,1,0},
            {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
            {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
            {0,0,1,0,1,0,1,0,1,0,1,0,1,0,0},
    };
    public static final Color[] BOSS_PALETTE = {
            new Color(0x6b3fa0ff), // hull purple
            new Color(0xb06bffff), // ring violet
            new Color(0xff4d6dff), // glow red
            new Color(0xfff200ff), // core eye yellow
    };
}
