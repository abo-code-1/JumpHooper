package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;

public final class StratosphereBiome implements SkyBiome {

    private static final Color TOP = new Color(0.15f, 0.22f, 0.50f, 1f);
    private static final Color BOTTOM = new Color(0.35f, 0.55f, 0.82f, 1f);

    @Override
    public String getName() {
        return "stratosphere";
    }

    @Override
    public float getStartAltitude() {
        return 5000f;
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
