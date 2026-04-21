package com.duddlejump.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.duddlejump.Config;
import com.duddlejump.events.EventBus;
import com.duddlejump.input.InputController;

public class Doodle implements Disposable {

    public static final float WIDTH = 56f;
    public static final float HEIGHT = 64f;

    private final Texture texture;
    private final boolean ownsTexture;
    private final Vector2 position = new Vector2();
    private final Vector2 previousPosition = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Rectangle feet = new Rectangle();
    private boolean facingRight = true;
    private boolean shielded = false;

    public Doodle(Texture texture, float x, float y) {
        this(texture, x, y, false);
    }

    public Doodle(Texture texture, float x, float y, boolean ownsTexture) {
        this.texture = texture;
        this.ownsTexture = ownsTexture;
        this.position.set(x, y);
        this.previousPosition.set(x, y);
        this.bounds.set(x, y, WIDTH, HEIGHT);
    }

    public void update(float dt, InputController input) {
        previousPosition.set(position);
        float horizontal = input.getHorizontal();
        if (horizontal != 0f) {
            velocity.x += horizontal * Config.HORIZONTAL_ACCEL * dt;
            facingRight = horizontal > 0f;
        } else {
            velocity.x *= Config.HORIZONTAL_DRAG;
            if (Math.abs(velocity.x) < 1f) {
                velocity.x = 0f;
            }
        }
        velocity.x = MathUtils.clamp(velocity.x, -Config.MAX_HORIZONTAL_SPEED, Config.MAX_HORIZONTAL_SPEED);
        velocity.y += Config.GRAVITY * dt;

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        wrapHorizontally();
        syncBounds();
    }

    public void bounce() {
        velocity.y = Config.JUMP_VELOCITY;
    }

    public void checkContact(Array<Platform> platforms, EventBus bus) {
        if (velocity.y >= 0f) {
            return;
        }
        syncBounds();
        feet.set(bounds.x + 6f, bounds.y, bounds.width - 12f, 8f);
        float previousBottom = previousPosition.y;
        for (int i = 0; i < platforms.size; i++) {
            Platform p = platforms.get(i);
            if (p.isDestroyed()) {
                continue;
            }
            Rectangle platformBounds = p.getBounds();
            float platformTop = platformBounds.y + platformBounds.height;
            boolean horizontalOverlap = feet.x < platformBounds.x + platformBounds.width
                && feet.x + feet.width > platformBounds.x;
            boolean crossedTop = previousBottom >= platformTop && feet.y <= platformTop;
            if (horizontalOverlap && crossedTop) {
                position.y = platformTop;
                syncBounds();
                bus.publishContact(this, p);
                return;
            }
            if (Intersector.overlaps(feet, platformBounds)) {
                position.y = platformTop;
                syncBounds();
                bus.publishContact(this, p);
                return;
            }
        }
    }

    public void boost(float verticalVelocity) {
        velocity.y = verticalVelocity;
    }

    public boolean consumeShield() {
        if (shielded) {
            shielded = false;
            return true;
        }
        return false;
    }

    public void grantShield() {
        shielded = true;
    }

    public boolean isShielded() {
        return shielded;
    }

    public void render(SpriteBatch batch) {
        float drawW = WIDTH;
        float originX = facingRight ? position.x : position.x + WIDTH;
        float w = facingRight ? drawW : -drawW;
        batch.draw(texture, originX, position.y, w, HEIGHT);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Rectangle getBounds() {
        syncBounds();
        return bounds;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        previousPosition.set(x, y);
        syncBounds();
    }

    public float getPreviousY() {
        return previousPosition.y;
    }

    private void wrapHorizontally() {
        if (position.x + WIDTH < 0f) {
            position.x = Config.WORLD_WIDTH;
        } else if (position.x > Config.WORLD_WIDTH) {
            position.x = -WIDTH;
        }
    }

    private void syncBounds() {
        bounds.setPosition(position.x, position.y);
    }

    @Override
    public void dispose() {
        if (ownsTexture) {
            texture.dispose();
        }
    }
}
