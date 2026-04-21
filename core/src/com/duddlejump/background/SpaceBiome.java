package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class SpaceBiome implements SkyBiome {

    private static final Color TOP = new Color(0.02f, 0.02f, 0.08f, 1f);
    private static final Color BOTTOM = new Color(0.07f, 0.10f, 0.28f, 1f);

    private static final int STAR_COUNT = 80;

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private Texture dot;

    public SpaceBiome() {
        MathUtils.random.setSeed(42L);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, 1f);
            starY[i] = MathUtils.random(0f, 1f);
            starSize[i] = MathUtils.random(1.2f, 3.5f);
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
        if (dot == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dot = new Texture(pix);
            pix.dispose();
        }
        float baseY = cameraY - viewH * 0.5f;
        float parallax = cameraY * 0.1f;
        batch.setColor(1f, 1f, 1f, alpha);
        for (int i = 0; i < STAR_COUNT; i++) {
            float px = starX[i] * viewW;
            float py = baseY + ((starY[i] * viewH - parallax) % viewH + viewH) % viewH;
            float s = starSize[i];
            batch.draw(dot, px, py, s, s);
        }
        batch.setColor(Color.WHITE);
    }
}
