package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class MoonBiome implements SkyBiome {

    private static final Color TOP = new Color(0.05f, 0.05f, 0.12f, 1f);
    private static final Color BOTTOM = new Color(0.18f, 0.20f, 0.30f, 1f);

    private Texture moon;

    @Override
    public String getName() {
        return "moon";
    }

    @Override
    public float getStartAltitude() {
        return 20000f;
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
        if (moon == null) {
            int r = 120;
            Pixmap pix = new Pixmap(r * 2, r * 2, Pixmap.Format.RGBA8888);
            pix.setColor(new Color(0f, 0f, 0f, 0f));
            pix.fill();
            pix.setColor(new Color(0.92f, 0.92f, 0.88f, 1f));
            pix.fillCircle(r, r, r - 2);
            pix.setColor(new Color(0.78f, 0.78f, 0.72f, 1f));
            pix.fillCircle((int) (r * 0.6f), (int) (r * 0.7f), 18);
            pix.fillCircle((int) (r * 1.35f), (int) (r * 0.5f), 12);
            pix.fillCircle((int) (r * 1.1f), (int) (r * 1.35f), 20);
            pix.fillCircle((int) (r * 0.4f), (int) (r * 1.2f), 10);
            moon = new Texture(pix);
            pix.dispose();
        }
        float mx = viewW * 0.62f;
        float my = cameraY + viewH * 0.18f;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(moon, mx, my, moon.getWidth() * 0.9f, moon.getHeight() * 0.9f);
        batch.setColor(Color.WHITE);
    }
}
