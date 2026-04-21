package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class SunBiome implements SkyBiome {

    private static final Color TOP = new Color(0.92f, 0.35f, 0.15f, 1f);
    private static final Color BOTTOM = new Color(1.00f, 0.72f, 0.15f, 1f);

    private static final int FLARE_COUNT = 8;
    private static final int WIND_COUNT = 35;
    private static final int SHIMMER_COUNT = 35;

    // Flare properties
    private final float[] flareAngle = new float[FLARE_COUNT];
    private final float[] flareLength = new float[FLARE_COUNT];
    private final float[] flareWidth = new float[FLARE_COUNT];
    private final float[] flareSpeed = new float[FLARE_COUNT];

    // Solar wind particle properties
    private final float[] windAngle = new float[WIND_COUNT];
    private final float[] windDist = new float[WIND_COUNT];
    private final float[] windSpeed = new float[WIND_COUNT];
    private final float[] windSize = new float[WIND_COUNT];
    private final float[] windPhase = new float[WIND_COUNT];

    // Heat shimmer properties
    private final float[] shimmerX = new float[SHIMMER_COUNT];
    private final float[] shimmerY = new float[SHIMMER_COUNT];
    private final float[] shimmerSize = new float[SHIMMER_COUNT];
    private final float[] shimmerPhase = new float[SHIMMER_COUNT];

    private Texture sunTex;
    private Texture coronaTex;
    private Texture flareTex;
    private Texture chromosphereTex;
    private Texture dot;

    public SunBiome() {
        MathUtils.random.setSeed(99L);
        for (int i = 0; i < FLARE_COUNT; i++) {
            flareAngle[i] = MathUtils.random(0f, MathUtils.PI2);
            flareLength[i] = MathUtils.random(90f, 260f);
            flareWidth[i] = MathUtils.random(18f, 55f);
            flareSpeed[i] = MathUtils.random(0.15f, 0.45f);
        }
        for (int i = 0; i < WIND_COUNT; i++) {
            windAngle[i] = MathUtils.random(0f, MathUtils.PI2);
            windDist[i] = MathUtils.random(0f, 1f);
            windSpeed[i] = MathUtils.random(40f, 120f);
            windSize[i] = MathUtils.random(1.5f, 4f);
            windPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
        for (int i = 0; i < SHIMMER_COUNT; i++) {
            shimmerX[i] = MathUtils.random(0f, 1f);
            shimmerY[i] = MathUtils.random(0f, 1f);
            shimmerSize[i] = MathUtils.random(2f, 8f);
            shimmerPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
    }

    @Override
    public String getName() {
        return "sun";
    }

    @Override
    public float getStartAltitude() {
        return 35000f;
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
        if (sunTex == null) {
            sunTex = buildSunTexture();
            coronaTex = buildCoronaTexture();
            flareTex = buildFlareTexture();
            chromosphereTex = buildChromosphereTexture();
            Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fillCircle(2, 2, 2);
            dot = new Texture(pix);
            pix.dispose();
        }

        float time = cameraY * 0.0002f;
        float sx = viewW * 0.5f;
        float sy = cameraY + viewH * 0.12f;
        float sunRadius = 160f;

        // --- Layer 1: Outer corona (largest, faintest) ---
        float corona3Size = 520 + MathUtils.sin(time * 0.8f) * 40f;
        batch.setColor(1f, 0.75f, 0.30f, alpha * 0.03f);
        batch.draw(coronaTex, sx - corona3Size * 0.5f, sy - corona3Size * 0.5f, corona3Size, corona3Size);

        // --- Layer 2: Mid corona ---
        float corona2Size = 440 + MathUtils.sin(time * 1.3f + 1f) * 30f;
        batch.setColor(1f, 0.82f, 0.38f, alpha * 0.05f);
        batch.draw(coronaTex, sx - corona2Size * 0.5f, sy - corona2Size * 0.5f, corona2Size, corona2Size);

        // --- Layer 3: Inner corona ---
        float corona1Size = 370 + MathUtils.sin(time * 1.8f + 2f) * 20f;
        batch.setColor(1f, 0.88f, 0.50f, alpha * 0.08f);
        batch.draw(coronaTex, sx - corona1Size * 0.5f, sy - corona1Size * 0.5f, corona1Size, corona1Size);

        // --- Solar wind particles streaming outward ---
        for (int i = 0; i < WIND_COUNT; i++) {
            float angle = windAngle[i];
            float maxDist = 200f + windSize[i] * 30f;
            float rawDist = (windDist[i] * maxDist + time * windSpeed[i]) % maxDist;
            float dist = sunRadius * 0.5f + rawDist;
            float wx = sx + MathUtils.cos(angle) * dist;
            float wy = sy + MathUtils.sin(angle) * dist;
            float distFade = 1f - rawDist / maxDist;
            float flicker = 0.5f + 0.5f * MathUtils.sin(time * 8f + windPhase[i]);
            float wAlpha = alpha * 0.12f * distFade * flicker;
            batch.setColor(1f, 0.90f, 0.55f, wAlpha);
            batch.draw(dot, wx - windSize[i] * 0.5f, wy - windSize[i] * 0.5f, windSize[i], windSize[i]);
        }

        // --- Solar flares / prominences ---
        for (int i = 0; i < FLARE_COUNT; i++) {
            float angle = flareAngle[i] + time * flareSpeed[i];
            float breathe = MathUtils.sin(time * 3f + i * 1.7f);
            float len = flareLength[i] + breathe * 40f;
            float fw = flareWidth[i] + breathe * 5f;
            float baseR = sunRadius * 0.45f;
            float fx = sx + MathUtils.cos(angle) * baseR;
            float fy = sy + MathUtils.sin(angle) * baseR;
            float flareAlpha = alpha * (0.10f + 0.06f * MathUtils.sin(time * 4f + i * 2.3f));

            // Draw with rotation: origin at bottom-center of the flare texture
            float originX = fw * 0.5f;
            float originY = 0f;
            float angleDeg = angle * MathUtils.radiansToDegrees - 90f;
            batch.setColor(1f, 0.88f, 0.45f, flareAlpha);
            batch.draw(flareTex,
                fx - originX, fy - originY,
                originX, originY,
                fw, len,
                1f, 1f,
                angleDeg,
                0, 0, flareTex.getWidth(), flareTex.getHeight(),
                false, false);

            // Secondary glow layer for thickness
            batch.setColor(1f, 0.70f, 0.30f, flareAlpha * 0.5f);
            batch.draw(flareTex,
                fx - originX * 1.3f, fy - originY,
                originX * 1.3f, originY,
                fw * 1.3f, len * 0.8f,
                1f, 1f,
                angleDeg,
                0, 0, flareTex.getWidth(), flareTex.getHeight(),
                false, false);
        }

        // --- Chromosphere ring ---
        float chromoSize = sunRadius * 2.15f;
        float chromoPulse = 0.7f + 0.3f * MathUtils.sin(time * 2.5f);
        batch.setColor(1f, 0.35f, 0.50f, alpha * 0.18f * chromoPulse);
        batch.draw(chromosphereTex,
            sx - chromoSize * 0.5f, sy - chromoSize * 0.5f,
            chromoSize, chromoSize);

        // --- Main sun body ---
        batch.setColor(1f, 1f, 1f, alpha);
        float sunDraw = sunRadius * 2f * 0.9f;
        batch.draw(sunTex, sx - sunDraw * 0.5f, sy - sunDraw * 0.5f, sunDraw, sunDraw);

        // --- Heat shimmer particles ---
        for (int i = 0; i < SHIMMER_COUNT; i++) {
            float hx = shimmerX[i] * viewW;
            float drift = MathUtils.sin(time * 3f + shimmerPhase[i] * 2f) * 8f;
            float hy = cameraY - viewH * 0.5f
                + ((shimmerY[i] * viewH + time * 15f + i * 7f) % viewH + viewH) % viewH;
            float hs = shimmerSize[i];
            float flicker = 0.5f + 0.5f * MathUtils.sin(time * 7f + shimmerPhase[i]);
            float hAlpha = alpha * 0.10f * flicker;
            batch.setColor(1f, 0.95f, 0.65f, hAlpha);
            batch.draw(dot, hx + drift, hy, hs, hs * 1.5f);
        }

        batch.setColor(Color.WHITE);
    }

    // ---- Texture builders ----

    /** Builds a 160-radius sun with multi-layer core, sunspots, and granulation. */
    private static Texture buildSunTexture() {
        int r = 160;
        int size = r * 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        // Limb-darkened disc: red edge fading inward through orange to yellow
        for (int i = r; i > 0; i--) {
            float t = 1f - (float) i / r;  // 0 at edge, 1 at center
            float red = 1f;
            float green, blue, a;
            if (t < 0.15f) {
                // Outermost: faint red glow (limb darkening zone)
                a = 0.05f + t * 0.6f;
                green = 0.25f + t * 1.0f;
                blue = 0.05f + t * 0.3f;
            } else if (t < 0.4f) {
                // Red-to-orange transition
                float localT = (t - 0.15f) / 0.25f;
                a = 0.15f + localT * 0.55f;
                green = 0.40f + localT * 0.30f;
                blue = 0.10f + localT * 0.10f;
            } else if (t < 0.7f) {
                // Orange-to-yellow band
                float localT = (t - 0.4f) / 0.3f;
                a = 0.70f + localT * 0.25f;
                green = 0.70f + localT * 0.22f;
                blue = 0.20f + localT * 0.40f;
            } else {
                // Yellow-to-white-hot center
                float localT = (t - 0.7f) / 0.3f;
                a = 0.95f + localT * 0.05f;
                green = 0.92f + localT * 0.08f;
                blue = 0.60f + localT * 0.35f;
            }
            pix.setColor(new Color(red, Math.min(green, 1f), Math.min(blue, 1f), Math.min(a, 1f)));
            pix.fillCircle(r, r, i);
        }

        // Surface granulation: many small semi-transparent blobs
        MathUtils.random.setSeed(42L);
        for (int g = 0; g < 120; g++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float dist = MathUtils.random(0f, r * 0.78f);
            int gx = r + (int)(MathUtils.cos(angle) * dist);
            int gy = r + (int)(MathUtils.sin(angle) * dist);
            int gr = MathUtils.random(2, 8);
            float bright = MathUtils.random(0.85f, 1.05f);
            float ga = MathUtils.random(0.04f, 0.12f);
            pix.setColor(new Color(
                Math.min(bright, 1f),
                Math.min(bright * 0.92f, 1f),
                Math.min(bright * 0.55f, 1f),
                ga));
            pix.fillCircle(gx, gy, gr);
        }

        // White-hot core
        pix.setColor(new Color(1f, 1f, 0.97f, 1f));
        pix.fillCircle(r, r, (int)(r * 0.22f));
        pix.setColor(new Color(1f, 1f, 1f, 0.6f));
        pix.fillCircle(r, r, (int)(r * 0.30f));

        // Sunspots: 7 dark spots of varied sizes
        int[][] spots = {
            {r - 35, r + 18, 10},
            {r + 40, r - 10, 8},
            {r + 8, r + 35, 7},
            {r - 18, r - 30, 9},
            {r + 55, r + 25, 6},
            {r - 50, r - 5, 5},
            {r + 20, r - 40, 7}
        };
        for (int[] spot : spots) {
            // Darker umbra
            pix.setColor(new Color(0.55f, 0.30f, 0.10f, 0.50f));
            pix.fillCircle(spot[0], spot[1], spot[2]);
            // Even darker core
            pix.setColor(new Color(0.35f, 0.18f, 0.05f, 0.45f));
            pix.fillCircle(spot[0], spot[1], (int)(spot[2] * 0.55f));
            // Penumbra ring (lighter ring around)
            pix.setColor(new Color(0.70f, 0.45f, 0.15f, 0.20f));
            pix.fillCircle(spot[0], spot[1], (int)(spot[2] * 1.5f));
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    /** Soft radial glow used for corona layers. */
    private static Texture buildCoronaTexture() {
        int r = 128;
        int size = r * 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        for (int i = r; i > 0; i--) {
            float t = (float) i / r;  // 1 at edge, 0 at center
            float a = (1f - t) * (1f - t);  // quadratic falloff
            pix.setColor(new Color(1f, 0.92f, 0.70f, a * 0.5f));
            pix.fillCircle(r, r, i);
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    /** Flare texture (20x100) with a curved, tapered shape. */
    private static Texture buildFlareTexture() {
        int w = 20, h = 100;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        float halfW = w / 2f;
        for (int y = 0; y < h; y++) {
            float t = (float) y / h;
            // Tapers with a smooth curve
            float fade = (1f - t);
            fade = fade * fade * (3f - 2f * fade);  // smoothstep-like falloff
            // Slight curve offset for organic look
            float curveOffset = MathUtils.sin(t * MathUtils.PI) * 2f;
            for (int x = 0; x < w; x++) {
                float dx = (x - halfW + curveOffset) / halfW;
                float xFade = Math.max(0f, 1f - dx * dx);
                int a = (int)(fade * xFade * 180);
                if (a > 0) {
                    // Brighter at base, more orange at tip
                    float green = 0.92f - t * 0.20f;
                    float blue = 0.55f - t * 0.35f;
                    px.setColor(new Color(1f, green, Math.max(blue, 0f), a / 255f));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    /** Thin chromosphere ring: reddish-pink annulus. */
    private static Texture buildChromosphereTexture() {
        int r = 100;
        int size = r * 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        int cx = r, cy = r;
        float innerR = r * 0.82f;
        float outerR = r * 0.95f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist >= innerR && dist <= outerR) {
                    // Smooth falloff from ring center
                    float ringCenter = (innerR + outerR) * 0.5f;
                    float ringHalf = (outerR - innerR) * 0.5f;
                    float ringT = 1f - Math.abs(dist - ringCenter) / ringHalf;
                    ringT = ringT * ringT;
                    float a = ringT * 0.7f;
                    // Reddish-pink with slight variation
                    pix.setColor(new Color(1f, 0.25f, 0.40f, a));
                    pix.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }
}
