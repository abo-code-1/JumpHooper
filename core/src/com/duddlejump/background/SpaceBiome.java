package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class SpaceBiome implements SkyBiome {

    private static final Color TOP = new Color(0.01f, 0.01f, 0.06f, 1f);
    private static final Color BOTTOM = new Color(0.04f, 0.06f, 0.18f, 1f);

    // --- Stars ---
    private static final int STAR_COUNT = 150;
    private static final int COLOR_CLASSES = 5;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final int[] starClass = new int[STAR_COUNT]; // 0-4 color temperature

    // Per-class colors: blue-white hot, white, yellow, orange, red-cool
    private static final float[][] CLASS_RGB = {
        {0.70f, 0.80f, 1.00f},  // blue-white hot
        {1.00f, 1.00f, 1.00f},  // white
        {1.00f, 0.96f, 0.80f},  // yellow
        {1.00f, 0.75f, 0.45f},  // orange
        {1.00f, 0.50f, 0.40f},  // red-cool
    };

    // --- Shooting stars ---
    private static final int SHOOTING_STARS = 5;
    private final float[] shootX = new float[SHOOTING_STARS];
    private final float[] shootY = new float[SHOOTING_STARS];
    private final float[] shootPhase = new float[SHOOTING_STARS];
    private final float[] shootAngle = new float[SHOOTING_STARS];

    // --- Planets ---
    private static final int PLANET_COUNT = 2;
    private final float[] planetX = new float[PLANET_COUNT];
    private final float[] planetY = new float[PLANET_COUNT];
    private final float[] planetRadius = new float[PLANET_COUNT];

    // --- Space dust ---
    private static final int DUST_COUNT = 60;
    private final float[] dustX = new float[DUST_COUNT];
    private final float[] dustY = new float[DUST_COUNT];
    private final float[] dustSize = new float[DUST_COUNT];
    private final float[] dustPhase = new float[DUST_COUNT];
    private final float[] dustR = new float[DUST_COUNT];
    private final float[] dustG = new float[DUST_COUNT];
    private final float[] dustB = new float[DUST_COUNT];

    // --- Textures (lazy-init) ---
    private Texture dotTex;
    private Texture glowTex;
    private Texture shootingStarTex;
    private Texture milkyWayTex;
    private Texture planetBlueTex;
    private Texture planetRedTex;
    private Texture dustTex;

    public SpaceBiome() {
        MathUtils.random.setSeed(42L);

        // Stars
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, 1f);
            starY[i] = MathUtils.random(0f, 1f);
            starSize[i] = MathUtils.random(1f, 4f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            starClass[i] = MathUtils.random(0, COLOR_CLASSES - 1);
        }

        // Shooting stars
        for (int i = 0; i < SHOOTING_STARS; i++) {
            shootX[i] = MathUtils.random(0.05f, 0.85f);
            shootY[i] = MathUtils.random(0.4f, 0.95f);
            shootPhase[i] = MathUtils.random(0f, 25f);
            shootAngle[i] = MathUtils.random(-0.4f, 0.1f); // mostly downward-right
        }

        // Planets
        planetX[0] = MathUtils.random(0.15f, 0.35f);
        planetY[0] = MathUtils.random(0.6f, 0.85f);
        planetRadius[0] = MathUtils.random(10f, 16f);
        planetX[1] = MathUtils.random(0.65f, 0.85f);
        planetY[1] = MathUtils.random(0.3f, 0.55f);
        planetRadius[1] = MathUtils.random(8f, 14f);

        // Space dust
        for (int i = 0; i < DUST_COUNT; i++) {
            dustX[i] = MathUtils.random(0f, 1f);
            dustY[i] = MathUtils.random(0f, 1f);
            dustSize[i] = MathUtils.random(2f, 6f);
            dustPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            float hue = MathUtils.random(0f, 1f);
            if (hue < 0.5f) {
                // deep blue
                dustR[i] = 0.15f; dustG[i] = 0.15f; dustB[i] = 0.45f;
            } else if (hue < 0.8f) {
                // purple
                dustR[i] = 0.30f; dustG[i] = 0.12f; dustB[i] = 0.50f;
            } else {
                // indigo
                dustR[i] = 0.18f; dustG[i] = 0.10f; dustB[i] = 0.38f;
            }
        }
    }

    @Override
    public String getName() {
        return "space";
    }

    @Override
    public float getStartAltitude() {
        return 10000f;
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
        if (dotTex == null) {
            initTextures();
        }

        float baseY = cameraY - viewH * 0.5f;
        float parallax = cameraY * 0.08f;
        float time = cameraY * 0.0004f;

        // --- Milky Way band (deep background, slow parallax) ---
        renderMilkyWay(batch, baseY, parallax, viewW, viewH, alpha);

        // --- Distant planets ---
        renderPlanets(batch, baseY, parallax, viewW, viewH, alpha, time);

        // --- Space dust ---
        renderDust(batch, baseY, parallax, viewW, viewH, alpha, time);

        // --- Starfield ---
        renderStars(batch, baseY, parallax, viewW, viewH, alpha, time);

        // --- Shooting stars ---
        renderShootingStars(batch, baseY, viewW, viewH, alpha, time);

        batch.setColor(Color.WHITE);
    }

    // ───────── Rendering helpers ─────────

    private void renderMilkyWay(SpriteBatch batch, float baseY, float parallax,
                                float viewW, float viewH, float alpha) {
        float mwParallax = parallax * 0.05f;
        float mx = -viewW * 0.15f;
        float my = baseY + ((0.35f * viewH - mwParallax) % viewH + viewH) % viewH;
        float scaleX = viewW * 1.6f / milkyWayTex.getWidth();
        float scaleY = viewH * 0.55f / milkyWayTex.getHeight();
        float drawW = milkyWayTex.getWidth() * scaleX;
        float drawH = milkyWayTex.getHeight() * scaleY;

        batch.setColor(1f, 1f, 1f, alpha * 0.12f);
        // Draw rotated using the overload with origin + rotation
        batch.draw(milkyWayTex,
            mx, my,                      // position
            drawW * 0.5f, drawH * 0.5f,  // origin
            drawW, drawH,                // size
            1f, 1f,                      // scale
            25f,                         // rotation (degrees)
            0, 0,                        // src xy
            milkyWayTex.getWidth(), milkyWayTex.getHeight(), // src size
            false, false);               // flip
    }

    private void renderPlanets(SpriteBatch batch, float baseY, float parallax,
                               float viewW, float viewH, float alpha, float time) {
        float pParallax = parallax * 0.12f;

        // Planet 0 - bluish
        float px0 = planetX[0] * viewW;
        float py0 = baseY + ((planetY[0] * viewH - pParallax) % viewH + viewH) % viewH;
        float r0 = planetRadius[0];
        batch.setColor(0.35f, 0.55f, 0.85f, alpha * 0.55f);
        batch.draw(planetBlueTex, px0 - r0, py0 - r0, r0 * 2f, r0 * 2f);

        // Planet 1 - reddish-brown
        float px1 = planetX[1] * viewW;
        float py1 = baseY + ((planetY[1] * viewH - pParallax) % viewH + viewH) % viewH;
        float r1 = planetRadius[1];
        batch.setColor(0.70f, 0.35f, 0.25f, alpha * 0.50f);
        batch.draw(planetRedTex, px1 - r1, py1 - r1, r1 * 2f, r1 * 2f);
    }

    private void renderDust(SpriteBatch batch, float baseY, float parallax,
                            float viewW, float viewH, float alpha, float time) {
        float dustParallax = parallax * 0.4f;
        for (int i = 0; i < DUST_COUNT; i++) {
            float dx = dustX[i] * viewW;
            float dy = baseY + ((dustY[i] * viewH - dustParallax) % viewH + viewH) % viewH;
            float s = dustSize[i];
            float drift = MathUtils.sin(time * 1.5f + dustPhase[i]) * 3f;
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * 2f + dustPhase[i]);
            batch.setColor(dustR[i], dustG[i], dustB[i], alpha * 0.10f * pulse);
            batch.draw(dustTex, dx + drift, dy, s, s);
        }
    }

    private void renderStars(SpriteBatch batch, float baseY, float parallax,
                             float viewW, float viewH, float alpha, float time) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float px = starX[i] * viewW;
            float py = baseY + ((starY[i] * viewH - parallax) % viewH + viewH) % viewH;
            float s = starSize[i];
            float twinkle = 0.3f + 0.7f * (0.5f + 0.5f * MathUtils.sin(time * 8f + starPhase[i]));

            int cls = starClass[i];
            float cr = CLASS_RGB[cls][0];
            float cg = CLASS_RGB[cls][1];
            float cb = CLASS_RGB[cls][2];

            batch.setColor(cr, cg, cb, alpha * twinkle);
            batch.draw(dotTex, px, py, s, s);

            // Bigger stars (>2.8) get diffraction spikes and glow halo
            if (s > 2.8f) {
                float glowA = alpha * twinkle * 0.12f;
                // Soft glow halo
                batch.setColor(cr, cg, cb, glowA);
                float gs = s * 4f;
                batch.draw(glowTex, px - gs * 0.5f + s * 0.5f, py - gs * 0.5f + s * 0.5f, gs, gs);

                // Cross-shaped diffraction spikes
                float spikeA = alpha * twinkle * 0.25f;
                batch.setColor(cr, cg, cb, spikeA);
                float spikeLen = s * 3f;
                float spikeThick = s * 0.35f;
                float cx = px + s * 0.5f;
                float cy = py + s * 0.5f;
                // Horizontal spike
                batch.draw(dotTex, cx - spikeLen * 0.5f, cy - spikeThick * 0.5f, spikeLen, spikeThick);
                // Vertical spike
                batch.draw(dotTex, cx - spikeThick * 0.5f, cy - spikeLen * 0.5f, spikeThick, spikeLen);
            }
        }
    }

    private void renderShootingStars(SpriteBatch batch, float baseY,
                                     float viewW, float viewH, float alpha, float time) {
        for (int i = 0; i < SHOOTING_STARS; i++) {
            float cycle = (time * 3.5f + shootPhase[i]) % 14f;
            if (cycle < 2f) {
                float t = cycle / 2f;
                float speed = 180f;
                float sx = shootX[i] * viewW + t * speed;
                float sy = baseY + shootY[i] * viewH + t * speed * shootAngle[i];

                float fadeIn = Math.min(1f, t * 4f);
                float fadeOut = Math.max(0f, 1f - (t - 0.4f) * 2.5f);
                float sAlpha = alpha * fadeIn * fadeOut * 0.9f;

                // Trail angle matching direction
                float angleDeg = MathUtils.atan2(shootAngle[i] * speed, speed) * MathUtils.radiansToDegrees;
                float drawW = shootingStarTex.getWidth();
                float drawH = shootingStarTex.getHeight();

                batch.setColor(1f, 1f, 1f, sAlpha);
                batch.draw(shootingStarTex,
                    sx, sy - drawH * 0.5f,
                    0f, drawH * 0.5f,           // origin at left-center
                    drawW, drawH,
                    1f, 1f,
                    angleDeg,
                    0, 0,
                    (int) drawW, (int) drawH,
                    false, false);
            }
        }
    }

    // ───────── Texture building ─────────

    private void initTextures() {
        // Small dot (4x4)
        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fillCircle(2, 2, 2);
        dotTex = new Texture(pix);
        pix.dispose();

        // Soft glow (32x32 radial falloff)
        glowTex = buildGlowTexture();

        // Shooting star trail (60x4)
        shootingStarTex = buildShootingStarTexture();

        // Milky Way band (200x80)
        milkyWayTex = buildMilkyWayTexture();

        // Planet textures
        planetBlueTex = buildPlanetTexture(0.25f, 0.45f, 0.80f, 0.50f, 0.70f, 1.0f);
        planetRedTex = buildPlanetTexture(0.55f, 0.25f, 0.15f, 0.80f, 0.45f, 0.30f);

        // Dust particle (8x8 soft circle)
        dustTex = buildDustTexture();
    }

    private static Texture buildGlowTexture() {
        int size = 32;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = size / 2;
        int cy = size / 2;
        float maxR = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                if (dist < maxR) {
                    float falloff = 1f - (dist / maxR);
                    falloff = falloff * falloff; // quadratic falloff
                    px.setColor(new Color(1f, 1f, 1f, falloff * 0.6f));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildShootingStarTexture() {
        int w = 60, h = 4;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int x = 0; x < w; x++) {
            float t = (float) x / w;
            float fade = t * t;

            // Color gradient: orange -> yellow -> white at head
            float r, g, b;
            if (t < 0.3f) {
                // Tail: orange fading in
                r = 1f; g = 0.55f; b = 0.20f;
            } else if (t < 0.7f) {
                // Mid: yellow
                float blend = (t - 0.3f) / 0.4f;
                r = 1f; g = 0.55f + blend * 0.45f; b = 0.20f + blend * 0.60f;
            } else {
                // Head: white-hot
                float blend = (t - 0.7f) / 0.3f;
                r = 1f; g = 1f; b = 0.80f + blend * 0.20f;
            }

            px.setColor(new Color(r, g, b, fade));
            // Thicker center, thinner edges
            px.fillRectangle(x, 1, 1, 2);
            if (t > 0.4f) {
                px.setColor(new Color(r, g, b, fade * 0.4f));
                px.drawPixel(x, 0);
                px.drawPixel(x, 3);
            }
        }

        // Bright head glow
        px.setColor(Color.WHITE);
        px.fillCircle(w - 3, 2, 2);
        px.setColor(new Color(1f, 0.95f, 0.85f, 0.6f));
        px.fillCircle(w - 3, 2, 3);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildMilkyWayTexture() {
        int w = 200, h = 80;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Dense cluster of overlapping soft circles in white/pale blue
        // Use a seeded sequence for reproducibility
        long seed = 777L;
        MathUtils.random.setSeed(seed);

        for (int i = 0; i < 80; i++) {
            int cx = MathUtils.random(10, w - 10);
            int cy = MathUtils.random(10, h - 10);
            int r = MathUtils.random(8, 25);

            float hue = MathUtils.random(0f, 1f);
            float cr, cg, cb;
            if (hue < 0.6f) {
                // White
                cr = 1f; cg = 1f; cb = 1f;
            } else {
                // Pale blue
                cr = 0.75f; cg = 0.85f; cb = 1f;
            }

            px.setColor(new Color(cr, cg, cb, 0.04f + MathUtils.random(0f, 0.06f)));
            px.fillCircle(cx, cy, r);
        }

        // Add a brighter central core band
        for (int i = 0; i < 40; i++) {
            int cx = MathUtils.random(30, w - 30);
            int cy = h / 2 + MathUtils.random(-12, 12);
            int r = MathUtils.random(5, 15);
            px.setColor(new Color(0.90f, 0.92f, 1f, 0.06f));
            px.fillCircle(cx, cy, r);
        }

        // Tiny scattered point-stars within the band
        for (int i = 0; i < 120; i++) {
            int sx = MathUtils.random(0, w - 1);
            int sy = MathUtils.random(0, h - 1);
            float distFromCenter = Math.abs(sy - h / 2f) / (h / 2f);
            if (MathUtils.random(0f, 1f) > distFromCenter) {
                px.setColor(new Color(1f, 1f, 1f, 0.10f + MathUtils.random(0f, 0.15f)));
                px.drawPixel(sx, sy);
            }
        }

        // Reset seed so other init code is not affected
        MathUtils.random.setSeed(42L + 999);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildPlanetTexture(float bodyR, float bodyG, float bodyB,
                                               float edgeR, float edgeG, float edgeB) {
        int size = 32;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = size / 2;
        int cy = size / 2;
        float radius = size / 2f - 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                if (dist <= radius) {
                    // Lit-edge effect: brighter on right side (simulating light from right)
                    float nx = (x - cx) / radius; // -1 to 1
                    float litFactor = 0.3f + 0.7f * Math.max(0f, nx * 0.6f + 0.4f);

                    // Edge glow (limb brightening at the lit edge)
                    float edgeness = dist / radius;
                    float edgeGlow = 0f;
                    if (edgeness > 0.75f && nx > 0f) {
                        edgeGlow = (edgeness - 0.75f) / 0.25f * nx;
                    }

                    float r = bodyR * litFactor + edgeR * edgeGlow * 0.5f;
                    float g = bodyG * litFactor + edgeG * edgeGlow * 0.5f;
                    float b = bodyB * litFactor + edgeB * edgeGlow * 0.5f;

                    // Soft edge anti-alias
                    float aa = Math.min(1f, (radius - dist) * 2f);

                    px.setColor(new Color(
                        Math.min(1f, r),
                        Math.min(1f, g),
                        Math.min(1f, b),
                        aa));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildDustTexture() {
        int size = 8;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = size / 2;
        int cy = size / 2;
        float maxR = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                if (dist < maxR) {
                    float falloff = 1f - (dist / maxR);
                    px.setColor(new Color(1f, 1f, 1f, falloff * falloff * 0.5f));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }
}
