package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.duddlejump.Config;
import com.duddlejump.events.EventBus;
import com.duddlejump.input.InputController;

public class Doodle implements Disposable {

    public static final float WIDTH = 56f;
    public static final float HEIGHT = 64f;

    private static final float SQUASH_SPEED = 12f;
    private static final float STRETCH_AMOUNT = 0.25f;

    private final Texture jumpTex;
    private final Texture fallTex;
    private final Texture idleTex;
    private final Texture glowTex;
    private final Vector2 position = new Vector2();
    private final Vector2 previousPosition = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Rectangle feet = new Rectangle();
    private boolean facingRight = true;
    private boolean shielded = false;

    private float squashStretch = 1f;
    private float targetSquash = 1f;
    private float eyeOffsetX = 0f;
    private float trailTimer = 0f;
    private float animTime = 0f;

    // Trail effect
    private static final int TRAIL_LENGTH = 6;
    private final float[] trailX = new float[TRAIL_LENGTH];
    private final float[] trailY = new float[TRAIL_LENGTH];
    private int trailIndex = 0;
    private float trailInterval = 0f;

    public Doodle(Texture texture, float x, float y) {
        this(texture, x, y, false);
    }

    public Doodle(Texture texture, float x, float y, boolean ownsTexture) {
        this.jumpTex = buildJumpSprite();
        this.fallTex = buildFallSprite();
        this.idleTex = buildIdleSprite();
        this.glowTex = buildGlowTexture();
        if (ownsTexture && texture != null) {
            texture.dispose();
        }
        this.position.set(x, y);
        this.previousPosition.set(x, y);
        this.bounds.set(x, y, WIDTH, HEIGHT);
        for (int i = 0; i < TRAIL_LENGTH; i++) {
            trailX[i] = x;
            trailY[i] = y;
        }
    }

    public void update(float dt, InputController input) {
        previousPosition.set(position);
        float horizontal = input.getHorizontal();
        if (horizontal != 0f) {
            velocity.x += horizontal * Config.HORIZONTAL_ACCEL * dt;
            facingRight = horizontal > 0f;
            eyeOffsetX = horizontal * 2f;
        } else {
            velocity.x *= Config.HORIZONTAL_DRAG;
            if (Math.abs(velocity.x) < 1f) {
                velocity.x = 0f;
            }
            eyeOffsetX *= 0.9f;
        }
        velocity.x = MathUtils.clamp(velocity.x, -Config.MAX_HORIZONTAL_SPEED, Config.MAX_HORIZONTAL_SPEED);
        velocity.y += Config.GRAVITY * dt;

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        if (velocity.y > 200f) {
            targetSquash = 1f + STRETCH_AMOUNT;
        } else if (velocity.y < -200f) {
            targetSquash = 1f - STRETCH_AMOUNT * 0.6f;
        } else {
            targetSquash = 1f;
        }
        squashStretch += (targetSquash - squashStretch) * SQUASH_SPEED * dt;

        animTime += dt;
        trailTimer += dt;

        // Update trail positions
        trailInterval += dt;
        if (trailInterval > 0.02f) {
            trailInterval = 0f;
            trailIndex = (trailIndex + 1) % TRAIL_LENGTH;
            trailX[trailIndex] = position.x + WIDTH * 0.5f;
            trailY[trailIndex] = position.y + HEIGHT * 0.3f;
        }

        wrapHorizontally();
        syncBounds();
    }

    public void bounce() {
        velocity.y = Config.JUMP_VELOCITY;
        squashStretch = 1f + STRETCH_AMOUNT * 1.5f;
    }

    public void checkContact(Array<Platform> platforms, EventBus bus) {
        if (velocity.y >= 0f) {
            return;
        }
        syncBounds();
        feet.set(bounds.x + 6f, bounds.y, bounds.width - 12f, 8f);
        float previousBottom = previousPosition.y;
        for (int i = 0; i < platforms.size; i++) {
            Platform p = platforms.get(i);
            if (p.isDestroyed()) {
                continue;
            }
            Rectangle platformBounds = p.getBounds();
            float platformTop = platformBounds.y + platformBounds.height;
            boolean horizontalOverlap = feet.x < platformBounds.x + platformBounds.width
                && feet.x + feet.width > platformBounds.x;
            boolean crossedTop = previousBottom >= platformTop && feet.y <= platformTop;
            if (horizontalOverlap && crossedTop) {
                position.y = platformTop;
                syncBounds();
                bus.publishContact(this, p);
                return;
            }
            if (Intersector.overlaps(feet, platformBounds)) {
                position.y = platformTop;
                syncBounds();
                bus.publishContact(this, p);
                return;
            }
        }
    }

