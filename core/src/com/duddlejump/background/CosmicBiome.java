package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class CosmicBiome implements SkyBiome {

    private static final Color TOP    = new Color(0.08f, 0.01f, 0.18f, 1f);
    private static final Color BOTTOM = new Color(0.28f, 0.05f, 0.35f, 1f);

    /* ── stars ─────────────────────────────────────────────── */
    private static final int STAR_COUNT = 200;
    private final float[] starX     = new float[STAR_COUNT];
    private final float[] starY     = new float[STAR_COUNT];
    private final float[] starSize  = new float[STAR_COUNT];
    private final float[] starR     = new float[STAR_COUNT];
    private final float[] starG     = new float[STAR_COUNT];
    private final float[] starB     = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];

    /* ── nebulae ───────────────────────────────────────────── */
    private static final int NEBULA_COUNT = 6;
    private final float[] nebX     = new float[NEBULA_COUNT];
    private final float[] nebY     = new float[NEBULA_COUNT];
    private final float[] nebScale = new float[NEBULA_COUNT];
    private final int[]   nebType  = new int[NEBULA_COUNT];   // color palette index

    /* ── pulsars ───────────────────────────────────────────── */
    private static final int PULSAR_COUNT = 3;
    private final float[] pulsarX     = new float[PULSAR_COUNT];
    private final float[] pulsarY     = new float[PULSAR_COUNT];
    private final float[] pulsarPhase = new float[PULSAR_COUNT];
    private final float[] pulsarFreq  = new float[PULSAR_COUNT];

    /* ── constellation lines ───────────────────────────────── */
    private static final int CONST_LINE_COUNT = 8;
    private final int[] constA = new int[CONST_LINE_COUNT]; // star index A
    private final int[] constB = new int[CONST_LINE_COUNT]; // star index B

    /* ── textures (lazy-init) ──────────────────────────────── */
    private Texture dotTex;
    private Texture haloTex;
    private Texture lineTex;       // 1-px wide elongated dot for constellation lines
    private Texture[] nebulaTex;   // one per nebula (different palettes)
    private Texture galaxyTex;
    private Texture dustTex;
    private Texture pulsarBeamTex;

    /* ================================================================ */

    public CosmicBiome() {
        MathUtils.random.setSeed(91L);

        // --- stars with 6 colour classes ---
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = MathUtils.random(0f, 1f);
            starY[i]     = MathUtils.random(0f, 1f);
            starSize[i]  = MathUtils.random(1.0f, 5.0f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);

            int cls = i % 6;
            switch (cls) {
                case 0: starR[i] = 1.00f; starG[i] = 0.55f; starB[i] = 0.65f; break; // rose-pink
                case 1: starR[i] = 0.50f; starG[i] = 0.85f; starB[i] = 1.00f; break; // cyan-blue
                case 2: starR[i] = 1.00f; starG[i] = 0.90f; starB[i] = 0.55f; break; // golden-yellow
                case 3: starR[i] = 0.75f; starG[i] = 0.45f; starB[i] = 1.00f; break; // purple-violet
                case 4: starR[i] = 0.85f; starG[i] = 0.90f; starB[i] = 1.00f; break; // blue-white
                case 5: starR[i] = 1.00f; starG[i] = 0.70f; starB[i] = 0.45f; break; // warm-orange
            }
        }

        // --- nebulae ---
        for (int i = 0; i < NEBULA_COUNT; i++) {
            nebX[i]     = MathUtils.random(0.05f, 0.95f);
            nebY[i]     = MathUtils.random(0.08f, 0.92f);
            nebScale[i] = MathUtils.random(0.9f, 2.2f);
            nebType[i]  = i; // each gets its own palette
        }

        // --- pulsars ---
        for (int i = 0; i < PULSAR_COUNT; i++) {
            pulsarX[i]     = MathUtils.random(0.1f, 0.9f);
            pulsarY[i]     = MathUtils.random(0.15f, 0.85f);
            pulsarPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            pulsarFreq[i]  = MathUtils.random(3f, 7f);
        }

        // --- constellation lines (connect nearby star pairs) ---
        // Pick deterministic pairs
        for (int i = 0; i < CONST_LINE_COUNT; i++) {
            constA[i] = (i * 23 + 7) % STAR_COUNT;
            constB[i] = (i * 31 + 19) % STAR_COUNT;
        }
    }

    /* ================================================================ */

    @Override public String getName()        { return "cosmic"; }
    @Override public float  getStartAltitude() { return 55000f; }
    @Override public Color  getTopColor()    { return TOP; }
    @Override public Color  getBottomColor() { return BOTTOM; }

    /* ================================================================ */

    @Override
    public void renderDecorations(SpriteBatch batch, float cameraX, float cameraY,
                                  float viewW, float viewH, float alpha) {
        if (dotTex == null) initTextures();

        float baseY    = cameraY - viewH * 0.5f;
        float parallax = cameraY * 0.06f;
        float time     = cameraY * 0.0003f;

        drawDust(batch, baseY, parallax, viewW, viewH, alpha, time);
        drawNebulae(batch, baseY, parallax, viewW, viewH, alpha, time);
        drawGalaxy(batch, baseY, parallax, viewW, viewH, alpha);
        drawConstellations(batch, baseY, parallax, viewW, viewH, alpha, time);
        drawStars(batch, baseY, parallax, viewW, viewH, alpha, time);
        drawPulsars(batch, baseY, parallax, viewW, viewH, alpha, time);

        batch.setColor(Color.WHITE);
    }

    /* ── layer renderers ─────────────────────────────────────────────── */

    private void drawDust(SpriteBatch batch, float baseY, float parallax,
                          float viewW, float viewH, float alpha, float time) {
        // Two large dust clouds scrolling slowly
        for (int i = 0; i < 3; i++) {
            float dx = (0.15f + i * 0.35f) * viewW;
            float rawY = (0.3f + i * 0.25f) * viewH - parallax * 0.08f;
            float dy = baseY + ((rawY % viewH) + viewH) % viewH;
            float breathe = 0.7f + 0.3f * MathUtils.sin(time * 0.8f + i * 1.5f);
            batch.setColor(0.15f, 0.05f, 0.25f, alpha * 0.06f * breathe);
            float s = 2.5f + i * 0.4f;
            batch.draw(dustTex, dx - dustTex.getWidth() * s * 0.5f,
                       dy - dustTex.getHeight() * s * 0.5f,
                       dustTex.getWidth() * s, dustTex.getHeight() * s);
        }
    }

    private void drawNebulae(SpriteBatch batch, float baseY, float parallax,
                             float viewW, float viewH, float alpha, float time) {
        // Nebula colour palettes: [r, g, b]
        float[][] palettes = {
            {0.55f, 0.15f, 0.75f}, // purple
            {0.80f, 0.20f, 0.55f}, // magenta
            {0.15f, 0.55f, 0.65f}, // teal
            {0.90f, 0.40f, 0.55f}, // pink
            {0.25f, 0.35f, 0.80f}, // blue
            {0.85f, 0.70f, 0.25f}, // golden
        };

        for (int i = 0; i < NEBULA_COUNT; i++) {
            float nx = nebX[i] * viewW;
            float rawY = nebY[i] * viewH - parallax * 0.18f;
            float ny = baseY + ((rawY % viewH) + viewH) % viewH;
            float s  = nebScale[i];
            float[] c = palettes[nebType[i] % palettes.length];

            float pulse = 0.55f + 0.45f * MathUtils.sin(time * 1.2f + i * 1.8f);
            batch.setColor(c[0], c[1], c[2], alpha * 0.10f * pulse);
            batch.draw(nebulaTex[i], nx - 75f * s, ny - 50f * s,
                       150f * s, 100f * s);
        }
    }

    private void drawGalaxy(SpriteBatch batch, float baseY, float parallax,
                            float viewW, float viewH, float alpha) {
        float gx = viewW * 0.30f;
        float rawY = 0.60f * viewH - parallax * 0.12f;
        float gy = baseY + ((rawY % viewH) + viewH) % viewH;
        batch.setColor(0.82f, 0.72f, 1f, alpha * 0.18f);
        float s = 0.9f;
        batch.draw(galaxyTex, gx - galaxyTex.getWidth() * s * 0.5f,
                   gy - galaxyTex.getHeight() * s * 0.5f,
                   galaxyTex.getWidth() * s, galaxyTex.getHeight() * s);
    }

    private void drawConstellations(SpriteBatch batch, float baseY, float parallax,
                                    float viewW, float viewH, float alpha, float time) {
        float flicker = 0.5f + 0.5f * MathUtils.sin(time * 2f);
        float lineAlpha = alpha * 0.035f * flicker;
        batch.setColor(0.6f, 0.7f, 1f, lineAlpha);

        for (int i = 0; i < CONST_LINE_COUNT; i++) {
            float ax = starX[constA[i]] * viewW;
            float ay = baseY + ((starY[constA[i]] * viewH - parallax) % viewH + viewH) % viewH;
            float bx = starX[constB[i]] * viewW;
            float by = baseY + ((starY[constB[i]] * viewH - parallax) % viewH + viewH) % viewH;

            // Only draw if both endpoints are reasonably close vertically
            if (Math.abs(ay - by) > viewH * 0.5f) continue;

            float dx = bx - ax;
            float dy = by - ay;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 5f || len > viewW * 0.35f) continue;

            float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            batch.draw(lineTex, ax, ay - 0.5f,
                       0, 0.5f,
                       len, 1f,
                       1f, 1f,
                       angle,
                       0, 0, lineTex.getWidth(), lineTex.getHeight(),
                       false, false);
        }
    }

    private void drawStars(SpriteBatch batch, float baseY, float parallax,
                           float viewW, float viewH, float alpha, float time) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float px = starX[i] * viewW;
            float py = baseY + ((starY[i] * viewH - parallax) % viewH + viewH) % viewH;
            float s  = starSize[i];
            float twinkle = 0.25f + 0.75f * (0.5f + 0.5f * MathUtils.sin(time * 5.5f + starPhase[i]));

            // Main dot
            batch.setColor(starR[i], starG[i], starB[i], alpha * twinkle);
            batch.draw(dotTex, px - s * 0.5f, py - s * 0.5f, s, s);

            // Big stars: diffraction spikes + halo
            if (s > 3.2f) {
                float spikeLen = s * 2.5f;
                float spikeW   = s * 0.35f;
                float spikeA   = alpha * twinkle * 0.30f;
                batch.setColor(starR[i], starG[i], starB[i], spikeA);
                // horizontal spike
                batch.draw(dotTex, px - spikeLen * 0.5f, py - spikeW * 0.5f, spikeLen, spikeW);
                // vertical spike
                batch.draw(dotTex, px - spikeW * 0.5f, py - spikeLen * 0.5f, spikeW, spikeLen);
                // diagonal spikes (shorter)
                float diagLen = spikeLen * 0.55f;
                float diagW   = spikeW * 0.7f;
                float dAlpha  = alpha * twinkle * 0.15f;
                batch.setColor(starR[i], starG[i], starB[i], dAlpha);
                // 45-degree spike
                batch.draw(lineTex, px - diagW * 0.5f, py - diagW * 0.5f,
                           diagW * 0.5f, diagW * 0.5f,
                           diagLen, diagW,
                           1f, 1f, 45f,
                           0, 0, lineTex.getWidth(), lineTex.getHeight(),
                           false, false);
                // 135-degree spike
                batch.draw(lineTex, px - diagW * 0.5f, py - diagW * 0.5f,
                           diagW * 0.5f, diagW * 0.5f,
                           diagLen, diagW,
                           1f, 1f, 135f,
                           0, 0, lineTex.getWidth(), lineTex.getHeight(),
                           false, false);

                // Halo glow
                float haloSize = s * 4f;
                batch.setColor(starR[i], starG[i], starB[i], alpha * twinkle * 0.08f);
                batch.draw(haloTex, px - haloSize * 0.5f, py - haloSize * 0.5f, haloSize, haloSize);
            }
        }
    }

    private void drawPulsars(SpriteBatch batch, float baseY, float parallax,
                             float viewW, float viewH, float alpha, float time) {
        for (int i = 0; i < PULSAR_COUNT; i++) {
            float px = pulsarX[i] * viewW;
            float rawY = pulsarY[i] * viewH - parallax * 0.5f;
            float py = baseY + ((rawY % viewH) + viewH) % viewH;

            float phase    = time * pulsarFreq[i] + pulsarPhase[i];
            float rawPulse = MathUtils.sin(phase);
            // Sharp brightness spike: raise sin to high power when positive
            float spike = rawPulse > 0 ? (float) Math.pow(rawPulse, 6.0) : 0f;
            float base  = 0.3f + 0.2f * (0.5f + 0.5f * rawPulse);
            float brightness = base + spike * 0.7f;

            // Core point
            float coreSize = 3f + spike * 4f;
            batch.setColor(0.9f, 0.95f, 1f, alpha * brightness);
            batch.draw(dotTex, px - coreSize * 0.5f, py - coreSize * 0.5f, coreSize, coreSize);

            // Halo
            float haloSize = 8f + spike * 16f;
            batch.setColor(0.7f, 0.8f, 1f, alpha * brightness * 0.15f);
            batch.draw(haloTex, px - haloSize * 0.5f, py - haloSize * 0.5f, haloSize, haloSize);

            // Light beams (two opposing jets)
            if (spike > 0.05f) {
                float beamLen = 30f + spike * 60f;
                float beamW   = 2f + spike * 3f;
                float beamA   = alpha * spike * 0.25f;
                float angle   = 90f + i * 40f; // each pulsar at different angle
                batch.setColor(0.7f, 0.85f, 1f, beamA);
                // Jet 1
                batch.draw(pulsarBeamTex, px, py - beamW * 0.5f,
                           0, beamW * 0.5f,
                           beamLen, beamW,
                           1f, 1f, angle,
                           0, 0, pulsarBeamTex.getWidth(), pulsarBeamTex.getHeight(),
                           false, false);
                // Jet 2 (opposite direction)
                batch.draw(pulsarBeamTex, px, py - beamW * 0.5f,
                           0, beamW * 0.5f,
                           beamLen, beamW,
                           1f, 1f, angle + 180f,
                           0, 0, pulsarBeamTex.getWidth(), pulsarBeamTex.getHeight(),
                           false, false);
            }
        }
    }

    /* ── texture builders ────────────────────────────────────────────── */

    private void initTextures() {
        dotTex        = buildDot();
        haloTex       = buildHalo();
        lineTex       = buildLine();
        nebulaTex     = buildAllNebulae();
        galaxyTex     = buildGalaxyTex();
        dustTex       = buildDustTex();
        pulsarBeamTex = buildBeamTex();
    }

    private static Texture buildDot() {
        Pixmap p = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        // Soft circle with smooth falloff
        int cx = 4, cy = 4;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                float d = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                float a = Math.max(0f, 1f - d / 4f);
                a = a * a; // quadratic falloff
                p.setColor(new Color(1f, 1f, 1f, a));
                p.drawPixel(x, y);
            }
        }
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private static Texture buildHalo() {
        int sz = 32;
        Pixmap p = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = sz / 2, cy = sz / 2;
        float rad = sz / 2f;
        for (int y = 0; y < sz; y++) {
            for (int x = 0; x < sz; x++) {
                float d = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                float a = Math.max(0f, 1f - d / rad);
                a = a * a * a; // cubic falloff for soft halo
                p.setColor(new Color(1f, 1f, 1f, a));
                p.drawPixel(x, y);
            }
        }
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private static Texture buildLine() {
        Pixmap p = new Pixmap(16, 2, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    /**
     * Build 6 nebula textures each 150x100 with different colour palettes.
     * Each is composed of many overlapping soft circles to create a cloudy gas look.
     */
    private static Texture[] buildAllNebulae() {
        float[][][] palettes = {
            // Each palette has 3 tints: [primary, secondary, accent]
            {{0.50f, 0.15f, 0.70f}, {0.65f, 0.25f, 0.85f}, {0.40f, 0.10f, 0.55f}}, // purple
            {{0.75f, 0.18f, 0.50f}, {0.90f, 0.30f, 0.60f}, {0.60f, 0.12f, 0.40f}}, // magenta
            {{0.12f, 0.50f, 0.60f}, {0.20f, 0.65f, 0.70f}, {0.08f, 0.40f, 0.50f}}, // teal
            {{0.85f, 0.35f, 0.50f}, {0.95f, 0.50f, 0.60f}, {0.70f, 0.25f, 0.40f}}, // pink
            {{0.20f, 0.30f, 0.75f}, {0.30f, 0.45f, 0.90f}, {0.15f, 0.20f, 0.60f}}, // blue
            {{0.80f, 0.65f, 0.20f}, {0.90f, 0.75f, 0.35f}, {0.65f, 0.50f, 0.15f}}, // golden
        };

        Texture[] textures = new Texture[6];
        for (int n = 0; n < 6; n++) {
            int w = 150, h = 100;
            Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            px.setColor(0, 0, 0, 0);
            px.fill();

            float[][] pal = palettes[n];
            MathUtils.random.setSeed(200L + n * 37L);

            // Layer 1: large diffuse background blobs
            for (int i = 0; i < 20; i++) {
                int cx = MathUtils.random(10, w - 10);
                int cy = MathUtils.random(10, h - 10);
                int r  = MathUtils.random(18, 40);
                float[] c = pal[i % 3];
                drawSoftCircle(px, cx, cy, r, c[0], c[1], c[2], 0.04f);
            }

            // Layer 2: medium detail clouds
            for (int i = 0; i < 30; i++) {
                int cx = MathUtils.random(5, w - 5);
                int cy = MathUtils.random(5, h - 5);
                int r  = MathUtils.random(8, 22);
                float[] c = pal[(i + 1) % 3];
                drawSoftCircle(px, cx, cy, r, c[0], c[1], c[2], 0.05f);
            }

            // Layer 3: small bright knots
            for (int i = 0; i < 15; i++) {
                int cx = w / 4 + MathUtils.random(0, w / 2);
                int cy = h / 4 + MathUtils.random(0, h / 2);
                int r  = MathUtils.random(4, 12);
                float[] c = pal[(i + 2) % 3];
                drawSoftCircle(px, cx, cy, r, c[0], c[1], c[2], 0.07f);
            }

            textures[n] = new Texture(px);
            px.dispose();
        }

        // Restore seed consistency
        MathUtils.random.setSeed(91L);

        return textures;
    }

    /** Draw a soft (radially-fading) filled circle onto a Pixmap. */
    private static void drawSoftCircle(Pixmap px, int cx, int cy, int radius,
                                       float r, float g, float b, float maxAlpha) {
        int x0 = Math.max(0, cx - radius);
        int y0 = Math.max(0, cy - radius);
        int x1 = Math.min(px.getWidth()  - 1, cx + radius);
        int y1 = Math.min(px.getHeight() - 1, cy + radius);

        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                float d = (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                if (d > radius) continue;
                float t = 1f - d / radius;
                float a = t * t * maxAlpha;
                // Additive-style: read existing pixel, blend manually
                int existing = px.getPixel(x, y);
                float er = ((existing >>> 24) & 0xFF) / 255f;
                float eg = ((existing >>> 16) & 0xFF) / 255f;
                float eb = ((existing >>>  8) & 0xFF) / 255f;
                float ea = (existing & 0xFF) / 255f;

                float nr = Math.min(1f, er + r * a);
                float ng = Math.min(1f, eg + g * a);
                float nb = Math.min(1f, eb + b * a);
                float na = Math.min(1f, ea + a);
                px.setColor(new Color(nr, ng, nb, na));
                px.drawPixel(x, y);
            }
        }
    }

    /** Build a 120x120 spiral galaxy with bright core, spiral arms, and dust lanes. */
    private static Texture buildGalaxyTex() {
        int size = 120;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = size / 2, cy = size / 2;

        // --- Core glow (multiple layers) ---
        drawSoftCircle(px, cx, cy, 18, 1.0f, 0.95f, 0.85f, 0.25f);
        drawSoftCircle(px, cx, cy, 10, 1.0f, 0.98f, 0.92f, 0.35f);
        drawSoftCircle(px, cx, cy, 5,  1.0f, 1.0f,  1.0f,  0.50f);

        // --- Spiral arms (parametric logarithmic spiral) ---
        // Two main arms + two secondary arms
        float[][] armColors = {
            {0.80f, 0.70f, 1.00f},  // blue-violet
            {0.85f, 0.78f, 0.95f},  // lavender
            {0.70f, 0.65f, 0.95f},  // blue
            {0.90f, 0.80f, 0.85f},  // pinkish
        };

        for (int arm = 0; arm < 4; arm++) {
            float startAngle = arm * MathUtils.PI / 2f;
            boolean isMain = arm < 2;
            int points = isMain ? 400 : 250;
            float maxAlpha = isMain ? 0.12f : 0.06f;
            float[] ac = armColors[arm];

            for (int i = 0; i < points; i++) {
                float t = (float) i / points;
                float angle = startAngle + t * MathUtils.PI * 3.5f; // ~1.75 full turns
                float r = 6f + t * (size * 0.42f);
                // Slight elliptical compression for tilt
                float ex = cx + MathUtils.cos(angle) * r;
                float ey = cy + MathUtils.sin(angle) * r * 0.45f;
                // Rotate the whole thing 30 degrees for visual interest
                float rot = 30f * MathUtils.degreesToRadians;
                float rx = cx + (ex - cx) * MathUtils.cos(rot) - (ey - cy) * MathUtils.sin(rot);
                float ry = cy + (ex - cx) * MathUtils.sin(rot) + (ey - cy) * MathUtils.cos(rot);

                int ix = (int) rx, iy = (int) ry;
                if (ix < 0 || ix >= size || iy < 0 || iy >= size) continue;

                float fade = (1f - t) * maxAlpha;
                int dotR = (int) (3 * (1f - t * 0.7f)) + 1;
                drawSoftCircle(px, ix, iy, dotR, ac[0], ac[1], ac[2], fade);
            }
        }

        // --- Dust lanes (dark areas between arms) ---
        for (int lane = 0; lane < 2; lane++) {
            float startAngle = lane * MathUtils.PI + MathUtils.PI / 4f;
            for (int i = 0; i < 150; i++) {
                float t = (float) i / 150;
                float angle = startAngle + t * MathUtils.PI * 2.5f;
                float r = 10f + t * (size * 0.35f);
                float ex = cx + MathUtils.cos(angle) * r;
                float ey = cy + MathUtils.sin(angle) * r * 0.45f;
                float rot = 30f * MathUtils.degreesToRadians;
                float rx = cx + (ex - cx) * MathUtils.cos(rot) - (ey - cy) * MathUtils.sin(rot);
                float ry = cy + (ex - cx) * MathUtils.sin(rot) + (ey - cy) * MathUtils.cos(rot);

                int ix = (int) rx, iy = (int) ry;
                if (ix < 0 || ix >= size || iy < 0 || iy >= size) continue;

                float fade = (1f - t) * 0.03f;
                int dotR = (int) (2 * (1f - t * 0.5f)) + 1;
                // Dark dust: very low alpha dark purple
                drawSoftCircle(px, ix, iy, dotR, 0.05f, 0.02f, 0.08f, fade);
            }
        }

        // --- Scattered star points in arms ---
        MathUtils.random.setSeed(777L);
        for (int i = 0; i < 60; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float r = MathUtils.random(8f, size * 0.45f);
            float ex = cx + MathUtils.cos(angle) * r;
            float ey = cy + MathUtils.sin(angle) * r * 0.45f;
            float rot = 30f * MathUtils.degreesToRadians;
            float rx = cx + (ex - cx) * MathUtils.cos(rot) - (ey - cy) * MathUtils.sin(rot);
            float ry = cy + (ex - cx) * MathUtils.sin(rot) + (ey - cy) * MathUtils.cos(rot);

            int ix = (int) rx, iy = (int) ry;
            if (ix >= 0 && ix < size && iy >= 0 && iy < size) {
                float bright = MathUtils.random(0.08f, 0.20f);
                px.setColor(new Color(0.9f, 0.88f, 1f, bright));
                px.drawPixel(ix, iy);
                if (bright > 0.14f && ix + 1 < size && iy + 1 < size) {
                    px.drawPixel(ix + 1, iy);
                    px.drawPixel(ix, iy + 1);
                }
            }
        }
        MathUtils.random.setSeed(91L);

        Texture t = new Texture(px);
        px.dispose();
        return t;
    }

    /** Background cosmic dust texture (large, soft). */
    private static Texture buildDustTex() {
        int w = 180, h = 120;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        MathUtils.random.setSeed(333L);
        for (int i = 0; i < 40; i++) {
            int cx = MathUtils.random(0, w);
            int cy = MathUtils.random(0, h);
            int r  = MathUtils.random(20, 50);
            // Deep purple / blue dust
            float cr = MathUtils.random(0.08f, 0.20f);
            float cg = MathUtils.random(0.03f, 0.10f);
            float cb = MathUtils.random(0.15f, 0.35f);
            drawSoftCircle(px, cx, cy, r, cr, cg, cb, 0.04f);
        }
        MathUtils.random.setSeed(91L);

        Texture t = new Texture(px);
        px.dispose();
        return t;
    }

    /** Tapered beam texture for pulsar jets. */
    private static Texture buildBeamTex() {
        int w = 32, h = 6;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cy = h / 2;
        for (int x = 0; x < w; x++) {
            float xFade = 1f - (float) x / w; // fade toward tip
            xFade = xFade * xFade;
            for (int y = 0; y < h; y++) {
                float yDist = Math.abs(y - cy) / (float) cy;
                float yFade = Math.max(0f, 1f - yDist);
                float a = xFade * yFade;
                px.setColor(new Color(1f, 1f, 1f, a));
                px.drawPixel(x, y);
            }
        }

        Texture t = new Texture(px);
        px.dispose();
        return t;
    }
}
