package com.starbots.starjump.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.starbots.starjump.Config;

/**
 * Drawing helper shared by every screen. Hides two pieces of bookkeeping:
 * <ol>
 *   <li>The model uses y-down coordinates (top = 0) like the original; this
 *       flips them to libGDX's y-up camera at draw time.</li>
 *   <li>Mixing {@link SpriteBatch} (images + text) with {@link ShapeRenderer}
 *       (cards + buttons), each with their own begin/end sessions.</li>
 * </ol>
 */
public final class Painter {

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Viewport viewport;

    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp3 = new Vector3();

    public Painter(SpriteBatch batch, ShapeRenderer shapes, Viewport viewport) {
        this.batch = batch;
        this.shapes = shapes;
        this.viewport = viewport;
    }

    // --- sprite batch sessions -------------------------------------------------

    public void begin() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
    }

    public void end() {
        batch.end();
    }

    /** Draw an image given its top-left corner in y-down world coords. */
    public void image(Texture tex, float left, float top, float w, float h) {
        image(tex, left, top, w, h, false);
    }

    public void image(Texture tex, float left, float top, float w, float h, boolean flipX) {
        float y = Config.WORLD_HEIGHT - top - h; // y-down -> y-up
        batch.draw(tex, left, y, w, h, 0, 0, tex.getWidth(), tex.getHeight(), flipX, false);
    }

    /** Stretch an image across the whole world (used for backgrounds). */
    public void fullscreen(Texture tex) {
        batch.draw(tex, 0, 0, Config.WORLD_WIDTH, Config.WORLD_HEIGHT);
    }

    public void text(BitmapFont font, String s, float left, float top) {
        font.draw(batch, s, left, Config.WORLD_HEIGHT - top);
    }

    public void textCentered(BitmapFont font, String s, float centerX, float top) {
        layout.setText(font, s);
        font.draw(batch, s, centerX - layout.width / 2f, Config.WORLD_HEIGHT - top);
    }

    /** Word-wrapped, left-aligned text inside a column of {@code width}px. */
    public float textWrapped(BitmapFont font, String s, float left, float top, float width) {
        layout.setText(font, s, font.getColor(), width, Align.left, true);
        font.draw(batch, layout, left, Config.WORLD_HEIGHT - top);
        return layout.height;
    }

    public float textWidth(BitmapFont font, String s) {
        layout.setText(font, s);
        return layout.width;
    }

    public float wrappedHeight(BitmapFont font, String s, float width) {
        layout.setText(font, s, Color.WHITE, width, Align.left, true);
        return layout.height;
    }

    // --- shape sessions --------------------------------------------------------

    public void beginShapes() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void endShapes() {
        shapes.end();
    }

    /** Filled rectangle, top-left in y-down coords. */
    public void rect(float left, float top, float w, float h, Color color) {
        shapes.setColor(color);
        shapes.rect(left, Config.WORLD_HEIGHT - top - h, w, h);
    }

    /** Vertical-gradient filled rectangle (visually {@code topColor} on top). */
    public void gradientRect(float left, float top, float w, float h, Color topColor, Color botColor) {
        float y = Config.WORLD_HEIGHT - top - h;
        // corners: bottom-left, bottom-right, top-right, top-left
        shapes.rect(left, y, w, h, botColor, botColor, topColor, topColor);
    }

    /** Rectangle outline of the given thickness, drawn as four filled bars. */
    public void border(float left, float top, float w, float h, float t, Color color) {
        shapes.setColor(color);
        float y = Config.WORLD_HEIGHT - top - h;
        shapes.rect(left, y, w, t);                 // bottom
        shapes.rect(left, y + h - t, w, t);         // top
        shapes.rect(left, y, t, h);                 // left
        shapes.rect(left + w - t, y, t, h);         // right
    }

    // --- input (top-down world coords) ----------------------------------------

    /** Current pointer position in y-down world coordinates. */
    public Vector2 pointer(Vector2 out) {
        tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(tmp3, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
        // unproject already yields y-up world; convert to y-down.
        out.set(tmp3.x, Config.WORLD_HEIGHT - tmp3.y);
        return out;
    }

    /** True on the frame the user releases a tap/click inside the given rect. */
    public boolean clicked(float left, float top, float w, float h) {
        if (!Gdx.input.justTouched()) return false;
        tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(tmp3, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
        float px = tmp3.x;
        float py = Config.WORLD_HEIGHT - tmp3.y;
        return px >= left && px <= left + w && py >= top && py <= top + h;
    }
}
