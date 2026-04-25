package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class MoonBiome implements SkyBiome {

    private static final Color TOP = new Color(0.03f, 0.03f, 0.08f, 1f);
    private static final Color BOTTOM = new Color(0.12f, 0.14f, 0.25f, 1f);

    private static final int ROCK_COUNT = 12;
    private static final int STAR_COUNT = 40;
    private static final int DUST_COUNT = 50;

    // Asteroid debris data
    private final float[] rockX = new float[ROCK_COUNT];
    private final float[] rockY = new float[ROCK_COUNT];
    private final float[] rockScale = new float[ROCK_COUNT];
    private final float[] rockRotSpeed = new float[ROCK_COUNT];
    private final int[] rockType = new int[ROCK_COUNT]; // 0=small, 1=medium, 2=large

    // Star data
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];

    // Lunar dust data
    private final float[] dustX = new float[DUST_COUNT];
    private final float[] dustY = new float[DUST_COUNT];
    private final float[] dustSize = new float[DUST_COUNT];
    private final float[] dustPhase = new float[DUST_COUNT];
    private final float[] dustDrift = new float[DUST_COUNT];

    private Texture moonTex;
    private Texture moonGlowTex;
    private Texture rockSmallTex;
    private Texture rockMedTex;
    private Texture rockLargeTex;
    private Texture earthTex;
    private Texture dot;

    public MoonBiome() {
        MathUtils.random.setSeed(77L);
        for (int i = 0; i < ROCK_COUNT; i++) {
            rockX[i] = MathUtils.random(0f, 1f);
            rockY[i] = MathUtils.random(0.05f, 0.95f);
            rockScale[i] = MathUtils.random(0.7f, 1.4f);
            rockRotSpeed[i] = MathUtils.random(-3f, 3f);
            rockType[i] = i % 3;
        }
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, 1f);
            starY[i] = MathUtils.random(0f, 1f);
            starSize[i] = MathUtils.random(1f, 2.5f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
        for (int i = 0; i < DUST_COUNT; i++) {
            dustX[i] = MathUtils.random(0f, 1f);
            dustY[i] = MathUtils.random(0f, 1f);
            dustSize[i] = MathUtils.random(0.5f, 1.8f);
            dustPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            dustDrift[i] = MathUtils.random(-0.5f, 0.5f);
        }
    }

    @Override
    public String getName() {
        return "moon";
    }

    @Override
    public float getStartAltitude() {
        return 20000f;
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
        if (moonTex == null) initTextures();

        float baseY = cameraY - viewH * 0.5f;
        float time = cameraY * 0.0003f;
        float parallax = cameraY * 0.04f;

        // --- Dim stars (moon washes them out) ---
        MathUtils.random.setSeed(777L);
        for (int i = 0; i < STAR_COUNT; i++) {
            float sx = starX[i] * viewW;
            float sy = baseY + ((starY[i] * viewH - parallax * 0.3f) % viewH + viewH) % viewH;
            float twinkle = 0.15f + 0.15f * MathUtils.sin(time * 5f + starPhase[i]);
            batch.setColor(0.85f, 0.88f, 0.95f, alpha * twinkle);
            batch.draw(dot, sx, sy, starSize[i], starSize[i]);
        }

        // --- Distant Earth ---
        float earthX = viewW * 0.12f;
        float earthY = baseY + ((0.72f * viewH - parallax * 0.1f) % viewH + viewH) % viewH;
        batch.setColor(1f, 1f, 1f, alpha * 0.7f);
        batch.draw(earthTex, earthX, earthY, 80f, 80f);

        // --- Moon glow (behind moon) ---
        float mx = viewW * 0.62f;
        float my = cameraY + viewH * 0.12f;
        float moonDrawSize = 220f * 2f * 0.85f;
        float glowSize = moonDrawSize * 1.8f;
        float glowOff = (glowSize - moonDrawSize) * 0.5f;
        batch.setColor(0.75f, 0.80f, 0.95f, alpha * 0.06f);
        batch.draw(moonGlowTex, mx - glowOff, my - glowOff, glowSize, glowSize);
        // Second tighter glow layer
        float glow2 = moonDrawSize * 1.35f;
        float glow2Off = (glow2 - moonDrawSize) * 0.5f;
        batch.setColor(0.82f, 0.85f, 0.97f, alpha * 0.10f);
        batch.draw(moonGlowTex, mx - glow2Off, my - glow2Off, glow2, glow2);

        // --- Moon ---
        batch.setColor(1f, 1f, 1f, alpha * 0.95f);
        batch.draw(moonTex, mx, my, moonDrawSize, moonDrawSize);

        // --- Lunar dust near the moon ---
        for (int i = 0; i < DUST_COUNT; i++) {
            float dx = dustX[i] * viewW;
            float dy = baseY + ((dustY[i] * viewH + MathUtils.sin(time * 2f + dustPhase[i]) * 8f) % viewH + viewH) % viewH;
            dx += MathUtils.sin(time * 1.2f + dustPhase[i]) * dustDrift[i] * 20f;
            float dAlpha = alpha * (0.04f + 0.06f * MathUtils.sin(time * 3f + dustPhase[i]));
            batch.setColor(0.80f, 0.82f, 0.88f, dAlpha);
            batch.draw(dot, dx, dy, dustSize[i], dustSize[i]);
        }

        // --- Floating asteroid debris ---
        for (int i = 0; i < ROCK_COUNT; i++) {
            float rx = rockX[i] * viewW;
            float ry = baseY + ((rockY[i] * viewH + MathUtils.sin(time + i * 1.7f) * 18f) % viewH + viewH) % viewH;
            float s = rockScale[i];
            Texture rock = rockType[i] == 0 ? rockSmallTex : rockType[i] == 1 ? rockMedTex : rockLargeTex;
            float rw = rock.getWidth() * s;
            float rh = rock.getHeight() * s;
            float rot = (time * rockRotSpeed[i] * 30f) % 360f;

            batch.setColor(0.55f, 0.52f, 0.50f, alpha * 0.7f);
            batch.draw(rock,
                rx - rw * 0.5f, ry - rh * 0.5f,
                rw * 0.5f, rh * 0.5f,
                rw, rh,
                1f, 1f,
                rot,
                0, 0, rock.getWidth(), rock.getHeight(),
                false, false);
        }

        batch.setColor(Color.WHITE);
    }

    // ---- Texture generation ----

    private void initTextures() {
        moonTex = buildMoonTexture();
        moonGlowTex = buildGlowTexture();
        rockSmallTex = buildRockTexture(12, 0);
        rockMedTex = buildRockTexture(20, 1);
        rockLargeTex = buildRockTexture(28, 2);
        earthTex = buildEarthTexture();

        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fillCircle(2, 2, 2);
        dot = new Texture(pix);
        pix.dispose();
    }

    private static Texture buildMoonTexture() {
        int radius = 220;
        int size = radius * 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        int cx = radius, cy = radius;

        // Base moon body -- warm white on lit side
        pix.setColor(new Color(0.92f, 0.91f, 0.87f, 1f));
        pix.fillCircle(cx, cy, radius - 2);

        // Surface texture variation: subtle noise-like patches
        MathUtils.random.setSeed(123L);
        for (int i = 0; i < 80; i++) {
            float angle = MathUtils.random(MathUtils.PI2);
            float dist = MathUtils.random(0f, (float)(radius - 10));
            int px = cx + (int)(MathUtils.cos(angle) * dist);
            int py = cy + (int)(MathUtils.sin(angle) * dist);
            int pr = MathUtils.random(4, 18);
            float shade = MathUtils.random(0.82f, 0.96f);
            pix.setColor(new Color(shade, shade, shade * 0.95f, 0.15f));
            pix.fillCircle(px, py, pr);
        }

        // Mare (dark patches) -- large dark basaltic regions
        drawMare(pix, cx - 50, cy - 30, 55, 45);
        drawMare(pix, cx + 40, cy + 50, 40, 35);
        drawMare(pix, cx - 20, cy + 70, 35, 25);
        drawMare(pix, cx + 70, cy - 50, 30, 40);
        drawMare(pix, cx - 70, cy + 20, 25, 30);

        // Terminator line: day/night boundary -- right side fades to blue-gray shadow
        float terminatorX = cx + radius * 0.2f; // lit up to ~20% right of center
        for (int y = 0; y < size; y++) {
            for (int x = (int) terminatorX; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > radius - 2) continue;

                float shadowFactor = (x - terminatorX) / (float)(size - terminatorX);
                shadowFactor = Math.min(shadowFactor * 1.5f, 1f);

                int existing = pix.getPixel(x, y);
                float er = ((existing >>> 24) & 0xFF) / 255f;
                float eg = ((existing >>> 16) & 0xFF) / 255f;
                float eb = ((existing >>> 8) & 0xFF) / 255f;
                float ea = (existing & 0xFF) / 255f;

                if (ea < 0.01f) continue;

                // Blend toward blue-gray shadow
                float sr = 0.25f, sg = 0.28f, sb = 0.38f;
                float r = MathUtils.lerp(er, sr, shadowFactor * 0.85f);
                float g = MathUtils.lerp(eg, sg, shadowFactor * 0.85f);
                float b = MathUtils.lerp(eb, sb, shadowFactor * 0.85f);

                pix.setColor(new Color(r, g, b, ea));
                pix.drawPixel(x, y);
            }
        }

        // Highlight on the lit side (top-left warm glow)
        pix.setColor(new Color(0.98f, 0.97f, 0.93f, 0.25f));
        pix.fillCircle(cx - 60, cy - 60, 60);
        pix.setColor(new Color(1f, 0.99f, 0.95f, 0.15f));
        pix.fillCircle(cx - 40, cy - 40, 80);

        // Craters with shadow/highlight rims (12 craters)
        drawCrater(pix, cx - 85, cy - 55, 24);
        drawCrater(pix, cx + 20, cy - 80, 18);
        drawCrater(pix, cx - 40, cy + 30, 28);
        drawCrater(pix, cx + 60, cy - 10, 15);
        drawCrater(pix, cx - 90, cy + 60, 20);
        drawCrater(pix, cx + 10, cy + 85, 22);
        drawCrater(pix, cx - 25, cy - 100, 14);
        drawCrater(pix, cx + 80, cy + 60, 12);
        drawCrater(pix, cx - 60, cy + 100, 16);
        drawCrater(pix, cx + 40, cy + 30, 10);
        drawCrater(pix, cx - 10, cy - 40, 12);
        drawCrater(pix, cx + 100, cy - 30, 9);

        // Clip to circle (clear outside)
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                if (dx * dx + dy * dy > (radius - 1) * (radius - 1)) {
                    pix.setColor(0, 0, 0, 0);
                    pix.drawPixel(x, y);
                }
            }
        }

        // Limb darkening: soft edge falloff
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > radius - 6 && d < radius) {
                    float fade = (d - (radius - 6)) / 6f;
                    int existing = pix.getPixel(x, y);
                    float ea = (existing & 0xFF) / 255f;
                    if (ea > 0.01f) {
                        float er = ((existing >>> 24) & 0xFF) / 255f;
                        float eg = ((existing >>> 16) & 0xFF) / 255f;
                        float eb = ((existing >>> 8) & 0xFF) / 255f;
                        pix.setColor(new Color(er * (1f - fade * 0.3f), eg * (1f - fade * 0.3f), eb * (1f - fade * 0.3f), ea * (1f - fade * 0.5f)));
                        pix.drawPixel(x, y);
                    }
                }
            }
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    private static void drawMare(Pixmap pix, int cx, int cy, int rx, int ry) {
        // Dark basaltic mare region (elliptical)
        for (int y = cy - ry; y <= cy + ry; y++) {
            for (int x = cx - rx; x <= cx + rx; x++) {
                if (x < 0 || x >= pix.getWidth() || y < 0 || y >= pix.getHeight()) continue;
                float dx = (float)(x - cx) / rx;
                float dy = (float)(y - cy) / ry;
                float d = dx * dx + dy * dy;
                if (d < 1f) {
                    float intensity = (1f - d) * 0.3f;
                    int existing = pix.getPixel(x, y);
                    float ea = (existing & 0xFF) / 255f;
                    if (ea < 0.01f) continue;
                    float er = ((existing >>> 24) & 0xFF) / 255f;
                    float eg = ((existing >>> 16) & 0xFF) / 255f;
                    float eb = ((existing >>> 8) & 0xFF) / 255f;
                    // Darken with slight blue tint
                    pix.setColor(new Color(
                        er * (1f - intensity * 0.7f),
                        eg * (1f - intensity * 0.6f),
                        eb * (1f - intensity * 0.4f),
                        ea));
                    pix.drawPixel(x, y);
                }
            }
        }
    }

    private static void drawCrater(Pixmap pix, int cx, int cy, int r) {
        // Shadow rim (upper-right, as light comes from upper-left)
        pix.setColor(new Color(0.55f, 0.54f, 0.50f, 0.5f));
        pix.fillCircle(cx + 2, cy + 2, r);

        // Crater floor
        pix.setColor(new Color(0.72f, 0.71f, 0.67f, 0.6f));
        pix.fillCircle(cx, cy, r - 1);

        // Inner shadow (darker center)
        pix.setColor(new Color(0.65f, 0.64f, 0.60f, 0.35f));
        pix.fillCircle(cx + 1, cy + 1, r - 3);

        // Highlight rim (upper-left, facing the light)
        pix.setColor(new Color(0.96f, 0.95f, 0.92f, 0.4f));
        pix.drawCircle(cx - 1, cy - 1, r);
        pix.drawCircle(cx - 2, cy - 2, r - 1);

        // Tiny central peak for larger craters
        if (r >= 16) {
            pix.setColor(new Color(0.85f, 0.84f, 0.80f, 0.4f));
            pix.fillCircle(cx, cy, 3);
        }
    }

    private static Texture buildGlowTexture() {
        int size = 256;
        int cx = size / 2, cy = size / 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        float maxR = size / 2f;
        // Radial gradient glow -- cool white/blue
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < maxR) {
                    float t = d / maxR;
                    float a = (1f - t * t) * 0.5f; // quadratic falloff
                    pix.setColor(new Color(0.85f, 0.88f, 0.98f, a));
                    pix.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    private static Texture buildRockTexture(int baseSize, int variant) {
        int s = baseSize + 4; // padding
        Pixmap px = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int half = s / 2;

        switch (variant) {
            case 0: // Small jagged rock
                px.setColor(new Color(0.50f, 0.47f, 0.42f, 1f));
                px.fillCircle(half, half + 1, baseSize / 2 - 1);
                px.fillCircle(half + 2, half - 2, baseSize / 2 - 2);
                px.fillCircle(half - 2, half, baseSize / 2 - 3);
                // Surface detail
                px.setColor(new Color(0.60f, 0.57f, 0.52f, 0.6f));
                px.fillCircle(half - 1, half - 1, baseSize / 4);
                px.setColor(new Color(0.40f, 0.38f, 0.34f, 0.4f));
                px.fillCircle(half + 1, half + 2, baseSize / 5);
                break;

            case 1: // Medium elongated rock
                px.setColor(new Color(0.52f, 0.48f, 0.44f, 1f));
                px.fillCircle(half - 3, half, baseSize / 2 - 2);
                px.fillCircle(half + 3, half, baseSize / 2 - 2);
                px.fillCircle(half, half - 1, baseSize / 2 - 1);
                // Cracks / detail
                px.setColor(new Color(0.42f, 0.40f, 0.36f, 0.5f));
                px.drawLine(half - 4, half - 2, half + 3, half + 3);
                px.setColor(new Color(0.62f, 0.58f, 0.54f, 0.5f));
                px.fillCircle(half - 2, half - 3, baseSize / 5);
                px.setColor(new Color(0.45f, 0.42f, 0.38f, 0.3f));
                px.fillCircle(half + 2, half + 1, baseSize / 6);
                break;

            case 2: // Large chunky rock
                px.setColor(new Color(0.48f, 0.45f, 0.40f, 1f));
                px.fillCircle(half, half, baseSize / 2 - 1);
                px.fillCircle(half - 3, half + 3, baseSize / 2 - 3);
                px.fillCircle(half + 4, half - 2, baseSize / 2 - 2);
                px.fillCircle(half - 1, half - 4, baseSize / 2 - 3);
                // Surface bumps
                px.setColor(new Color(0.58f, 0.55f, 0.50f, 0.5f));
                px.fillCircle(half - 4, half - 2, baseSize / 4);
                px.setColor(new Color(0.38f, 0.36f, 0.32f, 0.4f));
                px.fillCircle(half + 3, half + 2, baseSize / 5);
                px.setColor(new Color(0.55f, 0.52f, 0.48f, 0.35f));
                px.fillCircle(half + 1, half - 3, baseSize / 6);
                // Highlight edge
                px.setColor(new Color(0.68f, 0.65f, 0.60f, 0.3f));
                px.drawCircle(half - 1, half - 1, baseSize / 2 - 2);
                break;
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildEarthTexture() {
        int size = 80;
        int cx = size / 2, cy = size / 2;
        int radius = 36;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Ocean base
        px.setColor(new Color(0.15f, 0.35f, 0.75f, 1f));
        px.fillCircle(cx, cy, radius);

        // Continents (green/brown patches)
        px.setColor(new Color(0.20f, 0.55f, 0.25f, 0.7f));
        px.fillCircle(cx - 10, cy - 5, 12);
        px.fillCircle(cx + 8, cy + 6, 10);
        px.setColor(new Color(0.25f, 0.50f, 0.20f, 0.6f));
        px.fillCircle(cx - 5, cy + 12, 8);
        px.fillCircle(cx + 14, cy - 8, 7);
        // Desert/brown region
        px.setColor(new Color(0.55f, 0.45f, 0.25f, 0.4f));
        px.fillCircle(cx + 5, cy - 2, 6);

        // Cloud wisps (white semi-transparent arcs)
        px.setColor(new Color(1f, 1f, 1f, 0.35f));
        px.fillCircle(cx - 12, cy - 10, 9);
        px.fillCircle(cx + 6, cy - 14, 7);
        px.setColor(new Color(1f, 1f, 1f, 0.25f));
        px.fillCircle(cx + 15, cy + 3, 8);
        px.fillCircle(cx - 4, cy + 14, 6);
        px.fillCircle(cx + 10, cy + 12, 5);

        // Atmosphere haze at edges
        px.setColor(new Color(0.5f, 0.7f, 1f, 0.2f));
        px.drawCircle(cx, cy, radius);
        px.drawCircle(cx, cy, radius + 1);

        // Clip to circle
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                if (dx * dx + dy * dy > (radius + 2) * (radius + 2)) {
                    px.setColor(0, 0, 0, 0);
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }
}
