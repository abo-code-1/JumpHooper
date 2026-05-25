package com.starbots.starjump.fx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.model.platform.PlatformKind;

/**
 * Visible "crush" effect for breakable platforms: the platform splits into two
 * halves drawn from its real texture, which tumble, spin and fall away while
 * fading out. Pieces live in y-down world space and flip to the camera at draw.
 */
public final class BreakingPlatformFx {

    private static final class Piece {
        TextureRegion region;
        float cx, cy, w, h;     // center + size
        float vx, vy, rot, rotSpeed, life, maxLife;
    }

    private static final float GRAVITY = 900f;   // y-down: pulls pieces down
    private static final float LIFE = 0.7f;

    private final Assets assets;
    private final Array<Piece> pieces = new Array<>();

    public BreakingPlatformFx(Assets assets) {
        this.assets = assets;
    }

    /** Spawn two falling halves of a platform at (x,y) = its top-left, size w×h. */
    public void shatter(PlatformKind kind, float x, float y, float w, float h) {
        Texture tex = assets.platform(kind);
        int tw = tex.getWidth();
        int th = tex.getHeight();
        TextureRegion left = new TextureRegion(tex, 0, 0, tw / 2, th);
        TextureRegion right = new TextureRegion(tex, tw / 2, 0, tw - tw / 2, th);

        addHalf(left,  x + w * 0.25f, y + h / 2f, w / 2f, h, -1f);
        addHalf(right, x + w * 0.75f, y + h / 2f, w / 2f, h,  1f);
    }

    private void addHalf(TextureRegion region, float cx, float cy, float w, float h, float dir) {
        Piece p = new Piece();
        p.region = region;
        p.cx = cx; p.cy = cy; p.w = w; p.h = h;
        p.vx = dir * MathUtils.random(50f, 120f);
        p.vy = MathUtils.random(-120f, -40f);   // a little upward pop before falling
        p.rot = 0f;
        p.rotSpeed = dir * MathUtils.random(180f, 420f);
        p.life = p.maxLife = LIFE;
        pieces.add(p);
    }

    public void update(float dt) {
        for (int i = pieces.size - 1; i >= 0; i--) {
            Piece p = pieces.get(i);
            p.vy += GRAVITY * dt;
            p.cx += p.vx * dt;
            p.cy += p.vy * dt;
            p.rot += p.rotSpeed * dt;
            p.life -= dt;
            if (p.life <= 0) pieces.removeIndex(i);
        }
    }

    /** Must be called inside an active {@link SpriteBatch}. */
    public void render(SpriteBatch batch, float worldHeight) {
        for (Piece p : pieces) {
            float a = MathUtils.clamp(p.life / p.maxLife, 0f, 1f);
            batch.setColor(1f, 1f, 1f, a);
            float drawY = worldHeight - p.cy - p.h / 2f; // y-down center -> y-up bottom-left
            batch.draw(p.region, p.cx - p.w / 2f, drawY,
                    p.w / 2f, p.h / 2f, p.w, p.h, 1f, 1f, p.rot);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void clear() {
        pieces.clear();
    }
}
