package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

public final class ParticlePool implements Disposable {

    private static final int MAX = 768;
    private static final float GRAVITY = -800f;

    private final Texture dot;
    private final Texture softGlow;
    private final Texture sparkle;
    private final float[] px = new float[MAX];
    private final float[] py = new float[MAX];
    private final float[] vx = new float[MAX];
    private final float[] vy = new float[MAX];
    private final float[] life = new float[MAX];
    private final float[] maxLife = new float[MAX];
    private final float[] size = new float[MAX];
    private final float[] cr = new float[MAX];
    private final float[] cg = new float[MAX];
    private final float[] cb = new float[MAX];
    private final boolean[] grav = new boolean[MAX];
    private final int[] shape = new int[MAX]; // 0=dot, 1=glow, 2=sparkle
    private final float[] rot = new float[MAX];
    private final float[] rotSpeed = new float[MAX];
    private int count = 0;

    public ParticlePool() {
        Pixmap pix = new Pixmap(6, 6, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fillCircle(3, 3, 3);
        dot = new Texture(pix);
        pix.dispose();

        softGlow = buildSoftGlow();
        sparkle = buildSparkle();
    }

    public void emitRadial(float x, float y, int n, float speed, float sz,
                           float r, float g, float b, boolean gravity, float lifetime) {
        for (int i = 0; i < n && count < MAX; i++) {
            float angle = MathUtils.random(MathUtils.PI2);
            float spd = speed * MathUtils.random(0.4f, 1f);
            init(x, y, MathUtils.cos(angle) * spd, MathUtils.sin(angle) * spd,
                sz * MathUtils.random(0.6f, 1.2f), r, g, b, gravity,
                lifetime * MathUtils.random(0.7f, 1.3f), MathUtils.random(0, 2));
        }
    }

    public void emitUpward(float x, float y, int n, float speed, float spread,
                           float sz, float r, float g, float b, float lifetime) {
        for (int i = 0; i < n && count < MAX; i++) {
            float vxr = MathUtils.random(-spread, spread);
            float vyr = speed * MathUtils.random(0.6f, 1.2f);
            init(x + MathUtils.random(-8f, 8f), y, vxr, vyr,
                sz * MathUtils.random(0.5f, 1f), r, g, b, true,
                lifetime * MathUtils.random(0.8f, 1.2f), i % 3);
        }
    }

    public void emitFalling(float x, float y, int n, float speed, float spread,
                            float sz, float r, float g, float b, float lifetime) {
        for (int i = 0; i < n && count < MAX; i++) {
            float vxr = MathUtils.random(-spread, spread);
            float vyr = -speed * MathUtils.random(0.3f, 1f);
            init(x + MathUtils.random(-12f, 12f), y, vxr, vyr,
                sz * MathUtils.random(0.5f, 1.2f), r, g, b, true,
                lifetime * MathUtils.random(0.7f, 1.1f), 0);
        }
    }

    public void emitRising(float x, float y, int n, float speed,
                           float sz, float r, float g, float b, float lifetime) {
        for (int i = 0; i < n && count < MAX; i++) {
            float vxr = MathUtils.random(-30f, 30f);
            float vyr = speed * MathUtils.random(0.5f, 1f);
            init(x + MathUtils.random(-16f, 16f), y, vxr, vyr,
                sz * MathUtils.random(0.4f, 1f), r, g, b, false,
                lifetime * MathUtils.random(0.8f, 1.2f), 1);
        }
    }

    public void emitBurst(float x, float y, int n, float speed, float sz,
                          float r, float g, float b, float lifetime) {
        for (int i = 0; i < n && count < MAX; i++) {
            float angle = MathUtils.random(MathUtils.PI2);
            float spd = speed * MathUtils.random(0.5f, 1.2f);
            init(x, y, MathUtils.cos(angle) * spd, MathUtils.sin(angle) * spd,
                sz * MathUtils.random(0.8f, 1.5f), r, g, b, false,
                lifetime * MathUtils.random(0.6f, 1.4f), 2);
        }
    }

    public void update(float dt) {
        for (int i = count - 1; i >= 0; i--) {
            life[i] -= dt;
            if (life[i] <= 0f) {
                remove(i);
                continue;
            }
            if (grav[i]) {
                vy[i] += GRAVITY * dt;
            }
            px[i] += vx[i] * dt;
            py[i] += vy[i] * dt;
            rot[i] += rotSpeed[i] * dt;

            // Slow down particles over time for more natural feel
            vx[i] *= 0.998f;
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < count; i++) {
            float lifeRatio = life[i] / maxLife[i];
            float alpha = Math.min(1f, lifeRatio * 2.5f);
            float s = size[i] * (0.3f + 0.7f * lifeRatio);

            Texture tex;
            switch (shape[i]) {
                case 1:
                    tex = softGlow;
                    // Glow particles are larger and more transparent
                    s *= 2f;
                    alpha *= 0.5f;
                    break;
                case 2:
                    tex = sparkle;
                    // Sparkles flash
                    alpha *= 0.6f + 0.4f * MathUtils.sin(rot[i] * 10f);
                    break;
                default:
                    tex = dot;
                    break;
            }

            batch.setColor(cr[i], cg[i], cb[i], alpha);
            batch.draw(tex, px[i] - s * 0.5f, py[i] - s * 0.5f, s, s);
        }
        batch.setColor(Color.WHITE);
    }

    public int getCount() {
        return count;
    }

    private void init(float x, float y, float velX, float velY,
                      float sz, float r, float g, float b, boolean gravity,
                      float lifetime, int shapeType) {
        if (count >= MAX) return;
        int idx = count++;
        px[idx] = x;
        py[idx] = y;
        vx[idx] = velX;
        vy[idx] = velY;
        size[idx] = sz;
        cr[idx] = r;
        cg[idx] = g;
        cb[idx] = b;
        grav[idx] = gravity;
        life[idx] = lifetime;
        maxLife[idx] = lifetime;
        shape[idx] = shapeType;
        rot[idx] = MathUtils.random(MathUtils.PI2);
        rotSpeed[idx] = MathUtils.random(-3f, 3f);
    }

    private void remove(int i) {
        count--;
        if (i < count) {
            px[i] = px[count];
            py[i] = py[count];
            vx[i] = vx[count];
            vy[i] = vy[count];
            life[i] = life[count];
            maxLife[i] = maxLife[count];
            size[i] = size[count];
            cr[i] = cr[count];
            cg[i] = cg[count];
            cb[i] = cb[count];
            grav[i] = grav[count];
            shape[i] = shape[count];
            rot[i] = rot[count];
            rotSpeed[i] = rotSpeed[count];
        }
    }

    private static Texture buildSoftGlow() {
        int s = 16;
        Pixmap pix = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();
        int cx = s / 2, cy = s / 2;
        for (int r = s / 2; r > 0; r--) {
            float t = 1f - (float) r / (s / 2);
            pix.setColor(new Color(1f, 1f, 1f, t * t));
            pix.fillCircle(cx, cy, r);
        }
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    private static Texture buildSparkle() {
        int s = 12;
        Pixmap pix = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();
        int cx = s / 2, cy = s / 2;
        // Cross shape
        pix.setColor(Color.WHITE);
        pix.drawLine(cx, 0, cx, s - 1);
        pix.drawLine(0, cy, s - 1, cy);
        pix.drawLine(1, 1, s - 2, s - 2);
        pix.drawLine(s - 2, 1, 1, s - 2);
        // Center dot
        pix.fillCircle(cx, cy, 2);
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        dot.dispose();
        softGlow.dispose();
        sparkle.dispose();
    }
}
