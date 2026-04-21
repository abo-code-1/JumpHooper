package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class CloudsBiome implements SkyBiome {

    private static final Color TOP = new Color(0.28f, 0.52f, 0.92f, 1f);
    private static final Color BOTTOM = new Color(0.68f, 0.82f, 0.96f, 1f);

    // --- Layer 0: far / background (small, dim) ---
    private static final int FAR_COUNT = 6;
    private final float[] farX = new float[FAR_COUNT];
    private final float[] farY = new float[FAR_COUNT];
    private final float[] farScale = new float[FAR_COUNT];

    // --- Layer 1: mid ---
    private static final int MID_COUNT = 6;
    private final float[] midX = new float[MID_COUNT];
    private final float[] midY = new float[MID_COUNT];
    private final float[] midScale = new float[MID_COUNT];

    // --- Layer 2: near / foreground (large, bright) ---
    private static final int NEAR_COUNT = 5;
    private final float[] nearX = new float[NEAR_COUNT];
    private final float[] nearY = new float[NEAR_COUNT];
    private final float[] nearScale = new float[NEAR_COUNT];

    // --- Cirrus wisps ---
    private static final int WISP_COUNT = 5;
    private final float[] wispX = new float[WISP_COUNT];
    private final float[] wispY = new float[WISP_COUNT];
    private final float[] wispLen = new float[WISP_COUNT];
    private final float[] wispAngle = new float[WISP_COUNT];

    // --- God rays ---
    private static final int RAY_COUNT = 5;
    private final float[] rayX = new float[RAY_COUNT];
    private final float[] rayW = new float[RAY_COUNT];
    private final float[] rayPhase = new float[RAY_COUNT];

    // Textures (lazy-init)
    private Texture cloudSmall;
    private Texture cloudMed;
    private Texture cloudLarge;
    private Texture sunRayTex;
    private Texture wispTex;
    private Texture rainbowTex;

    public CloudsBiome() {
        MathUtils.random.setSeed(33L);

        // Far layer
        for (int i = 0; i < FAR_COUNT; i++) {
            farX[i] = MathUtils.random(-0.05f, 1.05f);
            farY[i] = MathUtils.random(0.15f, 0.90f);
            farScale[i] = MathUtils.random(0.5f, 0.8f);
        }
        // Mid layer
        for (int i = 0; i < MID_COUNT; i++) {
            midX[i] = MathUtils.random(-0.08f, 1.08f);
            midY[i] = MathUtils.random(0.10f, 0.85f);
            midScale[i] = MathUtils.random(0.9f, 1.4f);
        }
        // Near layer
        for (int i = 0; i < NEAR_COUNT; i++) {
            nearX[i] = MathUtils.random(-0.10f, 1.10f);
            nearY[i] = MathUtils.random(0.05f, 0.75f);
            nearScale[i] = MathUtils.random(1.5f, 2.4f);
        }
        // Wisps
        for (int i = 0; i < WISP_COUNT; i++) {
            wispX[i] = MathUtils.random(-0.05f, 0.85f);
            wispY[i] = MathUtils.random(0.75f, 0.96f);
            wispLen[i] = MathUtils.random(0.15f, 0.30f);
            wispAngle[i] = MathUtils.random(-8f, 8f);
        }
        // God rays
        for (int i = 0; i < RAY_COUNT; i++) {
            rayX[i] = MathUtils.random(0.25f, 0.95f);
            rayW[i] = MathUtils.random(40f, 90f);
            rayPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
    }

    @Override
    public String getName() {
        return "clouds";
    }

    @Override
    public float getStartAltitude() {
        return 2000f;
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
        if (cloudSmall == null) {
            cloudSmall = buildCloudTexture(48, 22, 0);
            cloudMed = buildCloudTexture(80, 34, 1);
            cloudLarge = buildCloudTexture(130, 52, 2);
            sunRayTex = buildSunRayTexture();
            wispTex = buildWispTexture();
            rainbowTex = buildRainbowTexture();
        }

        float baseY = cameraY - viewH * 0.5f;
        float time = cameraY * 0.001f;

        // ---- Rainbow arc (faint, upper portion) ----
        float rbParallax = cameraY * 0.04f;
        float rbY = baseY + ((0.70f * viewH - rbParallax) % viewH + viewH) % viewH;
        batch.setColor(1f, 1f, 1f, alpha * 0.18f);
        float rbW = viewW * 0.7f;
        float rbH = rbW * 0.35f;
        batch.draw(rainbowTex, viewW * 0.15f, rbY, rbW, rbH);

        // ---- God rays from top-right ----
        for (int i = 0; i < RAY_COUNT; i++) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * 1.8f + rayPhase[i]);
            float rayAlpha = alpha * 0.07f * pulse;
            batch.setColor(1f, 0.97f, 0.85f, rayAlpha);

            float rx = rayX[i] * viewW;
            float rw = rayW[i];
            // Draw ray as a tall thin quad angled from top-right
            batch.draw(sunRayTex, rx, baseY, rw * 0.5f, viewH,
                rw, viewH, 1f, 1f, -20f,
                0, 0, sunRayTex.getWidth(), sunRayTex.getHeight(),
                false, false);
        }

        // ---- Far cloud layer (slow parallax, dim) ----
        float farPar = cameraY * 0.05f;
        for (int i = 0; i < FAR_COUNT; i++) {
            float cx = farX[i] * viewW;
            float cy = baseY + ((farY[i] * viewH - farPar * 0.2f) % viewH + viewH) % viewH;
            float s = farScale[i];
            batch.setColor(0.85f, 0.88f, 0.95f, alpha * 0.30f);
            batch.draw(cloudSmall, cx, cy, cloudSmall.getWidth() * s, cloudSmall.getHeight() * s);
        }

        // ---- Mid cloud layer ----
        float midPar = cameraY * 0.10f;
        for (int i = 0; i < MID_COUNT; i++) {
            float cx = midX[i] * viewW;
            float cy = baseY + ((midY[i] * viewH - midPar * 0.25f) % viewH + viewH) % viewH;
            float s = midScale[i];
            Texture tex = (i % 2 == 0) ? cloudMed : cloudSmall;
            batch.setColor(0.95f, 0.96f, 1f, alpha * 0.55f);
            batch.draw(tex, cx, cy, tex.getWidth() * s, tex.getHeight() * s);
        }

        // ---- Cirrus wisps at the top ----
        float wispPar = cameraY * 0.07f;
        for (int i = 0; i < WISP_COUNT; i++) {
            float wx = wispX[i] * viewW;
            float wy = baseY + ((wispY[i] * viewH - wispPar * 0.15f) % viewH + viewH) % viewH;
            float wLen = wispLen[i] * viewW;
            float wH = 8f;
            batch.setColor(1f, 1f, 1f, alpha * 0.22f);
            batch.draw(wispTex, wx, wy, 0, wH * 0.5f,
                wLen, wH, 1f, 1f, wispAngle[i],
                0, 0, wispTex.getWidth(), wispTex.getHeight(),
                false, false);
        }

        // ---- Near cloud layer (large, bright, fast parallax) ----
        float nearPar = cameraY * 0.16f;
        for (int i = 0; i < NEAR_COUNT; i++) {
            float cx = nearX[i] * viewW;
            float cy = baseY + ((nearY[i] * viewH - nearPar * 0.3f) % viewH + viewH) % viewH;
            float s = nearScale[i];
            Texture tex = (i % 3 == 0) ? cloudLarge : cloudMed;

            // Bottom shadow pass
            batch.setColor(0.70f, 0.75f, 0.85f, alpha * 0.25f);
            batch.draw(tex, cx + 2, cy - 3, tex.getWidth() * s, tex.getHeight() * s * 0.5f);

            // Main cloud body
            batch.setColor(1f, 1f, 1f, alpha * 0.80f);
            batch.draw(tex, cx, cy, tex.getWidth() * s, tex.getHeight() * s);

            // Top highlight
            batch.setColor(1f, 1f, 1f, alpha * 0.20f);
            float hlW = tex.getWidth() * s * 0.6f;
            float hlH = tex.getHeight() * s * 0.35f;
            batch.draw(cloudSmall, cx + tex.getWidth() * s * 0.2f,
                cy + tex.getHeight() * s * 0.55f, hlW, hlH);
        }

        batch.setColor(Color.WHITE);
    }

    // ---- Texture builders ----

    /**
     * Build a cloud texture with multiple overlapping circles,
     * bottom shadow, top highlight, and soft edges.
     * variant: 0=small, 1=medium, 2=large
     */
    private static Texture buildCloudTexture(int w, int h, int variant) {
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int cx = w / 2;
        int cy = h / 2;

        // Bottom shadow layer (offset down, darker)
        px.setColor(new Color(0.75f, 0.78f, 0.88f, 0.35f));
        if (variant == 0) {
            px.fillCircle(cx - w / 6, cy + 3, h / 3);
            px.fillCircle(cx + w / 6, cy + 3, h / 3);
            px.fillCircle(cx, cy + 2, (int) (h * 0.38f));
        } else if (variant == 1) {
            px.fillCircle(cx - w / 4, cy + 4, h / 3);
            px.fillCircle(cx, cy + 3, (int) (h * 0.42f));
            px.fillCircle(cx + w / 4, cy + 4, h / 3);
            px.fillCircle(cx - w / 8, cy + h / 4, h / 4);
            px.fillCircle(cx + w / 8, cy + h / 4, h / 4);
        } else {
            px.fillCircle(cx - w / 3, cy + 4, (int) (h * 0.32f));
            px.fillCircle(cx - w / 7, cy + 3, (int) (h * 0.40f));
            px.fillCircle(cx + w / 7, cy + 3, (int) (h * 0.38f));
            px.fillCircle(cx + w / 3, cy + 4, (int) (h * 0.30f));
            px.fillCircle(cx, cy + h / 4, h / 4);
        }

        // Main body (white, high alpha)
        px.setColor(new Color(1f, 1f, 1f, 0.85f));
        if (variant == 0) {
            px.fillCircle(cx - w / 6, cy, h / 3);
            px.fillCircle(cx + w / 6, cy, h / 3);
            px.fillCircle(cx, cy - 2, (int) (h * 0.40f));
        } else if (variant == 1) {
            px.fillCircle(cx - w / 4, cy, h / 3);
            px.fillCircle(cx, cy - 2, (int) (h * 0.45f));
            px.fillCircle(cx + w / 4, cy, h / 3);
            px.fillCircle(cx - w / 8, cy + 1, (int) (h * 0.36f));
            px.fillCircle(cx + w / 8, cy + 1, (int) (h * 0.36f));
        } else {
            px.fillCircle(cx - w / 3, cy - 1, (int) (h * 0.32f));
            px.fillCircle(cx - w / 7, cy - 3, (int) (h * 0.44f));
            px.fillCircle(cx + w / 7, cy - 2, (int) (h * 0.42f));
            px.fillCircle(cx + w / 3, cy, (int) (h * 0.30f));
            px.fillCircle(cx, cy + 2, (int) (h * 0.35f));
            px.fillCircle(cx - w / 5, cy + h / 5, h / 4);
            px.fillCircle(cx + w / 5, cy + h / 5, h / 4);
        }

        // Top highlight (brighter, smaller circles shifted up)
        px.setColor(new Color(1f, 1f, 1f, 0.40f));
        if (variant == 0) {
            px.fillCircle(cx, cy - h / 5, h / 4);
        } else if (variant == 1) {
            px.fillCircle(cx - w / 10, cy - h / 4, h / 4);
            px.fillCircle(cx + w / 10, cy - h / 5, h / 5);
        } else {
            px.fillCircle(cx - w / 8, cy - h / 4, h / 4);
            px.fillCircle(cx + w / 10, cy - h / 4, (int) (h * 0.22f));
            px.fillCircle(cx + w / 4, cy - h / 5, h / 5);
        }

        // Soft edge pass: fade out perimeter pixels
        softEdge(px, w, h);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    /** Apply radial alpha fade near the edges to soften the cloud outline. */
    private static void softEdge(Pixmap px, int w, int h) {
        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float rx = w * 0.5f;
        float ry = h * 0.5f;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = (x - cx) / rx;
                float dy = (y - cy) / ry;
                float dist = dx * dx + dy * dy;   // elliptical distance squared
                if (dist > 0.6f) {
                    int argb = px.getPixel(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    if (a == 0) continue;
                    float fade = 1f - MathUtils.clamp((dist - 0.6f) / 0.4f, 0f, 1f);
                    int na = (int) (a * fade);
                    px.drawPixel(x, y, (argb & 0x00FFFFFF) | (na << 24));
                }
            }
        }
    }

    private static Texture buildSunRayTexture() {
        int w = 32, h = 256;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int y = 0; y < h; y++) {
            float yFade = 1f - (float) y / h;                 // stronger at top
            for (int x = 0; x < w; x++) {
                float xFade = 1f - Math.abs(x - w * 0.5f) / (w * 0.5f);  // fade at sides
                float a = yFade * xFade * 0.45f;
                px.setColor(new Color(1f, 0.97f, 0.85f, a));
                px.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildWispTexture() {
        int w = 128, h = 8;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        for (int x = 0; x < w; x++) {
            // Taper alpha towards both ends
            float xFade = 1f - Math.abs(x - w * 0.5f) / (w * 0.5f);
            xFade = xFade * xFade;  // quadratic taper for smoother wisp
            for (int y = 0; y < h; y++) {
                float yFade = 1f - Math.abs(y - h * 0.5f) / (h * 0.5f);
                float a = xFade * yFade * 0.6f;
                px.setColor(new Color(1f, 1f, 1f, a));
                px.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static Texture buildRainbowTexture() {
        int w = 256, h = 96;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Rainbow band colors (ROYGBIV outer to inner)
        float[][] bands = {
            {1.0f, 0.20f, 0.20f},  // red
            {1.0f, 0.55f, 0.15f},  // orange
            {1.0f, 0.90f, 0.20f},  // yellow
            {0.20f, 0.85f, 0.30f}, // green
            {0.20f, 0.50f, 0.95f}, // blue
            {0.30f, 0.15f, 0.75f}, // indigo
            {0.55f, 0.15f, 0.80f}, // violet
        };

        float centerX = w * 0.5f;
        float centerY = h * 1.1f;     // center below the texture so we see the top arc
        float outerR = w * 0.48f;
        float bandThickness = 2.5f;
        float totalBand = bands.length * bandThickness;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                float innerR = outerR - totalBand;
                if (dist < innerR || dist > outerR) continue;
                if (dy > 0) continue;  // only upper arc

                float pos = (outerR - dist) / totalBand;  // 0=outer edge, 1=inner edge
                int bandIdx = MathUtils.clamp((int) (pos * bands.length), 0, bands.length - 1);

                // Fade at horizontal edges
                float edgeFade = 1f - Math.abs(dx) / (w * 0.48f);
                edgeFade = MathUtils.clamp(edgeFade * 3f, 0f, 1f);

                // Fade at band edges for smooth blending
                float bandPos = (pos * bands.length) - bandIdx;
                float bandFade = 1f - 2f * Math.abs(bandPos - 0.5f);
                bandFade = 0.5f + 0.5f * bandFade;

                float a = 0.35f * edgeFade * bandFade;

                px.setColor(new Color(bands[bandIdx][0], bands[bandIdx][1], bands[bandIdx][2], a));
                px.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }
}
