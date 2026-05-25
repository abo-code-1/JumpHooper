package com.starbots.starjump.fx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.Config;
import com.starbots.starjump.assets.Assets;

/**
 * A living parallax space backdrop drawn over the original nebula image:
 * three depth layers of twinkling stars, an occasional shooting star, and a
 * slowly drifting planet. All layers parallax-scroll with the player's climb,
 * so the world feels like it is genuinely moving through space.
 */
public final class SpaceBackground {

    private static final class Star {
        float x, y, size, phase, twinkle, parallax, r, g, b;
    }

    private final Texture nebula;
    private final Texture glow;

    private final Star[] stars;
    private final float[] planet = new float[2]; // x, y
    private float planetParallax = 0.18f;
    private float time;

    // Shooting star state.
    private float shootTimer;
    private float shootX, shootY, shootVx, shootVy, shootLife;

    private float nebulaOffset;

    public SpaceBackground(Assets assets) {
        this.nebula = assets.background;
        this.glow = assets.glow;

        stars = new Star[110];
        for (int i = 0; i < stars.length; i++) {
            Star s = new Star();
            s.x = MathUtils.random(0f, Config.WORLD_WIDTH);
            s.y = MathUtils.random(0f, Config.WORLD_HEIGHT);
            int layer = i % 3; // 0 far, 1 mid, 2 near
            s.size = 1.5f + layer * 1.8f + MathUtils.random(0f, 1.5f);
            s.parallax = 0.15f + layer * 0.28f;
            s.phase = MathUtils.random(0f, MathUtils.PI2);
            s.twinkle = MathUtils.random(1.5f, 4f);
            float tint = MathUtils.random();
            s.r = 1f;
            s.g = tint < 0.4f ? 0.85f + MathUtils.random(0.15f) : 1f;
            s.b = 1f;
            if (tint > 0.8f) { s.r = 0.8f; s.g = 0.9f; } // a few bluish
            stars[i] = s;
        }
        planet[0] = Config.WORLD_WIDTH * 0.72f;
        planet[1] = Config.WORLD_HEIGHT * 0.28f;
        shootTimer = MathUtils.random(2f, 5f);
    }

    /**
     * @param scrollDy how far the world scrolled this frame (>= 0). In y-up
     *                 camera space the backdrop moves <em>down</em> as the
     *                 player climbs, so positions decrease.
     */
    public void update(float dt, float scrollDy) {
        time += dt;
        nebulaOffset -= scrollDy * 0.08f;
        if (nebulaOffset <= -Config.WORLD_HEIGHT) nebulaOffset += Config.WORLD_HEIGHT;

        for (Star s : stars) {
            s.y -= scrollDy * s.parallax;
            if (s.y < 0) {
                s.y += Config.WORLD_HEIGHT;
                s.x = MathUtils.random(0f, Config.WORLD_WIDTH);
            }
        }

        planet[1] -= scrollDy * planetParallax + dt * 3f;
        if (planet[1] < -150) {
            planet[1] = Config.WORLD_HEIGHT + 150;
            planet[0] = MathUtils.random(Config.WORLD_WIDTH * 0.1f, Config.WORLD_WIDTH * 0.9f);
        }

        // Shooting stars streak downward from the top.
        if (shootLife > 0) {
            shootX += shootVx * dt;
            shootY += shootVy * dt;
            shootLife -= dt;
        } else {
            shootTimer -= dt;
            if (shootTimer <= 0) {
                shootTimer = MathUtils.random(2.5f, 6f);
                shootX = MathUtils.random(0f, Config.WORLD_WIDTH);
                shootY = MathUtils.random(Config.WORLD_HEIGHT * 0.6f, Config.WORLD_HEIGHT);
                float dir = MathUtils.random(-0.6f, 0.6f);
                float speed = MathUtils.random(420f, 700f);
                shootVx = MathUtils.cos(dir) * speed;
                shootVy = -(MathUtils.sin(dir) * speed + 200f);
                shootLife = 0.7f;
            }
        }
    }

    /** Draw the backdrop. Must be called inside an active {@link SpriteBatch}. */
    public void render(SpriteBatch batch) {
        // Base nebula, gently drifting (two stacked copies for seamless wrap).
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(nebula, 0, nebulaOffset, Config.WORLD_WIDTH, Config.WORLD_HEIGHT);
        batch.draw(nebula, 0, nebulaOffset + Config.WORLD_HEIGHT, Config.WORLD_WIDTH, Config.WORLD_HEIGHT);

        // Glowing elements use additive blending.
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);

        // Drifting planet (soft, low alpha so it never hides gameplay).
        batch.setColor(0.35f, 0.85f, 0.95f, 0.30f);
        batch.draw(glow, planet[0] - 90, planet[1] - 90, 180, 180);
        batch.setColor(0.5f, 0.95f, 1f, 0.18f);
        batch.draw(glow, planet[0] - 130, planet[1] - 130, 260, 260);

        // Stars (twinkle).
        for (Star s : stars) {
            float a = 0.35f + 0.45f * (0.5f + 0.5f * MathUtils.sin(time * s.twinkle + s.phase));
            batch.setColor(s.r, s.g, s.b, a);
            float sz = s.size * 2.4f;
            batch.draw(glow, s.x - sz / 2f, s.y - sz / 2f, sz, sz);
        }

        // Shooting star trail.
        if (shootLife > 0) {
            float a = MathUtils.clamp(shootLife / 0.7f, 0f, 1f);
            for (int i = 0; i < 6; i++) {
                float t = i / 6f;
                float px = shootX - shootVx * 0.02f * i;
                float py = shootY - shootVy * 0.02f * i;
                float sz = 10f * (1f - t);
                batch.setColor(1f, 1f, 1f, a * (1f - t));
                batch.draw(glow, px - sz / 2f, py - sz / 2f, sz, sz);
            }
        }

        // Restore default blending + color for subsequent draws.
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
