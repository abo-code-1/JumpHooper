package com.duddlejump.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public final class BackgroundManager implements Disposable {

    private static final float CROSSFADE_WINDOW = 900f;

    private final Array<SkyBiome> biomes = new Array<>();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Color topBlend = new Color();
    private final Color bottomBlend = new Color();

    private SkyBiome currentBiome;
    private SkyBiome nextBiome;
    private float fadeAmount;

    public BackgroundManager() {
        biomes.add(new EarthBiome());
        biomes.add(new CloudsBiome());
        biomes.add(new StratosphereBiome());
        biomes.add(new SpaceBiome());
        biomes.add(new MoonBiome());
        biomes.add(new SunBiome());
        biomes.add(new CosmicBiome());
        currentBiome = biomes.first();
    }

    public void update(float altitude) {
        SkyBiome cur = biomes.first();
        SkyBiome nxt = null;
        for (int i = 0; i < biomes.size; i++) {
            SkyBiome b = biomes.get(i);
            if (altitude >= b.getStartAltitude()) {
                cur = b;
                nxt = (i + 1 < biomes.size) ? biomes.get(i + 1) : null;
            }
        }
        currentBiome = cur;
        nextBiome = nxt;
        if (nxt != null) {
            float start = nxt.getStartAltitude() - CROSSFADE_WINDOW;
            fadeAmount = MathUtils.clamp((altitude - start) / CROSSFADE_WINDOW, 0f, 1f);
        } else {
            fadeAmount = 0f;
        }
    }

    public void renderGradient(OrthographicCamera camera, float viewW, float viewH) {
        Color top = currentBiome.getTopColor();
        Color bottom = currentBiome.getBottomColor();
        if (nextBiome != null && fadeAmount > 0f) {
            top = lerp(top, nextBiome.getTopColor(), fadeAmount, topBlend);
            bottom = lerp(bottom, nextBiome.getBottomColor(), fadeAmount, bottomBlend);
        }
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float x = camera.position.x - viewW * 0.5f;
        float y = camera.position.y - viewH * 0.5f;
        shapes.rect(x, y, viewW, viewH, bottom, bottom, top, top);
        shapes.end();
    }

    public void renderDecorations(SpriteBatch batch, OrthographicCamera camera,
                                  float viewW, float viewH) {
        float currentAlpha = nextBiome != null ? (1f - fadeAmount) : 1f;
        currentBiome.renderDecorations(batch, camera.position.x, camera.position.y,
            viewW, viewH, currentAlpha);
        if (nextBiome != null && fadeAmount > 0f) {
            nextBiome.renderDecorations(batch, camera.position.x, camera.position.y,
                viewW, viewH, fadeAmount);
        }
    }

    public SkyBiome getCurrentBiome() {
        return currentBiome;
    }

    public String getBannerLabel() {
        if (nextBiome != null && fadeAmount >= 0.5f) {
            return nextBiome.getName();
        }
        return currentBiome.getName();
    }

    private static Color lerp(Color a, Color b, float t, Color out) {
        out.r = a.r + (b.r - a.r) * t;
        out.g = a.g + (b.g - a.g) * t;
        out.b = a.b + (b.b - a.b) * t;
        out.a = 1f;
        return out;
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
