package com.duddlejump.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.duddlejump.entities.BluePlatform;
import com.duddlejump.entities.GreenPlatform;
import com.duddlejump.entities.Platform;
import com.duddlejump.entities.PlatformKind;
import com.duddlejump.entities.RedPlatform;
import com.duddlejump.entities.SpringPlatform;
import com.duddlejump.entities.WhitePlatform;

/** Factory Method — builds platform instances without exposing subclass logic to callers. */
public final class PlatformFactory implements Disposable {

    private final Texture greenTex;
    private final Texture redTex;
    private final Texture blueTex;
    private final Texture whiteTex;
    private final Texture springTex;

    public PlatformFactory() {
        this.greenTex = buildGreenPlatform();
        this.redTex = buildRedPlatform();
        this.blueTex = buildBluePlatform();
        this.whiteTex = buildWhitePlatform();
        this.springTex = buildSpringTexture();
    }

    public Platform create(PlatformKind kind, float x, float y) {
        switch (kind) {
            case GREEN:  return new GreenPlatform(greenTex, x, y);
            case RED:    return new RedPlatform(redTex, x, y);
            case BLUE:   return new BluePlatform(blueTex, x, y);
            case WHITE:  return new WhitePlatform(whiteTex, x, y);
            case SPRING: return new SpringPlatform(greenTex, springTex, x, y);
            default:
                throw new IllegalArgumentException("Unknown PlatformKind: " + kind);
        }
    }

    public Platform random(float x, float y, float redProb, float blueProb,
                           float whiteProb, float springProb) {
        float r = MathUtils.random();
        if ((r -= whiteProb) < 0f)  return create(PlatformKind.WHITE, x, y);
        if ((r -= redProb) < 0f)    return create(PlatformKind.RED, x, y);
        if ((r -= blueProb) < 0f)   return create(PlatformKind.BLUE, x, y);
        if ((r -= springProb) < 0f) return create(PlatformKind.SPRING, x, y);
        return create(PlatformKind.GREEN, x, y);
    }

