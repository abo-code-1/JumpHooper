package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class SunBiome implements SkyBiome {

    private static final Color TOP = new Color(0.98f, 0.42f, 0.22f, 1f);
    private static final Color BOTTOM = new Color(1.00f, 0.78f, 0.20f, 1f);

    private Texture sun;

    @Override
    public String getName() {
        return "sun";
    }

    @Override
    public float getStartAltitude() {
        return 35000f;
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
        if (sun == null) {
            int r = 160;
            Pixmap pix = new Pixmap(r * 2, r * 2, Pixmap.Format.RGBA8888);
            pix.setColor(new Color(0f, 0f, 0f, 0f));
            pix.fill();
            for (int i = r; i > 0; i--) {
                float t = 1f - (float) i / r;
                pix.setColor(new Color(1f, 0.95f - t * 0.2f, 0.35f + t * 0.5f, 0.08f + t * 0.9f));
                pix.fillCircle(r, r, i);
            }
            sun = new Texture(pix);
            pix.dispose();
        }
        float sx = viewW * 0.5f - sun.getWidth() * 0.5f;
        float sy = cameraY + viewH * 0.05f;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(sun, sx, sy);
        batch.setColor(Color.WHITE);
    }
}
