package com.starbots.starjump.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Generates textures procedurally at runtime — no external image files needed.
 *
 * <ul>
 *   <li>{@link #softGlow} — a radial-gradient disc used (tinted) for stars,
 *       particles, projectiles and thruster flames.</li>
 *   <li>{@link #fromPattern} — crisp, scaled-up retro sprites from a small
 *       integer grid, cohesive with the original pixel-art astronaut.</li>
 * </ul>
 */
public final class PixelArt {
    private PixelArt() {}

    /** White radial glow, {@code size×size}, alpha fading to 0 at the edge. */
    public static Texture softGlow(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        float c = (size - 1) / 2f;
        float radius = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - c, dy = y - c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float t = Math.max(0f, 1f - d / radius);
                float a = t * t; // soft falloff
                pm.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }

    /**
     * Build a sprite from a pattern grid. Cell {@code 0} is transparent; any
     * other value indexes into {@code palette} (1-based). Each cell becomes a
     * {@code scale×scale} block; nearest filtering keeps the pixels crisp.
     */
    public static Texture fromPattern(int[][] pattern, int scale, Color... palette) {
        int rows = pattern.length;
        int cols = pattern[0].length;
        Pixmap pm = new Pixmap(cols * scale, rows * scale, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int v = pattern[r][col];
                if (v <= 0) continue;
                pm.setColor(palette[v - 1]);
                pm.fillRectangle(col * scale, r * scale, scale, scale);
            }
        }
        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        return tex;
    }
}