    public void boost(float verticalVelocity) {
        velocity.y = verticalVelocity;
        squashStretch = 1f + STRETCH_AMOUNT * 2f;
    }

    public boolean consumeShield() {
        if (shielded) {
            shielded = false;
            return true;
        }
        return false;
    }

    public void grantShield() {
        shielded = true;
    }

    public boolean isShielded() {
        return shielded;
    }

    public void render(SpriteBatch batch) {
        // Render motion trail when moving fast
        if (Math.abs(velocity.y) > 300f) {
            for (int i = 0; i < TRAIL_LENGTH; i++) {
                int idx = (trailIndex - i + TRAIL_LENGTH) % TRAIL_LENGTH;
                float alpha = (1f - (float) i / TRAIL_LENGTH) * 0.12f;
                float s = 12f - i * 1.5f;
                batch.setColor(0.15f, 0.90f, 0.85f, alpha);
                batch.draw(glowTex, trailX[idx] - s, trailY[idx] - s, s * 2f, s * 2f);
            }
        }

        // Antenna glow effect
        float glowPulse = 0.4f + 0.3f * MathUtils.sin(animTime * 4f);
        float glowSize = 18f;
        float antLeftX = position.x + WIDTH * 0.5f - 10f;
        float antRightX = position.x + WIDTH * 0.5f + 10f;
        float antY = position.y + HEIGHT * 0.5f + 14f;
        batch.setColor(1f, 0.7f, 0.2f, glowPulse * 0.25f);
        batch.draw(glowTex, antLeftX - glowSize * 0.5f, antY - glowSize * 0.5f, glowSize, glowSize);
        batch.draw(glowTex, antRightX - glowSize * 0.5f, antY - glowSize * 0.5f, glowSize, glowSize);

        Texture tex;
        if (velocity.y > 100f) {
            tex = jumpTex;
        } else if (velocity.y < -100f) {
            tex = fallTex;
        } else {
            tex = idleTex;
        }

        float scaleX = facingRight ? 1f : -1f;
        float scaleY = squashStretch;
        float invScaleX = 1f / squashStretch;

        float drawW = WIDTH * invScaleX * scaleX;
        float drawH = HEIGHT * scaleY;
        float offsetX = (WIDTH - WIDTH * invScaleX) * 0.5f;
        float offsetY = (HEIGHT - drawH) * 0.5f;

        float drawX;
        if (facingRight) {
            drawX = position.x + offsetX;
        } else {
            drawX = position.x + WIDTH - offsetX;
        }

        batch.setColor(Color.WHITE);
        batch.draw(tex, drawX, position.y + offsetY, drawW, drawH);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Rectangle getBounds() {
        syncBounds();
        return bounds;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        previousPosition.set(x, y);
        syncBounds();
    }

    public float getPreviousY() {
        return previousPosition.y;
    }

    private void wrapHorizontally() {
        if (position.x + WIDTH < 0f) {
            position.x = Config.WORLD_WIDTH;
        } else if (position.x > Config.WORLD_WIDTH) {
            position.x = -WIDTH;
        }
    }

    private void syncBounds() {
        bounds.setPosition(position.x, position.y);
    }

    @Override
    public void dispose() {
        jumpTex.dispose();
        fallTex.dispose();
        idleTex.dispose();
        glowTex.dispose();
    }

    // --- Glow texture for effects ---

    private static Texture buildGlowTexture() {
        int size = 32;
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = size / 2, cy = size / 2;
        for (int r = size / 2; r > 0; r--) {
            float t = 1f - (float) r / (size / 2);
            float a = t * t;
            p.setColor(new Color(1f, 1f, 1f, a));
            p.fillCircle(cx, cy, r);
        }
        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    // --- Sprite generation ---

    private static Texture buildIdleSprite() {
        int w = 56, h = 64;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();

        drawBodyEnhanced(p, w, h, 0, 0);
        drawArms(p, w, h, 0, 0, 0);
        drawFace(p, w, h, 0, 0, 0);
        drawAntennaeEnhanced(p, w, h, 0);
        drawFeetEnhanced(p, w, h, 0);

        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    private static Texture buildJumpSprite() {
        int w = 56, h = 64;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();

        drawBodyEnhanced(p, w, h, 0, -2);
        drawArms(p, w, h, 0, -2, 1);
        drawFace(p, w, h, 0, -3, 1);
        drawAntennaeEnhanced(p, w, h, -3);
        drawFeetEnhanced(p, w, h, 4);

        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    private static Texture buildFallSprite() {
        int w = 56, h = 64;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();

        drawBodyEnhanced(p, w, h, 0, 2);
        drawArms(p, w, h, 0, 2, 2);
        drawFace(p, w, h, 0, 2, 2);
        drawAntennaeEnhanced(p, w, h, 4);
        drawFeetEnhanced(p, w, h, -2);

        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    private static void drawBodyEnhanced(Pixmap p, int w, int h, int offX, int offY) {
        int cx = w / 2 + offX;
        int cy = h / 2 + offY + 4;

        // Outer glow
        p.setColor(new Color(0.12f, 0.65f, 0.65f, 0.12f));
        p.fillCircle(cx, cy, 22);

        // Shadow underneath
        p.setColor(new Color(0.05f, 0.28f, 0.30f, 0.45f));
        p.fillCircle(cx + 1, cy + 3, 19);

        // Main body - rich teal gradient (outer ring)
        p.setColor(new Color(0.10f, 0.62f, 0.65f, 1f));
        p.fillCircle(cx, cy, 19);

        // Main body - bright teal
        p.setColor(new Color(0.15f, 0.78f, 0.78f, 1f));
        p.fillCircle(cx, cy, 17);

        // Body highlight - lighter teal (top-left lit)
        p.setColor(new Color(0.28f, 0.92f, 0.90f, 1f));
        p.fillCircle(cx - 4, cy - 5, 13);

        // Secondary highlight
        p.setColor(new Color(0.40f, 0.95f, 0.92f, 0.7f));
        p.fillCircle(cx - 6, cy - 7, 8);

        // Specular highlight
        p.setColor(new Color(0.70f, 1f, 0.98f, 0.5f));
        p.fillCircle(cx - 7, cy - 8, 4);

        // Belly - soft light
        p.setColor(new Color(0.55f, 0.95f, 0.92f, 0.55f));
        p.fillCircle(cx + 1, cy + 5, 10);

        // Belly inner
        p.setColor(new Color(0.70f, 0.98f, 0.96f, 0.35f));
        p.fillCircle(cx + 1, cy + 6, 6);

        // Subtle outline
        p.setColor(new Color(0.04f, 0.40f, 0.42f, 0.7f));
        p.drawCircle(cx, cy, 18);
        p.drawCircle(cx, cy, 17);
    }

    private static void drawArms(Pixmap p, int w, int h, int offX, int offY, int expression) {
        int cx = w / 2 + offX;
        int cy = h / 2 + offY + 4;

        int armOffY = 0;
        if (expression == 1) armOffY = -3; // arms up when jumping
        else if (expression == 2) armOffY = 2; // arms down when falling

        // Left arm
        p.setColor(new Color(0.12f, 0.68f, 0.70f, 1f));
        for (int t = 0; t < 3; t++) {
            p.drawLine(cx - 16, cy + armOffY - t, cx - 22, cy - 4 + armOffY - t);
        }
        // Left hand
        p.setColor(new Color(0.15f, 0.78f, 0.78f, 1f));
        p.fillCircle(cx - 22, cy - 4 + armOffY, 3);

        // Right arm
        p.setColor(new Color(0.12f, 0.68f, 0.70f, 1f));
        for (int t = 0; t < 3; t++) {
            p.drawLine(cx + 16, cy + armOffY - t, cx + 22, cy - 4 + armOffY - t);
        }
        // Right hand
        p.setColor(new Color(0.15f, 0.78f, 0.78f, 1f));
        p.fillCircle(cx + 22, cy - 4 + armOffY, 3);
    }

    private static void drawFace(Pixmap p, int w, int h, int offX, int offY, int expression) {
        int cx = w / 2 + offX;
        int cy = h / 2 + offY + 2;

        // Left eye white with shading
        int lex = cx - 8;
        int ley = cy - 4;
        p.setColor(new Color(0.95f, 0.95f, 0.98f, 1f));
        p.fillCircle(lex, ley, 7);
        p.setColor(Color.WHITE);
        p.fillCircle(lex - 1, ley - 1, 6);

        // Right eye white with shading
        int rex = cx + 8;
        int rey = cy - 4;
        p.setColor(new Color(0.95f, 0.95f, 0.98f, 1f));
        p.fillCircle(rex, rey, 7);
        p.setColor(Color.WHITE);
        p.fillCircle(rex - 1, rey - 1, 6);

        // Eye outlines
        p.setColor(new Color(0.08f, 0.28f, 0.32f, 0.7f));
        p.drawCircle(lex, ley, 7);
        p.drawCircle(rex, rey, 7);

        // Pupils with direction
        int pupilOff = 0;
        if (expression == 1) pupilOff = -2;      // looking up (jumping)
        else if (expression == 2) pupilOff = 2;   // looking down (falling)

        // Pupil outer
        p.setColor(new Color(0.05f, 0.05f, 0.10f, 1f));
        p.fillCircle(lex + 1, ley + pupilOff, 4);
        p.fillCircle(rex + 1, rey + pupilOff, 4);

        // Pupil inner (darker)
        p.setColor(new Color(0.02f, 0.02f, 0.05f, 1f));
        p.fillCircle(lex + 1, ley + pupilOff, 3);
        p.fillCircle(rex + 1, rey + pupilOff, 3);

        // Large highlight
        p.setColor(Color.WHITE);
        p.fillCircle(lex + 3, ley + pupilOff - 2, 2);
        p.fillCircle(rex + 3, rey + pupilOff - 2, 2);

        // Small highlight
        p.setColor(new Color(1f, 1f, 1f, 0.8f));
        p.fillCircle(lex - 1, ley + pupilOff + 1, 1);
        p.fillCircle(rex - 1, rey + pupilOff + 1, 1);

        // Eyebrows
        if (expression == 1) {
            // Happy raised eyebrows
            p.setColor(new Color(0.08f, 0.45f, 0.48f, 0.8f));
            p.drawLine(lex - 5, ley - 9, lex + 4, ley - 10);
            p.drawLine(rex - 4, ley - 10, rex + 5, ley - 9);
        } else if (expression == 2) {
            // Worried eyebrows
            p.setColor(new Color(0.08f, 0.45f, 0.48f, 0.8f));
            p.drawLine(lex - 5, ley - 10, lex + 4, ley - 8);
            p.drawLine(rex - 4, ley - 8, rex + 5, ley - 10);
        }

        // Mouth
        if (expression == 1) {
            // Big happy open mouth (jumping)
            p.setColor(new Color(0.06f, 0.30f, 0.33f, 1f));
            p.fillCircle(cx, cy + 7, 5);
            // Tongue
            p.setColor(new Color(0.92f, 0.40f, 0.42f, 1f));
            p.fillCircle(cx, cy + 9, 3);
            p.setColor(new Color(0.98f, 0.50f, 0.50f, 1f));
            p.fillCircle(cx, cy + 8, 2);
        } else if (expression == 2) {
            // Worried 'O' mouth (falling)
            p.setColor(new Color(0.06f, 0.30f, 0.33f, 1f));
            p.fillCircle(cx, cy + 7, 4);
            p.setColor(new Color(0.12f, 0.40f, 0.42f, 1f));
            p.fillCircle(cx, cy + 7, 2);
        } else {
            // Cute closed smile
            p.setColor(new Color(0.06f, 0.30f, 0.33f, 1f));
            for (int t = 0; t < 2; t++) {
                p.drawLine(cx - 5, cy + 6 + t, cx - 2, cy + 8 + t);
                p.drawLine(cx - 2, cy + 8 + t, cx + 2, cy + 8 + t);
                p.drawLine(cx + 2, cy + 8 + t, cx + 5, cy + 6 + t);
            }
        }

        // Cheeks - rosy with glow
        p.setColor(new Color(0.98f, 0.50f, 0.50f, 0.25f));
        p.fillCircle(lex - 6, cy + 2, 5);
        p.fillCircle(rex + 6, cy + 2, 5);
        p.setColor(new Color(1f, 0.60f, 0.55f, 0.35f));
        p.fillCircle(lex - 5, cy + 2, 3);
        p.fillCircle(rex + 5, cy + 2, 3);
    }

    private static void drawAntennaeEnhanced(Pixmap p, int w, int h, int offY) {
        int cx = w / 2;
        int topY = h / 2 - 14 + offY;

        // Left antenna stem (thicker, with gradient)
        p.setColor(new Color(0.08f, 0.48f, 0.52f, 1f));
        for (int t = -1; t <= 1; t++) {
            p.drawLine(cx - 6 + t, topY, cx - 10 + t, topY - 12);
        }
        p.setColor(new Color(0.15f, 0.60f, 0.62f, 1f));
        p.drawLine(cx - 6, topY, cx - 10, topY - 12);

        // Left antenna tip - glowing orb
        p.setColor(new Color(1f, 0.55f, 0.10f, 0.4f));
        p.fillCircle(cx - 10, topY - 12, 5);
        p.setColor(new Color(1f, 0.65f, 0.15f, 1f));
        p.fillCircle(cx - 10, topY - 12, 3);
        p.setColor(new Color(1f, 0.85f, 0.40f, 1f));
        p.fillCircle(cx - 10, topY - 12, 2);
        p.setColor(new Color(1f, 0.95f, 0.70f, 0.9f));
        p.fillCircle(cx - 11, topY - 13, 1);

        // Right antenna stem
        p.setColor(new Color(0.08f, 0.48f, 0.52f, 1f));
        for (int t = -1; t <= 1; t++) {
            p.drawLine(cx + 6 + t, topY, cx + 10 + t, topY - 12);
        }
        p.setColor(new Color(0.15f, 0.60f, 0.62f, 1f));
        p.drawLine(cx + 6, topY, cx + 10, topY - 12);

        // Right antenna tip - glowing orb
        p.setColor(new Color(1f, 0.55f, 0.10f, 0.4f));
        p.fillCircle(cx + 10, topY - 12, 5);
        p.setColor(new Color(1f, 0.65f, 0.15f, 1f));
        p.fillCircle(cx + 10, topY - 12, 3);
        p.setColor(new Color(1f, 0.85f, 0.40f, 1f));
        p.fillCircle(cx + 10, topY - 12, 2);
        p.setColor(new Color(1f, 0.95f, 0.70f, 0.9f));
        p.fillCircle(cx + 11, topY - 13, 1);
    }

    private static void drawFeetEnhanced(Pixmap p, int w, int h, int offY) {
        int cy = h / 2 + 20 + offY;
        int cx = w / 2;

        // Left leg connector
        p.setColor(new Color(0.08f, 0.52f, 0.55f, 1f));
        p.fillRectangle(cx - 10, cy - 4, 4, 5);

        // Left foot shadow
        p.setColor(new Color(0.06f, 0.42f, 0.44f, 0.6f));
        p.fillCircle(cx - 8, cy + 1, 6);

        // Left foot
        p.setColor(new Color(0.10f, 0.58f, 0.60f, 1f));
        p.fillCircle(cx - 8, cy, 5);
        p.setColor(new Color(0.18f, 0.72f, 0.72f, 1f));
        p.fillCircle(cx - 8, cy - 1, 4);
        // Shoe highlight
        p.setColor(new Color(0.30f, 0.82f, 0.80f, 0.5f));
        p.fillCircle(cx - 9, cy - 2, 2);

        // Right leg connector
        p.setColor(new Color(0.08f, 0.52f, 0.55f, 1f));
        p.fillRectangle(cx + 6, cy - 4, 4, 5);

        // Right foot shadow
        p.setColor(new Color(0.06f, 0.42f, 0.44f, 0.6f));
        p.fillCircle(cx + 8, cy + 1, 6);

        // Right foot
        p.setColor(new Color(0.10f, 0.58f, 0.60f, 1f));
        p.fillCircle(cx + 8, cy, 5);
        p.setColor(new Color(0.18f, 0.72f, 0.72f, 1f));
        p.fillCircle(cx + 8, cy - 1, 4);
        // Shoe highlight
        p.setColor(new Color(0.30f, 0.82f, 0.80f, 0.5f));
        p.fillCircle(cx + 7, cy - 2, 2);
    }
}
