package com.duddlejump.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Platform {

    public static final float WIDTH = 72f;
    public static final float HEIGHT = 18f;

    protected final Rectangle bounds = new Rectangle();
    protected boolean destroyed = false;
    protected float alpha = 1f;

    protected Platform(float x, float y) {
        bounds.set(x, y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public float getAlpha() {
        return alpha;
    }

    public void update(float dt) {
    }

    public abstract void onContact(Doodle doodle);

    public abstract void render(SpriteBatch batch);

    public abstract PlatformKind getKind();
}
