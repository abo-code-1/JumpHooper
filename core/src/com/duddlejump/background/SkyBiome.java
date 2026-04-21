package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface SkyBiome {

    String getName();

    float getStartAltitude();

    Color getTopColor();

    Color getBottomColor();

    default void renderDecorations(SpriteBatch batch, float cameraX, float cameraY, float viewW, float viewH, float alpha) {
    }
}
