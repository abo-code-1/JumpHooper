package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;

public final class CloudsBiome implements SkyBiome {

    private static final Color TOP = new Color(0.38f, 0.62f, 0.93f, 1f);
    private static final Color BOTTOM = new Color(0.78f, 0.88f, 0.98f, 1f);

    @Override
    public String getName() {
        return "clouds";
    }

    @Override
    public float getStartAltitude() {
        return 2000f;
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
