package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;

public final class EarthBiome implements SkyBiome {

    private static final Color TOP = new Color(0.53f, 0.81f, 0.98f, 1f);
    private static final Color BOTTOM = new Color(0.87f, 0.95f, 0.78f, 1f);

    @Override
    public String getName() {
        return "earth";
    }

    @Override
    public float getStartAltitude() {
        return 0f;
    }

    @Override
    public Color getTopColor() {
        return TOP;
    }

    @Override
    public Color getBottomColor() {
        return BOTTOM;
    }
}
