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

    /** Heart pickup (+1 life). 1 = outline, 2 = red, 3 = highlight. */
    public static final int[][] HEART = {
            {0,1,1,0,0,0,1,1,0},
            {1,2,2,1,0,1,2,2,1},
            {1,2,3,2,1,2,2,2,1},
            {1,2,2,2,2,2,2,2,1},
            {0,1,2,2,2,2,2,1,0},
            {0,0,1,2,2,2,1,0,0},
            {0,0,0,1,2,1,0,0,0},
            {0,0,0,0,1,0,0,0,0},
    };
    public static final Color[] HEART_PALETTE = {
            new Color(0x7a0a1aff), // outline dark red
            new Color(0xff3b5cff), // red
            new Color(0xffd0d8ff), // highlight
    };

    /** Coiled spring that sits on a platform. 1 = dark metal, 2 = light, 3 = red cap. */
    public static final int[][] SPRING = {
            {0,0,3,3,3,3,3,0,0},
            {0,0,1,1,1,1,1,0,0},
            {0,1,2,2,2,2,2,1,0},
            {0,0,1,2,2,2,1,0,0},
            {0,1,2,2,2,2,2,1,0},
            {0,0,1,2,2,2,1,0,0},
            {0,1,1,1,1,1,1,1,0},
            {1,1,1,1,1,1,1,1,1},
    };
    public static final Color[] SPRING_PALETTE = {
            new Color(0x4a4a52ff), // dark metal
            new Color(0xb8c0ccff), // light metal
            new Color(0xff4d4dff), // red cap
    };

    /** Jetpack pickup. 1 = casing, 2 = tank, 3 = nozzle, 4 = flame. */
    public static final int[][] JETPACK = {
            {0,1,1,0,0,1,1,0},
            {1,2,2,1,1,2,2,1},
            {1,2,2,1,1,2,2,1},
            {1,2,2,1,1,2,2,1},
            {1,2,2,1,1,2,2,1},
            {1,1,1,3,3,1,1,1},
            {0,1,3,3,3,3,1,0},
            {0,0,4,4,4,4,0,0},
            {0,0,0,4,4,0,0,0},
            {0,0,0,0,4,0,0,0},
    };
    public static final Color[] JETPACK_PALETTE = {
            new Color(0x3a3a40ff), // casing
            new Color(0xc8ccd2ff), // tank silver
            new Color(0xff8c1aff), // nozzle orange
            new Color(0xffe24aff), // flame
    };
}
