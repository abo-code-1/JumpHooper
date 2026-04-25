package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class EarthBiome implements SkyBiome {

    private static final Color TOP = new Color(0.42f, 0.72f, 0.95f, 1f);
    private static final Color BOTTOM = new Color(0.68f, 0.90f, 0.65f, 1f);

    private static final int CLOUD_COUNT = 10;
    private static final int BIRD_COUNT = 8;
    private static final int LEAF_COUNT = 6;

    // Cloud data
    private final float[] cloudX = new float[CLOUD_COUNT];
    private final float[] cloudY = new float[CLOUD_COUNT];
    private final float[] cloudScale = new float[CLOUD_COUNT];
    private final int[] cloudType = new int[CLOUD_COUNT];   // 0=small, 1=medium, 2=large
    private final boolean[] cloudTinted = new boolean[CLOUD_COUNT];

    // Bird data
    private final float[] birdX = new float[BIRD_COUNT];
    private final float[] birdY = new float[BIRD_COUNT];
    private final float[] birdSpeed = new float[BIRD_COUNT];
    private final float[] birdPhase = new float[BIRD_COUNT];

    // Leaf / petal data
    private final float[] leafX = new float[LEAF_COUNT];
    private final float[] leafY = new float[LEAF_COUNT];
    private final float[] leafDriftX = new float[LEAF_COUNT];
    private final float[] leafDriftY = new float[LEAF_COUNT];
    private final float[] leafPhase = new float[LEAF_COUNT];

    // Mountain data (peak x-positions and heights as fractions)
    private static final int PEAK_COUNT = 7;
    private final float[] peakX = new float[PEAK_COUNT];
    private final float[] peakH = new float[PEAK_COUNT];

    // Textures (lazy-init)
    private Texture cloudSmallTex;
    private Texture cloudMedTex;
    private Texture cloudLargeTex;
    private Texture birdTex;
    private Texture mountainTex;
    private Texture sunTex;
    private Texture leafTex;

    public EarthBiome() {
        MathUtils.random.setSeed(17L);

        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudX[i] = MathUtils.random(-0.05f, 1.05f);
            cloudY[i] = MathUtils.random(0.35f, 0.92f);
            cloudScale[i] = MathUtils.random(0.7f, 1.3f);
            cloudType[i] = MathUtils.random(0, 2);
            cloudTinted[i] = MathUtils.random() < 0.3f;
        }

        for (int i = 0; i < BIRD_COUNT; i++) {
            birdX[i] = MathUtils.random(0f, 1f);
            birdY[i] = MathUtils.random(0.45f, 0.88f);
            birdSpeed[i] = MathUtils.random(0.015f, 0.045f);
            birdPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }

        for (int i = 0; i < LEAF_COUNT; i++) {
            leafX[i] = MathUtils.random(0f, 1f);
            leafY[i] = MathUtils.random(0.2f, 0.8f);
            leafDriftX[i] = MathUtils.random(0.008f, 0.02f);
            leafDriftY[i] = MathUtils.random(0.003f, 0.008f);
            leafPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }

        for (int i = 0; i < PEAK_COUNT; i++) {
            peakX[i] = (float) i / (PEAK_COUNT - 1);
            peakH[i] = MathUtils.random(0.08f, 0.22f);
        }
    }

    @Override
    public String getName() {
        return "earth";
    }

    @Override
    public float getStartAltitude() {
        return 0f;
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
        if (cloudSmallTex == null) {
            cloudSmallTex = buildCloudTexture(60, 28, false);
            cloudMedTex = buildCloudTexture(100, 45, false);
            cloudLargeTex = buildCloudTexture(140, 60, true);
            birdTex = buildBirdTexture();
            mountainTex = buildMountainTexture();
            sunTex = buildSunTexture();
            leafTex = buildLeafTexture();
        }

        float baseY = cameraY - viewH * 0.5f;
        float time = cameraY * 0.001f;

        // --- Sun glow (top-right corner) ---
        float sunSize = viewW * 0.55f;
        batch.setColor(1f, 0.97f, 0.82f, alpha * 0.22f);
        batch.draw(sunTex, viewW - sunSize * 0.55f, baseY + viewH - sunSize * 0.45f,
            sunSize, sunSize);

        // --- Mountain silhouettes (parallax, behind everything else) ---
        float mountainParallax = cameraY * 0.05f;
        float mtW = viewW + 40f;
        float mtH = viewH * 0.28f;
        float mtY = baseY - (mountainParallax % (mtH * 0.5f));
        batch.setColor(0.22f, 0.35f, 0.28f, alpha * 0.35f);
        batch.draw(mountainTex, -20f, mtY, mtW, mtH);

        // --- Clouds ---
        float cloudParallax = cameraY * 0.15f;
        Texture[] cloudTextures = { cloudSmallTex, cloudMedTex, cloudLargeTex };
        for (int i = 0; i < CLOUD_COUNT; i++) {
            float cx = cloudX[i] * viewW;
            float cy = baseY + ((cloudY[i] * viewH - cloudParallax * 0.4f) % viewH + viewH) % viewH;
            float s = cloudScale[i];
            Texture tex = cloudTextures[cloudType[i]];

            if (cloudTinted[i]) {
                // Sunset-tinted cloud: warm pink/orange
                float tintPulse = 0.85f + 0.15f * MathUtils.sin(time * 0.5f + i);
                batch.setColor(1f, 0.82f * tintPulse, 0.72f * tintPulse, alpha * 0.75f);
            } else {
                batch.setColor(1f, 1f, 1f, alpha * 0.72f);
            }
            batch.draw(tex, cx, cy, tex.getWidth() * s, tex.getHeight() * s);
        }

        // --- Birds with wing-flap animation ---
        for (int i = 0; i < BIRD_COUNT; i++) {
            float bx = ((birdX[i] + time * birdSpeed[i]) % 1.2f) * viewW - 20f;
            float by = baseY + birdY[i] * viewH + MathUtils.sin(time * 2.5f + birdPhase[i]) * 10f;

            // Wing flap: scale Y of the bird texture based on sin wave
            float wingFlap = MathUtils.sin(time * 8f + birdPhase[i]);
            float birdW = 20f;
            float birdH = 10f;
            float flapScaleY = 0.5f + 0.5f * Math.abs(wingFlap);

            batch.setColor(0.12f, 0.12f, 0.18f, alpha * 0.55f);
            batch.draw(birdTex, bx, by + birdH * (1f - flapScaleY) * 0.5f,
                birdW, birdH * flapScaleY);
        }

        // --- Floating leaves / petals ---
        for (int i = 0; i < LEAF_COUNT; i++) {
            float lx = ((leafX[i] + time * leafDriftX[i]) % 1.1f) * viewW;
            float ly = baseY + ((leafY[i] * viewH + MathUtils.sin(time * 1.5f + leafPhase[i]) * 15f
                - time * leafDriftY[i] * viewH) % viewH + viewH) % viewH;

            // Alternate between green leaves and pink petals
            if (i % 2 == 0) {
                batch.setColor(0.35f, 0.65f, 0.25f, alpha * 0.5f);
            } else {
                batch.setColor(0.95f, 0.65f, 0.72f, alpha * 0.45f);
            }

            float leafSize = 6f + 3f * MathUtils.sin(time * 0.7f + leafPhase[i]);
            batch.draw(leafTex, lx, ly, leafSize, leafSize);
        }

        batch.setColor(Color.WHITE);
    }

    // ---- Texture builders ----

    private static Texture buildCloudTexture(int w, int h, boolean detailed) {
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = w / 2;
        int cy = h / 2;

        // Shadow / underside (drawn first, offset down)
        px.setColor(new Color(0.78f, 0.82f, 0.88f, 0.45f));
        px.fillCircle(cx - w / 5, cy + h / 5, h / 3);
        px.fillCircle(cx, cy + h / 5, (int)(h * 0.35f));
        px.fillCircle(cx + w / 5, cy + h / 5, h / 4);

        // Main cloud body: multiple overlapping white circles
        px.setColor(new Color(1f, 1f, 1f, 0.88f));
        px.fillCircle(cx - w / 4, cy + 2, (int)(h * 0.38f));
        px.fillCircle(cx - w / 10, cy - h / 7, (int)(h * 0.42f));
        px.fillCircle(cx + w / 10, cy, (int)(h * 0.40f));
        px.fillCircle(cx + w / 4, cy + 2, (int)(h * 0.34f));

        // Fill the gaps in the middle
        px.setColor(new Color(1f, 1f, 1f, 0.92f));
        px.fillCircle(cx, cy + 1, (int)(h * 0.36f));
        px.fillCircle(cx - w / 7, cy + h / 8, (int)(h * 0.30f));
        px.fillCircle(cx + w / 7, cy + h / 8, (int)(h * 0.28f));

        // Top highlight (bright, smaller circles on top)
        px.setColor(new Color(1f, 1f, 1f, 0.50f));
        px.fillCircle(cx - w / 8, cy - h / 5, (int)(h * 0.26f));
        px.fillCircle(cx + w / 12, cy - h / 5, (int)(h * 0.22f));

        if (detailed) {
            // Extra fluffy bumps on top for large clouds
            px.setColor(new Color(1f, 1f, 1f, 0.80f));
            px.fillCircle(cx - w / 3, cy - 1, (int)(h * 0.28f));
            px.fillCircle(cx + w / 3, cy + 1, (int)(h * 0.25f));
            px.fillCircle(cx, cy - h / 4, (int)(h * 0.30f));

            // Bright rim highlight
            px.setColor(new Color(1f, 1f, 0.97f, 0.35f));
            px.fillCircle(cx, cy - h / 3, (int)(h * 0.18f));
        }

        // Soft edges: semi-transparent fringe circles
        px.setColor(new Color(1f, 1f, 1f, 0.18f));
        px.fillCircle(cx - w / 3 - 4, cy + 3, (int)(h * 0.22f));
        px.fillCircle(cx + w / 3 + 4, cy + 3, (int)(h * 0.20f));
        px.fillCircle(cx, cy + h / 3, (int)(h * 0.22f));

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildBirdTexture() {
        int w = 20, h = 10;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        Color body = new Color(0.15f, 0.15f, 0.22f, 1f);
        px.setColor(body);

        // Left wing
        px.drawLine(0, 6, 3, 2);
        px.drawLine(3, 2, 6, 1);
        px.drawLine(6, 1, 8, 3);
        px.drawLine(1, 6, 4, 3);
        px.drawLine(4, 3, 7, 2);

        // Body center
        px.fillCircle(10, 5, 2);
        px.drawLine(8, 4, 12, 4);
        px.drawLine(8, 5, 12, 5);

        // Right wing
        px.drawLine(12, 3, 14, 1);
        px.drawLine(14, 1, 17, 2);
        px.drawLine(17, 2, 19, 6);
        px.drawLine(13, 4, 16, 2);
        px.drawLine(16, 2, 18, 5);

        // Tail feather hint
        px.drawLine(9, 6, 7, 8);
        px.drawLine(10, 6, 8, 8);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildMountainTexture() {
        int w = 256, h = 80;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Build a mountain range silhouette using a series of peaks
        // Peak definitions: x-position (0-1) and height (0-1)
        float[][] peaks = {
            {0.00f, 0.20f}, {0.10f, 0.55f}, {0.22f, 0.35f}, {0.32f, 0.75f},
            {0.42f, 0.45f}, {0.55f, 0.90f}, {0.65f, 0.50f}, {0.75f, 0.70f},
            {0.85f, 0.40f}, {0.95f, 0.60f}, {1.00f, 0.25f}
        };

        // Dark green-blue mountain color
        Color mtColor = new Color(0.18f, 0.30f, 0.22f, 0.90f);
        px.setColor(mtColor);

        // For each column, find the height by interpolating between nearest peaks
        for (int x = 0; x < w; x++) {
            float xf = (float) x / w;
            float height = 0f;

            // Find surrounding peaks and interpolate
            for (int p = 0; p < peaks.length - 1; p++) {
                if (xf >= peaks[p][0] && xf <= peaks[p + 1][0]) {
                    float t = (xf - peaks[p][0]) / (peaks[p + 1][0] - peaks[p][0]);
                    // Smooth interpolation (cosine)
                    t = (1f - MathUtils.cos(t * MathUtils.PI)) * 0.5f;
                    height = peaks[p][1] + t * (peaks[p + 1][1] - peaks[p][1]);
                    break;
                }
            }

            int colH = (int)(height * h);
            for (int y = 0; y < colH; y++) {
                px.drawPixel(x, h - 1 - y);
            }
        }

        // Lighter ridge highlight on top portion
        Color highlight = new Color(0.25f, 0.42f, 0.32f, 0.40f);
        px.setColor(highlight);
        for (int x = 0; x < w; x++) {
            float xf = (float) x / w;
            float height = 0f;
            for (int p = 0; p < peaks.length - 1; p++) {
                if (xf >= peaks[p][0] && xf <= peaks[p + 1][0]) {
                    float t = (xf - peaks[p][0]) / (peaks[p + 1][0] - peaks[p][0]);
                    t = (1f - MathUtils.cos(t * MathUtils.PI)) * 0.5f;
                    height = peaks[p][1] + t * (peaks[p + 1][1] - peaks[p][1]);
                    break;
                }
            }
            int colH = (int)(height * h);
            int highlightRows = Math.min(colH, 6);
            for (int y = colH - highlightRows; y < colH; y++) {
                if (y >= 0) px.drawPixel(x, h - 1 - y);
            }
        }

        // Snow caps on the tallest peaks
        Color snow = new Color(0.90f, 0.93f, 0.95f, 0.50f);
        px.setColor(snow);
        for (int x = 0; x < w; x++) {
            float xf = (float) x / w;
            float height = 0f;
            for (int p = 0; p < peaks.length - 1; p++) {
                if (xf >= peaks[p][0] && xf <= peaks[p + 1][0]) {
                    float t = (xf - peaks[p][0]) / (peaks[p + 1][0] - peaks[p][0]);
                    t = (1f - MathUtils.cos(t * MathUtils.PI)) * 0.5f;
                    height = peaks[p][1] + t * (peaks[p + 1][1] - peaks[p][1]);
                    break;
                }
            }
            if (height > 0.65f) {
                int colH = (int)(height * h);
                int snowRows = (int)((height - 0.65f) * h * 0.3f);
                snowRows = Math.min(snowRows, 4);
                for (int y = colH - snowRows; y < colH; y++) {
                    if (y >= 0) px.drawPixel(x, h - 1 - y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildSunTexture() {
        int size = 128;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = size / 2;
        int cy = size / 2;

        // Concentric glow rings from outside in (outer soft, inner bright)
        for (int r = size / 2; r > 0; r--) {
            float t = (float) r / (size / 2);
            float a = (1f - t) * 0.12f;
            px.setColor(new Color(1f, 0.96f, 0.80f, a));
            px.fillCircle(cx, cy, r);
        }

        // Bright core
        px.setColor(new Color(1f, 0.98f, 0.88f, 0.25f));
        px.fillCircle(cx, cy, size / 8);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildLeafTexture() {
        int size = 10;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Simple leaf / petal shape using an ellipse approximation
        px.setColor(new Color(1f, 1f, 1f, 0.9f));
        px.fillCircle(5, 5, 3);
        px.fillCircle(4, 4, 2);
        px.fillCircle(6, 6, 2);

        // Stem hint
        px.setColor(new Color(1f, 1f, 1f, 0.6f));
        px.drawLine(2, 8, 5, 5);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }
}