    // --- Green platform: lush, 3D, grassy ---
    private static Texture buildGreenPlatform() {
        int w = 72, h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int r = 6;

        // Drop shadow
        px.setColor(new Color(0.08f, 0.30f, 0.10f, 0.45f));
        fillRoundedRect(px, 3, 4, w - 6, h - 3, r);

        // Dark edge (bottom bevel)
        px.setColor(new Color(0.16f, 0.48f, 0.20f, 1f));
        fillRoundedRect(px, 1, 2, w - 2, h - 3, r);

        // Main body - rich green
        px.setColor(new Color(0.22f, 0.65f, 0.28f, 1f));
        fillRoundedRect(px, 1, 1, w - 2, h - 4, r);

        // Mid highlight
        px.setColor(new Color(0.30f, 0.76f, 0.35f, 1f));
        fillRoundedRect(px, 3, 1, w - 6, h / 2 + 1, r - 1);

        // Top highlight - bright
        px.setColor(new Color(0.42f, 0.88f, 0.45f, 1f));
        fillRoundedRect(px, 5, 1, w - 10, h / 2 - 2, r - 2);

        // Specular shine strip
        px.setColor(new Color(0.65f, 0.96f, 0.60f, 0.55f));
        fillRoundedRect(px, 8, 2, w - 16, 3, 2);

        // Gloss dot
        px.setColor(new Color(0.80f, 1f, 0.75f, 0.35f));
        px.fillCircle(w / 4, 4, 3);

        // Grass tufts on top (varied heights)
        int[] turfX = {6, 12, 19, 26, 33, 40, 47, 54, 61};
        int[] turfH = {4, 3, 5, 3, 4, 3, 5, 3, 4};
        for (int i = 0; i < turfX.length; i++) {
            px.setColor(new Color(0.28f, 0.75f + (i % 2) * 0.08f, 0.32f, 1f));
            px.fillRectangle(turfX[i], 0, 2, turfH[i]);
            if (i < turfX.length - 1) {
                px.setColor(new Color(0.35f, 0.82f, 0.38f, 0.7f));
                px.fillRectangle(turfX[i] + 3, 0, 1, turfH[i] - 1);
            }
        }

        // Subtle edge outline
        px.setColor(new Color(0.12f, 0.40f, 0.15f, 0.5f));
        drawRoundedRect(px, 1, 1, w - 2, h - 4, r);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    // --- Red platform: cracked, dangerous, glowing ---
    private static Texture buildRedPlatform() {
        int w = 72, h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int r = 6;

        // Danger glow
        px.setColor(new Color(0.95f, 0.20f, 0.10f, 0.18f));
        fillRoundedRect(px, 0, 0, w, h, r + 1);

        // Shadow
        px.setColor(new Color(0.35f, 0.06f, 0.05f, 0.55f));
        fillRoundedRect(px, 3, 4, w - 6, h - 3, r);

        // Dark bottom
        px.setColor(new Color(0.60f, 0.15f, 0.12f, 1f));
        fillRoundedRect(px, 1, 2, w - 2, h - 3, r);

        // Main body - warm red
        px.setColor(new Color(0.85f, 0.25f, 0.22f, 1f));
        fillRoundedRect(px, 1, 1, w - 2, h - 4, r);

        // Top highlight
        px.setColor(new Color(0.95f, 0.38f, 0.32f, 1f));
        fillRoundedRect(px, 3, 1, w - 6, h / 2, r - 1);

        // Hot specular
        px.setColor(new Color(1f, 0.55f, 0.42f, 0.5f));
        fillRoundedRect(px, 8, 2, w - 16, 3, 2);

        // Crack network (more detailed)
        px.setColor(new Color(0.45f, 0.08f, 0.06f, 0.8f));
        // Main crack
        px.drawLine(w / 3, 2, w / 3 + 2, h / 2);
        px.drawLine(w / 3 + 2, h / 2, w / 3 + 6, h / 2 - 2);
        px.drawLine(w / 3 + 6, h / 2 - 2, w / 3 + 8, h - 4);
        // Branch crack
        px.drawLine(w / 3 + 2, h / 2, w / 3 - 2, h - 3);
        // Second crack
        px.drawLine(w * 2 / 3, 3, w * 2 / 3 - 2, h / 2 + 1);
        px.drawLine(w * 2 / 3 - 2, h / 2 + 1, w * 2 / 3 + 3, h - 3);
        // Small crack
        px.drawLine(w / 2, 3, w / 2 + 2, h / 2);

        // Danger warning dots (glowing)
        px.setColor(new Color(1f, 0.70f, 0.25f, 0.6f));
        px.fillCircle(10, h / 2, 3);
        px.fillCircle(w - 10, h / 2, 3);
        px.setColor(new Color(1f, 0.85f, 0.45f, 0.4f));
        px.fillCircle(10, h / 2 - 1, 2);
        px.fillCircle(w - 10, h / 2 - 1, 2);

        // Crumble debris dots
        px.setColor(new Color(0.72f, 0.20f, 0.15f, 0.5f));
        px.fillCircle(22, h - 3, 1);
        px.fillCircle(50, h - 2, 1);
        px.fillCircle(38, h - 3, 1);

        // Edge outline
        px.setColor(new Color(0.42f, 0.08f, 0.06f, 0.6f));
        drawRoundedRect(px, 1, 1, w - 2, h - 4, r);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    // --- Blue platform: icy, crystalline, sparkling ---
    private static Texture buildBluePlatform() {
        int w = 72, h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int r = 6;

        // Ice glow
        px.setColor(new Color(0.40f, 0.70f, 1f, 0.12f));
        fillRoundedRect(px, 0, 0, w, h, r + 1);

        // Shadow
        px.setColor(new Color(0.08f, 0.18f, 0.45f, 0.5f));
        fillRoundedRect(px, 3, 4, w - 6, h - 3, r);

        // Dark bottom
        px.setColor(new Color(0.20f, 0.42f, 0.78f, 1f));
        fillRoundedRect(px, 1, 2, w - 2, h - 3, r);

        // Main body - ice blue
        px.setColor(new Color(0.30f, 0.58f, 0.92f, 1f));
        fillRoundedRect(px, 1, 1, w - 2, h - 4, r);

        // Top highlight
        px.setColor(new Color(0.48f, 0.75f, 0.98f, 1f));
        fillRoundedRect(px, 3, 1, w - 6, h / 2, r - 1);

        // Ice crystal shine
        px.setColor(new Color(0.75f, 0.90f, 1f, 0.7f));
        fillRoundedRect(px, 8, 2, w - 16, 3, 2);

        // Bright specular
        px.setColor(new Color(0.90f, 0.96f, 1f, 0.45f));
        px.fillCircle(w / 3, 4, 4);

        // Sparkle dots (crystalline)
        px.setColor(new Color(1f, 1f, 1f, 0.95f));
        px.fillCircle(14, 4, 1);
        px.fillCircle(30, 6, 2);
        px.fillCircle(48, 3, 1);
        px.fillCircle(58, 5, 1);
        // Sparkle crosses
        px.setColor(new Color(0.95f, 0.98f, 1f, 0.7f));
        drawSparkle(px, 22, 5);
        drawSparkle(px, 42, 4);
        drawSparkle(px, 62, 7);

        // Ice crystal lines
        px.setColor(new Color(0.55f, 0.78f, 1f, 0.35f));
        px.drawLine(12, 3, 18, h - 4);
        px.drawLine(38, 2, 44, h - 3);
        px.drawLine(55, 4, 60, h - 3);

        // Frost edge
        px.setColor(new Color(0.55f, 0.78f, 1f, 0.5f));
        drawRoundedRect(px, 1, 1, w - 2, h - 4, r);

        // Movement arrows (animated feel)
        px.setColor(new Color(0.25f, 0.50f, 0.90f, 0.5f));
        // Left arrows
        px.drawLine(8, h / 2, 14, h / 2 - 4);
        px.drawLine(8, h / 2, 14, h / 2 + 4);
        px.drawLine(12, h / 2, 18, h / 2 - 4);
        px.drawLine(12, h / 2, 18, h / 2 + 4);
        // Right arrows
        px.drawLine(w - 8, h / 2, w - 14, h / 2 - 4);
        px.drawLine(w - 8, h / 2, w - 14, h / 2 + 4);
        px.drawLine(w - 12, h / 2, w - 18, h / 2 - 4);
        px.drawLine(w - 12, h / 2, w - 18, h / 2 + 4);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    // --- White platform: ghostly, ethereal, shimmering ---
    private static Texture buildWhitePlatform() {
        int w = 72, h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int r = 6;

        // Ethereal outer glow
        px.setColor(new Color(0.80f, 0.82f, 0.92f, 0.15f));
        fillRoundedRect(px, 0, 0, w, h, r + 1);

        // Soft glow underneath
        px.setColor(new Color(0.70f, 0.72f, 0.82f, 0.20f));
        fillRoundedRect(px, 2, 3, w - 4, h - 2, r);

        // Main body - ethereal
        px.setColor(new Color(0.92f, 0.93f, 0.97f, 0.80f));
        fillRoundedRect(px, 1, 1, w - 2, h - 3, r);

        // Top shine
        px.setColor(new Color(0.96f, 0.97f, 1f, 0.65f));
        fillRoundedRect(px, 3, 1, w - 6, h / 2, r - 1);

        // Bright core
        px.setColor(new Color(1f, 1f, 1f, 0.50f));
        fillRoundedRect(px, 8, 3, w - 16, h / 2 - 2, 3);

        // Shimmer dots (floating ghost particles)
        px.setColor(new Color(1f, 1f, 1f, 0.7f));
        px.fillCircle(10, 4, 2);
        px.fillCircle(24, 6, 1);
        px.fillCircle(36, 3, 2);
        px.fillCircle(48, 5, 1);
        px.fillCircle(60, 4, 2);

        // Wispy tendrils at edges
        px.setColor(new Color(0.88f, 0.90f, 0.96f, 0.30f));
        px.drawLine(5, h - 2, 2, h + 2);
        px.drawLine(15, h - 2, 13, h + 1);
        px.drawLine(w - 5, h - 2, w - 2, h + 2);
        px.drawLine(w - 15, h - 2, w - 13, h + 1);

        // Ghost outline (barely visible)
        px.setColor(new Color(0.75f, 0.78f, 0.88f, 0.25f));
        drawRoundedRect(px, 1, 1, w - 2, h - 3, r);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    // --- Spring texture: detailed metallic coil ---
    private static Texture buildSpringTexture() {
        int w = 24, h = 20;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        // Base plate shadow
        px.setColor(new Color(0.35f, 0.35f, 0.38f, 0.6f));
        px.fillRectangle(3, h - 3, w - 6, 3);

        // Base plate
        px.setColor(new Color(0.55f, 0.55f, 0.60f, 1f));
        px.fillRectangle(3, h - 5, w - 6, 4);
        // Base plate highlight
        px.setColor(new Color(0.72f, 0.72f, 0.78f, 1f));
        px.fillRectangle(4, h - 5, w - 8, 2);
        // Base plate shine
        px.setColor(new Color(0.85f, 0.85f, 0.90f, 0.5f));
        px.fillRectangle(5, h - 5, w - 10, 1);

        // Spring coils (with 3D effect)
        for (int i = 0; i < 4; i++) {
            int y = h - 7 - i * 4;
            float t = (float) i / 3f;
            int indent = 3 + (int)(t * 2);

            // Coil shadow
            px.setColor(new Color(0.42f, 0.35f, 0.08f, 0.6f));
            px.fillRectangle(indent + 1, y + 2, w - indent * 2, 2);

            // Coil body (dark side)
            px.setColor(new Color(0.72f, 0.58f, 0.12f, 1f));
            px.fillRectangle(indent, y, w - indent * 2, 3);

            // Coil body (bright)
            px.setColor(new Color(0.88f, 0.72f, 0.22f, 1f));
            px.fillRectangle(indent, y, w - indent * 2, 2);

            // Coil highlight (top edge)
            px.setColor(new Color(1f, 0.92f, 0.50f, 0.85f));
            px.drawLine(indent + 1, y, w - indent - 2, y);

            // Coil specular
            px.setColor(new Color(1f, 0.98f, 0.70f, 0.4f));
            px.drawLine(indent + 2, y, indent + 6, y);
        }

        // Top cap with bounce indicator
        px.setColor(new Color(0.75f, 0.22f, 0.15f, 1f));
        px.fillRectangle(2, 0, w - 4, 4);
        px.setColor(new Color(0.92f, 0.32f, 0.22f, 1f));
        px.fillRectangle(3, 0, w - 6, 2);
        // Cap shine
        px.setColor(new Color(1f, 0.50f, 0.38f, 0.6f));
        px.fillRectangle(4, 0, w - 8, 1);
        // Arrow on cap
        px.setColor(new Color(1f, 0.85f, 0.75f, 0.5f));
        px.drawLine(w / 2, 0, w / 2 - 2, 2);
        px.drawLine(w / 2, 0, w / 2 + 2, 2);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    // --- Sparkle cross helper ---
    private static void drawSparkle(Pixmap px, int x, int y) {
        px.drawLine(x - 2, y, x + 2, y);
        px.drawLine(x, y - 2, x, y + 2);
    }

    // --- Rounded rectangle utilities ---

    private static void fillRoundedRect(Pixmap px, int x, int y, int w, int h, int r) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        px.fillRectangle(x + r, y, w - 2 * r, h);
        px.fillRectangle(x, y + r, w, h - 2 * r);
        px.fillCircle(x + r, y + r, r);
        px.fillCircle(x + w - r - 1, y + r, r);
        px.fillCircle(x + r, y + h - r - 1, r);
        px.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    private static void drawRoundedRect(Pixmap px, int x, int y, int w, int h, int r) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        px.drawLine(x + r, y, x + w - r - 1, y);
        px.drawLine(x + r, y + h - 1, x + w - r - 1, y + h - 1);
        px.drawLine(x, y + r, x, y + h - r - 1);
        px.drawLine(x + w - 1, y + r, x + w - 1, y + h - r - 1);
        px.drawCircle(x + r, y + r, r);
        px.drawCircle(x + w - r - 1, y + r, r);
        px.drawCircle(x + r, y + h - r - 1, r);
        px.drawCircle(x + w - r - 1, y + h - r - 1, r);
    }

    @Override
    public void dispose() {
        greenTex.dispose();
        redTex.dispose();
        blueTex.dispose();
        whiteTex.dispose();
        springTex.dispose();
    }
}
