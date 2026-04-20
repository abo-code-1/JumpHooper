package com.duddlejump.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.duddlejump.entities.BluePlatform;
import com.duddlejump.entities.GreenPlatform;
import com.duddlejump.entities.Platform;
import com.duddlejump.entities.PlatformKind;
import com.duddlejump.entities.RedPlatform;
import com.duddlejump.entities.WhitePlatform;

public final class PlatformFactory implements Disposable {

    private final Texture greenTex;
    private final Texture redTex;
    private final Texture blueTex;
    private final Texture whiteTex;

    public PlatformFactory() {
        this.greenTex = buildTexture(new Color(0.34f, 0.78f, 0.36f, 1f), new Color(0.18f, 0.52f, 0.22f, 1f));
        this.redTex = buildTexture(new Color(0.92f, 0.30f, 0.32f, 1f), new Color(0.60f, 0.14f, 0.18f, 1f));
        this.blueTex = buildTexture(new Color(0.34f, 0.58f, 0.95f, 1f), new Color(0.18f, 0.32f, 0.68f, 1f));
        this.whiteTex = buildTexture(new Color(0.95f, 0.95f, 0.97f, 1f), new Color(0.62f, 0.62f, 0.70f, 1f));
    }

    public Platform create(PlatformKind kind, float x, float y) {
        switch (kind) {
            case GREEN: return new GreenPlatform(greenTex, x, y);
            case RED:   return new RedPlatform(redTex, x, y);
            case BLUE:  return new BluePlatform(blueTex, x, y);
            case WHITE: return new WhitePlatform(whiteTex, x, y);
            default:
                throw new IllegalArgumentException("Unknown PlatformKind: " + kind);
        }
    }

    public Platform random(float x, float y, float redProb, float blueProb, float whiteProb) {
        float r = MathUtils.random();
        if ((r -= whiteProb) < 0f) return create(PlatformKind.WHITE, x, y);
        if ((r -= redProb) < 0f)   return create(PlatformKind.RED, x, y);
        if ((r -= blueProb) < 0f)  return create(PlatformKind.BLUE, x, y);
        return create(PlatformKind.GREEN, x, y);
    }

    private static Texture buildTexture(Color top, Color bottom) {
        int w = 72;
        int h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(new Color(0f, 0f, 0f, 0f));
        px.fill();
        int half = h / 2;
        px.setColor(top);
        px.fillRectangle(2, 0, w - 4, half);
        px.setColor(bottom);
        px.fillRectangle(2, half, w - 4, h - half);
        px.setColor(new Color(0f, 0f, 0f, 0.25f));
        px.drawRectangle(2, 0, w - 4, h);
        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        greenTex.dispose();
        redTex.dispose();
        blueTex.dispose();
        whiteTex.dispose();
    }
}
