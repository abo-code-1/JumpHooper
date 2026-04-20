package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class Doodle implements Disposable {

    public static final float WIDTH = 48f;
    public static final float HEIGHT = 48f;

    private final Texture texture;
    private final boolean ownsTexture;
    private final Vector2 position = new Vector2();
    private final Rectangle bounds = new Rectangle();

    public Doodle(Texture texture, float x, float y) {
        this(texture, x, y, false);
    }

    public Doodle(Texture texture, float x, float y, boolean ownsTexture) {
        this.texture = texture;
        this.ownsTexture = ownsTexture;
        this.position.set(x, y);
        this.bounds.set(x, y, WIDTH, HEIGHT);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, WIDTH, HEIGHT);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        bounds.setPosition(position.x, position.y);
        return bounds;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    @Override
    public void dispose() {
        if (ownsTexture) {
            texture.dispose();
        }
    }
}
