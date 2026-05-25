package com.starbots.starjump.util;

import com.badlogic.gdx.graphics.Color;

/** UI palette ported from the original StyleSheet/LinearGradient values. */
public final class Colors {
    private Colors() {}

    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;

    // "Iniciar" button gradient (#ffde59 -> #ffe47a)
    public static final Color YELLOW_TOP = rgb(0xffde59);
    public static final Color YELLOW_BOT = rgb(0xffe47a);

    // "Conquistas" / "Voltar" button gradient (#eeede9 -> #d5d2c9)
    public static final Color GRAY_TOP = rgb(0xeeede9);
    public static final Color GRAY_BOT = rgb(0xd5d2c9);

    // Translucent card background (#ffffff at 0x90 alpha).
    public static final Color CARD_BG = new Color(1f, 1f, 1f, 0x90 / 255f);

    // Unlocked curiosity card (#61cb46 -> #50a93b).
    public static final Color GREEN_TOP = rgb(0x61cb46);
    public static final Color GREEN_BOT = rgb(0x50a93b);

    // Locked curiosity card (#e2e0da -> #c6c3b7).
    public static final Color LOCKED_TOP = rgb(0xe2e0da);
    public static final Color LOCKED_BOT = rgb(0xc6c3b7);

    // In-card "Curiosidades" button (RkButton rkType 'start' -> #cdc739).
    public static final Color OLIVE_TOP = rgb(0xd8d24a);
    public static final Color OLIVE_BOT = rgb(0xcdc739);

    // "Novo recorde!" highlight (#ffde59).
    public static final Color NEW_RECORD = rgb(0xffde59);

    public static final Color DIM = new Color(0f, 0f, 0f, 0.35f);

    private static Color rgb(int hex) {
        return new Color(
                ((hex >> 16) & 0xff) / 255f,
                ((hex >> 8) & 0xff) / 255f,
                (hex & 0xff) / 255f,
                1f);
    }
}
