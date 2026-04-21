package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class CosmicBiome implements SkyBiome {

    private static final Color TOP = new Color(0.12f, 0.02f, 0.22f, 1f);
    private static final Color BOTTOM = new Color(0.38f, 0.08f, 0.42f, 1f);

    private static final int STAR_COUNT = 150;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final Color[] starColor = new Color[STAR_COUNT];
    private Texture dot;

    public CosmicBiome() {
        MathUtils.random.setSeed(91L);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, 1f);
            starY[i] = MathUtils.random(0f, 1f);
            starSize[i] = MathUtils.random(1.5f, 4.5f);
            float hue = MathUtils.random(0f, 1f);
            if (hue < 0.4f) {
                starColor[i] = new Color(0.95f, 0.75f, 1f, 1f);
            } else if (hue < 0.75f) {
                starColor[i] = new Color(0.65f, 0.85f, 1f, 1f);
            } else {
                starColor[i] = new Color(1f, 0.95f, 0.75f, 1f);
            }
        }
    }

    @Override
    public String getName() {
        return "cosmic";
    }

    @Override
    public float getStartAltitude() {
        return 55000f;
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
        if (dot == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dot = new Texture(pix);
            pix.dispose();
        }
        float baseY = cameraY - viewH * 0.5f;
        float parallax = cameraY * 0.08f;
        for (int i = 0; i < STAR_COUNT; i++) {
            float px = starX[i] * viewW;
            float py = baseY + ((starY[i] * viewH - parallax) % viewH + viewH) % viewH;
            float s = starSize[i];
            Color c = starColor[i];
            batch.setColor(c.r, c.g, c.b, alpha);
            batch.draw(dot, px, py, s, s);
        }
        batch.setColor(Color.WHITE);
    }
}
