package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class StratosphereBiome implements SkyBiome {

    private static final Color TOP = new Color(0.08f, 0.12f, 0.38f, 1f);
    private static final Color BOTTOM = new Color(0.22f, 0.40f, 0.72f, 1f);

    private static final int AURORA_BANDS = 7;
    private static final int WISP_COUNT = 8;
    private static final int STAR_COUNT = 25;
    private static final int METEOR_COUNT = 2;
    private static final int ATMO_BANDS = 4;

    // Aurora
    private final float[] auroraX = new float[AURORA_BANDS];
    private final float[] auroraPhase = new float[AURORA_BANDS];
    private final float[] auroraSpeed = new float[AURORA_BANDS];

    // Noctilucent wisps
    private final float[] wispX = new float[WISP_COUNT];
    private final float[] wispY = new float[WISP_COUNT];
    private final float[] wispW = new float[WISP_COUNT];
    private final float[] wispH = new float[WISP_COUNT];

    // Stars
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final float[] starTwinkleRate = new float[STAR_COUNT];
    private final float[] starR = new float[STAR_COUNT];
    private final float[] starG = new float[STAR_COUNT];
    private final float[] starB = new float[STAR_COUNT];

    // Meteors
    private final float[] meteorX = new float[METEOR_COUNT];
    private final float[] meteorY = new float[METEOR_COUNT];
    private final float[] meteorAngle = new float[METEOR_COUNT];
    private final float[] meteorPhase = new float[METEOR_COUNT];

    // Atmospheric band positions
    private final float[] atmoBandY = new float[ATMO_BANDS];
    private final float[] atmoBandR = new float[ATMO_BANDS];
    private final float[] atmoBandG = new float[ATMO_BANDS];
    private final float[] atmoBandB = new float[ATMO_BANDS];

    private Texture wispTex;
    private Texture auroraTex;
    private Texture dot;
    private Texture meteorTex;
    private Texture atmoBandTex;

    public StratosphereBiome() {
        MathUtils.random.setSeed(55L);

        // Aurora bands
        for (int i = 0; i < AURORA_BANDS; i++) {
            auroraX[i] = MathUtils.random(0f, 1f);
            auroraPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            auroraSpeed[i] = MathUtils.random(1.5f, 3.5f);
        }

        // Noctilucent wisps
        for (int i = 0; i < WISP_COUNT; i++) {
            wispX[i] = MathUtils.random(0f, 1f);
            wispY[i] = MathUtils.random(0.15f, 0.85f);
            wispW[i] = MathUtils.random(0.7f, 1.8f);
            wispH[i] = MathUtils.random(0.6f, 1.2f);
        }

        // Stars with color variety
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, 1f);
            starY[i] = MathUtils.random(0f, 1f);
            starSize[i] = MathUtils.random(1f, 3.5f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            starTwinkleRate[i] = MathUtils.random(3f, 8f);

            float hue = MathUtils.random(0f, 1f);
            if (hue < 0.55f) {
                // White
                starR[i] = 1f; starG[i] = 1f; starB[i] = 1f;
            } else if (hue < 0.75f) {
                // Blue-white
                starR[i] = 0.75f; starG[i] = 0.85f; starB[i] = 1f;
            } else if (hue < 0.90f) {
                // Warm yellow
                starR[i] = 1f; starG[i] = 0.92f; starB[i] = 0.65f;
            } else {
                // Pale blue
                starR[i] = 0.65f; starG[i] = 0.78f; starB[i] = 1f;
            }
        }

        // Meteors
        for (int i = 0; i < METEOR_COUNT; i++) {
            meteorX[i] = MathUtils.random(0.1f, 0.9f);
            meteorY[i] = MathUtils.random(0.4f, 0.9f);
            meteorAngle[i] = MathUtils.random(0.3f, 0.8f);
            meteorPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }

        // Atmospheric bands
        atmoBandY[0] = 0.15f; atmoBandR[0] = 0.12f; atmoBandG[0] = 0.22f; atmoBandB[0] = 0.52f;
        atmoBandY[1] = 0.35f; atmoBandR[1] = 0.15f; atmoBandG[1] = 0.28f; atmoBandB[1] = 0.58f;
        atmoBandY[2] = 0.55f; atmoBandR[2] = 0.10f; atmoBandG[2] = 0.18f; atmoBandB[2] = 0.48f;
        atmoBandY[3] = 0.75f; atmoBandR[3] = 0.14f; atmoBandG[3] = 0.20f; atmoBandB[3] = 0.45f;
    }

    @Override
    public String getName() {
        return "stratosphere";
    }

    @Override
    public float getStartAltitude() {
        return 5000f;
    }

    @Override
    public Color getTopColor() {
        return TOP;
    }

    @Override
    public Color getBottomColor() {
        return BOTTOM;
    }

    @Override
    public void renderDecorations(SpriteBatch batch, float cameraX, float cameraY,
                                  float viewW, float viewH, float alpha) {
        if (wispTex == null) {
            wispTex = buildWispTexture();
            auroraTex = buildAuroraTexture();
            meteorTex = buildMeteorTexture();
            atmoBandTex = buildAtmoBandTexture();
            Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fillCircle(2, 2, 2);
            dot = new Texture(pix);
            pix.dispose();
        }

        float baseY = cameraY - viewH * 0.5f;
        float parallax = cameraY * 0.1f;
        float time = cameraY * 0.0005f;

        // --- Atmospheric gradient bands ---
        for (int i = 0; i < ATMO_BANDS; i++) {
            float by = baseY + atmoBandY[i] * viewH;
            float bandAlpha = alpha * 0.06f;
            batch.setColor(atmoBandR[i], atmoBandG[i], atmoBandB[i], bandAlpha);
            batch.draw(atmoBandTex, 0, by, viewW, viewH * 0.12f);
        }

        // --- Aurora borealis ---
        for (int i = 0; i < AURORA_BANDS; i++) {
            float ax = auroraX[i] * viewW - 90f;
            float baseAy = baseY + viewH * (0.65f + i * 0.03f);
            float undulate = MathUtils.sin(time * auroraSpeed[i] + auroraPhase[i]) * 25f;
            float ay = baseAy + undulate;

            // Color cycling: shift between green, teal, purple, pink
            float colorPhase = time * 1.2f + i * 0.9f;
            float cMod = (MathUtils.sin(colorPhase) + 1f) * 0.5f;

            float ar, ag, ab;
            if (cMod < 0.25f) {
                // Green
                float t = cMod / 0.25f;
                ar = lerp(0.1f, 0.0f, t);
                ag = lerp(0.9f, 0.85f, t);
                ab = lerp(0.3f, 0.6f, t);
            } else if (cMod < 0.5f) {
                // Teal
                float t = (cMod - 0.25f) / 0.25f;
                ar = lerp(0.0f, 0.4f, t);
                ag = lerp(0.85f, 0.3f, t);
                ab = lerp(0.6f, 0.8f, t);
            } else if (cMod < 0.75f) {
                // Purple
                float t = (cMod - 0.5f) / 0.25f;
                ar = lerp(0.4f, 0.8f, t);
                ag = lerp(0.3f, 0.25f, t);
                ab = lerp(0.8f, 0.6f, t);
            } else {
                // Pink back to green
                float t = (cMod - 0.75f) / 0.25f;
                ar = lerp(0.8f, 0.1f, t);
                ag = lerp(0.25f, 0.9f, t);
                ab = lerp(0.6f, 0.3f, t);
            }

            float aAlpha = alpha * (0.12f + 0.08f * MathUtils.sin(time * 2.5f + auroraPhase[i]));
            batch.setColor(ar, ag, ab, aAlpha);
            batch.draw(auroraTex, ax, ay, 180, 100);
        }

        // --- Noctilucent clouds ---
        for (int i = 0; i < WISP_COUNT; i++) {
            float wx = wispX[i] * viewW;
            float wy = baseY + ((wispY[i] * viewH - parallax * 0.2f) % viewH + viewH) % viewH;
            float ww = wispTex.getWidth() * wispW[i];
            float wh = wispTex.getHeight() * wispH[i];

            // Electric-blue glow that pulses
            float glow = 0.7f + 0.3f * MathUtils.sin(time * 4f + i * 1.3f);
            batch.setColor(0.45f, 0.7f, 1f, alpha * 0.35f * glow);
            batch.draw(wispTex, wx, wy, ww, wh);

            // Bright core overlay
            batch.setColor(0.6f, 0.85f, 1f, alpha * 0.15f * glow);
            batch.draw(wispTex, wx + ww * 0.15f, wy + wh * 0.1f,
                ww * 0.7f, wh * 0.8f);
        }

        // --- Stars ---
        for (int i = 0; i < STAR_COUNT; i++) {
            float sx = starX[i] * viewW;
            float sy = baseY + ((starY[i] * viewH - parallax * 0.05f) % viewH + viewH) % viewH;
            float ss = starSize[i];
            float twinkle = 0.3f + 0.7f * (0.5f + 0.5f *
                MathUtils.sin(time * starTwinkleRate[i] + starPhase[i]));

            batch.setColor(starR[i], starG[i], starB[i], alpha * 0.4f * twinkle);
            batch.draw(dot, sx, sy, ss, ss);

            // Cross glow for larger stars
            if (ss > 2.5f) {
                float glowAlpha = alpha * twinkle * 0.12f;
                batch.setColor(starR[i], starG[i], starB[i], glowAlpha);
                batch.draw(dot, sx - ss * 0.4f, sy + ss * 0.3f, ss * 1.8f, ss * 0.4f);
                batch.draw(dot, sx + ss * 0.3f, sy - ss * 0.4f, ss * 0.4f, ss * 1.8f);
            }
        }

        // --- Meteor streaks ---
        for (int i = 0; i < METEOR_COUNT; i++) {
            // Meteors appear intermittently via a cycling phase
            float cycle = MathUtils.sin(time * 0.8f + meteorPhase[i]);
            if (cycle > 0.85f) {
                // Active streak: map cycle [0.85..1] to streak progress [0..1]
                float progress = (cycle - 0.85f) / 0.15f;

                float mx = meteorX[i] * viewW + progress * viewW * 0.3f;
                float my = baseY + meteorY[i] * viewH - progress * viewH * 0.15f;

                // Orange-white head
                float headAlpha = alpha * 0.7f * (1f - progress * 0.5f);
                batch.setColor(1f, 0.85f, 0.5f, headAlpha);
                batch.draw(meteorTex, mx, my, 18, 4);

                // Orange trail behind
                float trailAlpha = alpha * 0.4f * (1f - progress);
                batch.setColor(1f, 0.55f, 0.15f, trailAlpha);
                batch.draw(meteorTex, mx - 22, my + 1, 24, 3);

                // Faint red tail
                float tailAlpha = alpha * 0.15f * (1f - progress);
                batch.setColor(1f, 0.3f, 0.1f, tailAlpha);
                batch.draw(meteorTex, mx - 42, my + 2, 22, 2);
            }
        }

        batch.setColor(Color.WHITE);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Texture buildWispTexture() {
        int w = 120, h = 10;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int x = 0; x < w; x++) {
            float t = (float) x / w;
            float xFade = (float) Math.sin(t * Math.PI);
            xFade *= xFade; // sharper falloff at edges
            for (int y = 0; y < h; y++) {
                float yFade = 1f - Math.abs(2f * y / (float) h - 1f);
                float a = xFade * yFade * 0.75f;
                if (a > 0.01f) {
                    px.setColor(new Color(0.55f, 0.78f, 1f, a));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildAuroraTexture() {
        int w = 180, h = 100;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int y = 0; y < h; y++) {
            float yNorm = (float) y / h;
            // Vertical fade: strongest in middle, fades at top/bottom
            float yFade = (float) Math.sin(yNorm * Math.PI);
            yFade = yFade * yFade;

            for (int x = 0; x < w; x++) {
                float xNorm = (float) x / w;
                // Horizontal fade at edges
                float xFade = (float) Math.sin(xNorm * Math.PI);
                xFade = (float) Math.sqrt(xFade);

                // Undulating curtain effect via sine distortion
                float curtain = 0.7f + 0.3f * (float) Math.sin(xNorm * 8.0 + yNorm * 3.0);

                // Color gradient: green at top, teal in middle, hints of purple at bottom
                float r = 0.1f + yNorm * 0.35f;
                float g = 0.85f - yNorm * 0.45f;
                float b = 0.4f + yNorm * 0.3f;

                float finalAlpha = xFade * yFade * curtain * 0.45f;
                if (finalAlpha > 0.005f) {
                    px.setColor(new Color(r, g, b, finalAlpha));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildMeteorTexture() {
        int w = 24, h = 4;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int x = 0; x < w; x++) {
            float t = (float) x / w;
            // Brighter at the leading edge (right side)
            float intensity = t * t;
            for (int y = 0; y < h; y++) {
                float yFade = 1f - Math.abs(2f * y / (float) h - 1f);
                float a = intensity * yFade * 0.9f;
                if (a > 0.01f) {
                    px.setColor(new Color(1f, 0.9f * t + 0.6f * (1f - t),
                        0.4f * t, a));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildAtmoBandTexture() {
        int w = 4, h = 32;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int y = 0; y < h; y++) {
            float yNorm = (float) y / h;
            float fade = (float) Math.sin(yNorm * Math.PI);
            fade *= fade;
            if (fade > 0.01f) {
                px.setColor(new Color(1f, 1f, 1f, fade * 0.5f));
                px.fillRectangle(0, y, w, 1);
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }
}
