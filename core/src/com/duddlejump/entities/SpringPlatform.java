package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.duddlejump.Config;

public class SpringPlatform extends Platform {

    private final Texture platformTexture;
    private final Texture springTexture;

    public SpringPlatform(Texture platformTexture, Texture springTexture, float x, float y) {
        super(x, y);
        this.platformTexture = platformTexture;
        this.springTexture = springTexture;
    }

    @Override
    public void onContact(Doodle doodle) {
        doodle.boost(Config.SPRING_VELOCITY);
    }

    @Override
    public void render(SpriteBatch batch) {
        Color prev = batch.getColor();
        batch.setColor(prev.r, prev.g, prev.b, alpha);
        batch.draw(platformTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        float sw = 24f;
        float sh = 18f;
        batch.draw(springTexture, bounds.x + (bounds.width - sw) * 0.5f,
            bounds.y + bounds.height, sw, sh);
        batch.setColor(prev);
    }

    @Override
    public PlatformKind getKind() {
        return PlatformKind.SPRING;
    }
}
