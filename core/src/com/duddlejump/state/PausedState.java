package com.duddlejump.state;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.duddlejump.Config;
import com.duddlejump.entities.Platform;
import com.duddlejump.screens.GameScreen;

public final class PausedState implements GameState, Disposable {

    private final GameScreen screen;
    private final GlyphLayout layout = new GlyphLayout();

    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private Texture dimPixel;

    public PausedState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        if (titleFont == null) {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(2.4f);
            bodyFont = new BitmapFont();
            bodyFont.getData().setScale(1.3f);

            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dimPixel = new Texture(pix);
            pix.dispose();
        }
    }

    @Override
    public void update(float dt) {
        if (screen.getInput().isPauseRequested()) {
            screen.setState(screen.getPlayingState());
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Render world behind
        Array<Platform> platforms = screen.getSpawner().getPlatforms();
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).render(batch);
        }
        screen.getDoodle().render(batch);

        float cy = screen.getCamera().position.y;

        // Dim overlay
        batch.setColor(0.03f, 0.02f, 0.08f, 0.50f);
        batch.draw(dimPixel, 0, cy - Config.VIEWPORT_HEIGHT * 0.5f,
            Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        batch.setColor(Color.WHITE);

        // Pause title
        titleFont.setColor(new Color(0.20f, 0.95f, 0.92f, 0.9f));
        drawCentered(batch, titleFont, "PAUSED", cy + Config.VIEWPORT_HEIGHT * 0.08f);

        // Resume prompt
        bodyFont.setColor(new Color(0.75f, 0.68f, 0.90f, 0.7f));
        drawCentered(batch, bodyFont, "press P or ESC to resume", cy - Config.VIEWPORT_HEIGHT * 0.05f);
    }

    @Override
    public void exit() {
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            bodyFont.dispose();
            dimPixel.dispose();
        }
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
