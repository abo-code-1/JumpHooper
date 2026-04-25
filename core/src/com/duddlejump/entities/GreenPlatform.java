package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GreenPlatform extends Platform {

    private final Texture texture;

    public GreenPlatform(Texture texture, float x, float y) {
        super(x, y);
        this.texture = texture;
    }

    @Override
    public void onContact(Doodle doodle) {
        doodle.bounce();
    }

    @Override
    public void render(SpriteBatch batch) {
        Color prev = batch.getColor();
        batch.setColor(prev.r, prev.g, prev.b, alpha);
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(prev);
    }

    @Override
    public PlatformKind getKind() {
        return PlatformKind.GREEN;
    }
}
