package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WhitePlatform extends Platform {

    private static final float FADE_SPEED = 2.5f;

    private final Texture texture;
    private boolean fading = false;

    public WhitePlatform(Texture texture, float x, float y) {
        super(x, y);
        this.texture = texture;
    }

    @Override
    public void update(float dt) {
        if (fading) {
            alpha -= FADE_SPEED * dt;
            if (alpha <= 0f) {
                alpha = 0f;
                destroyed = true;
            }
        }
    }

    @Override
    public void onContact(Doodle doodle) {
        doodle.bounce();
        fading = true;
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
        return PlatformKind.WHITE;
    }
}
