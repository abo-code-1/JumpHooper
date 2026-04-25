package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.duddlejump.Config;

public class BluePlatform extends Platform {

    private static final float HORIZONTAL_SPEED = 110f;

    private final Texture texture;
    private float vx;

    public BluePlatform(Texture texture, float x, float y) {
        super(x, y);
        this.texture = texture;
        this.vx = HORIZONTAL_SPEED;
    }

    @Override
    public void update(float dt) {
        bounds.x += vx * dt;
        if (bounds.x < 0f) {
            bounds.x = 0f;
            vx = Math.abs(vx);
        } else if (bounds.x + bounds.width > Config.WORLD_WIDTH) {
            bounds.x = Config.WORLD_WIDTH - bounds.width;
            vx = -Math.abs(vx);
        }
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
        return PlatformKind.BLUE;
    }
}
