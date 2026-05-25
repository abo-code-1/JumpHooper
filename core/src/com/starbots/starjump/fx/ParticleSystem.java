package com.starbots.starjump.fx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import com.starbots.starjump.assets.Assets;

/**
 * A small additive-blended particle system with an <b>Object Pool</b> so bursts
 * never allocate during play. Coordinates are y-down world space (matching the
 * game model); the system flips to the camera at draw time.
 */
public final class ParticleSystem {

    /** One particle. Reset/reused via the pool. */
    public static final class Particle implements Pool.Poolable {
        float x, y, vx, vy, life, maxLife, size, gravity;
        float r, g, b;

        @Override public void reset() {
            x = y = vx = vy = life = maxLife = size = gravity = 0f;
            r = g = b = 1f;
        }
    }

    private final Texture glow;
    private final Pool<Particle> pool = new Pool<Particle>() {
        @Override protected Particle newObject() { return new Particle(); }
    };
    private final Array<Particle> active = new Array<>(false, 256);

    public ParticleSystem(Assets assets) {
        this.glow = assets.glow;
    }

    private void emit(float x, float y, float vx, float vy, float life, float size,
                      float gravity, float r, float g, float b) {
        Particle p = pool.obtain();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.life = p.maxLife = life; p.size = size; p.gravity = gravity;
        p.r = r; p.g = g; p.b = b;
        active.add(p);
    }

    /** Upward sparkle burst on a bounce. */
    public void jumpSparkle(float x, float y) {
        for (int i = 0; i < 10; i++) {
            float ang = MathUtils.random(MathUtils.PI * 0.2f, MathUtils.PI * 0.8f);
            float spd = MathUtils.random(40f, 130f);
            emit(x, y, MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd,
                    MathUtils.random(0.25f, 0.5f), MathUtils.random(6f, 12f),
                    180f, 1f, 0.95f, 0.5f);
        }
    }

    /** Fiery embers (lava landing). */
    public void embers(float x, float y) {
        for (int i = 0; i < 12; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float spd = MathUtils.random(30f, 120f);
            emit(x, y, MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd,
                    MathUtils.random(0.3f, 0.6f), MathUtils.random(6f, 14f),
                    -120f, 1f, MathUtils.random(0.3f, 0.6f), 0.15f);
        }
    }

    /** Earthy debris that falls (a platform crumbling). */
    public void debris(float x, float y) {
        for (int i = 0; i < 12; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float spd = MathUtils.random(30f, 130f);
            float shade = MathUtils.random(0.45f, 0.7f);
            emit(x, y, MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd,
                    MathUtils.random(0.3f, 0.6f), MathUtils.random(6f, 13f),
                    420f, shade, shade * 0.7f, shade * 0.4f);
        }
    }

    /** A big radial explosion (death, enemy kill, boss hit). */
    public void explosion(float x, float y, float r, float g, float b, int count) {
        for (int i = 0; i < count; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float spd = MathUtils.random(60f, 320f);
            emit(x, y, MathUtils.cos(ang) * spd, MathUtils.sin(ang) * spd,
                    MathUtils.random(0.35f, 0.8f), MathUtils.random(8f, 20f),
                    40f, r, g, b);
        }
    }

    public void update(float dt) {
        for (int i = active.size - 1; i >= 0; i--) {
            Particle p = active.get(i);
            p.life -= dt;
            if (p.life <= 0) {
                active.removeIndex(i);
                pool.free(p);
                continue;
            }
            p.vy += p.gravity * dt; // y-down: positive gravity pulls downward
            p.x += p.vx * dt;
            p.y += p.vy * dt;
        }
    }

    /** Draw additively. Must be called inside an active {@link SpriteBatch}. */
    public void render(SpriteBatch batch, float worldHeight) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Particle p : active) {
            float a = MathUtils.clamp(p.life / p.maxLife, 0f, 1f);
            batch.setColor(p.r, p.g, p.b, a);
            float drawY = worldHeight - p.y - p.size; // y-down -> y-up
            batch.draw(glow, p.x - p.size / 2f, drawY, p.size, p.size);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void clear() {
        for (Particle p : active) pool.free(p);
        active.clear();
    }
}
