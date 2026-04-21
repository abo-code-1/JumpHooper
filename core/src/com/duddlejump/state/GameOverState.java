package com.duddlejump.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.Config;
import com.duddlejump.entities.Platform;
import com.duddlejump.managers.ScoreManager;
import com.duddlejump.screens.GameScreen;
import com.duddlejump.screens.MainMenuScreen;

public final class GameOverState implements GameState {

    private final GameScreen screen;
    private final GlyphLayout layout = new GlyphLayout();

    private BitmapFont titleFont;
    private BitmapFont scoreFont;
    private BitmapFont bodyFont;
    private BitmapFont smallFont;
    private Texture dimPixel;
    private Texture panelTex;
    private float finalScore;
    private int finalHigh;
    private boolean isNewBest;
    private float elapsed;

    public GameOverState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        screen.queueGameOver();
        finalScore = screen.getScore();
        ScoreManager.INSTANCE.setCurrent((int) finalScore);
        int prevHigh = ScoreManager.INSTANCE.getHigh();
        isNewBest = (int) finalScore >= prevHigh && (int) finalScore > 0;
        finalHigh = ScoreManager.INSTANCE.getHigh();
        elapsed = 0f;

        if (titleFont == null) {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(2.8f);
            scoreFont = new BitmapFont();
            scoreFont.getData().setScale(2f);
            bodyFont = new BitmapFont();
            bodyFont.getData().setScale(1.3f);
            smallFont = new BitmapFont();
            smallFont.getData().setScale(1f);

            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dimPixel = new Texture(pix);
            pix.dispose();

            panelTex = buildPanelTexture();
        }
    }

    @Override
    public void update(float dt) {
        elapsed += dt;

        boolean restart = Gdx.input.isKeyJustPressed(Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Keys.ENTER)
            || Gdx.input.justTouched();
        if (restart && elapsed > 0.5f) {
            screen.getGame().setScreen(new GameScreen(screen.getGame()));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            screen.getGame().setScreen(new MainMenuScreen(screen.getGame()));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Render game world behind
        Array<Platform> platforms = screen.getSpawner().getPlatforms();
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).render(batch);
        }
        screen.getDoodle().render(batch);

        float cy = screen.getCamera().position.y;
        float fadeIn = Math.min(1f, elapsed * 2f);

        // Dim overlay
        batch.setColor(0.03f, 0.02f, 0.08f, 0.65f * fadeIn);
        batch.draw(dimPixel, 0, cy - Config.VIEWPORT_HEIGHT * 0.5f,
            Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);

        // Semi-transparent panel
        float panelW = 320f;
        float panelH = 280f;
        float panelX = (Config.VIEWPORT_WIDTH - panelW) * 0.5f;
        float panelY = cy - panelH * 0.4f;
        batch.setColor(0.08f, 0.06f, 0.18f, 0.85f * fadeIn);
        batch.draw(panelTex, panelX, panelY, panelW, panelH);

        batch.setColor(Color.WHITE);

        // Title
        float slideIn = Math.min(1f, elapsed * 3f);
        titleFont.setColor(new Color(0.95f, 0.30f, 0.25f, fadeIn));
        drawCentered(batch, titleFont, "GAME OVER", cy + panelH * 0.28f * slideIn);

        // Score
        scoreFont.setColor(new Color(0.20f, 0.95f, 0.92f, fadeIn));
        drawCentered(batch, scoreFont, String.valueOf((int) finalScore), cy + panelH * 0.08f);

        bodyFont.setColor(new Color(0.55f, 0.50f, 0.70f, fadeIn * 0.8f));
        drawCentered(batch, bodyFont, "score", cy + panelH * 0.18f);

        // Best score
        bodyFont.setColor(new Color(0.80f, 0.72f, 0.95f, fadeIn * 0.7f));
        drawCentered(batch, bodyFont, "best  " + finalHigh, cy - panelH * 0.08f);

        // New best indicator
        if (isNewBest) {
            float pulse = 0.7f + 0.3f * MathUtils.sin(elapsed * 5f);
            bodyFont.setColor(new Color(1f, 0.80f, 0.20f, fadeIn * pulse));
            drawCentered(batch, bodyFont, "NEW BEST!", cy - panelH * 0.02f);
        }

        // Restart prompt
        if (elapsed > 0.5f) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(elapsed * 3f);
            bodyFont.setColor(new Color(1f, 0.75f, 0.30f, fadeIn * pulse));
            drawCentered(batch, bodyFont, "SPACE / tap to restart", cy - panelH * 0.22f);
        }

        smallFont.setColor(new Color(0.45f, 0.40f, 0.55f, fadeIn * 0.6f));
        drawCentered(batch, smallFont, "ESC for menu", cy - panelH * 0.32f);
    }

    @Override
    public void exit() {
    }

    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            scoreFont.dispose();
            bodyFont.dispose();
            smallFont.dispose();
            dimPixel.dispose();
            panelTex.dispose();
        }
    }

    private static Texture buildPanelTexture() {
        int w = 64, h = 64;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(new Color(0.12f, 0.08f, 0.22f, 0.9f));
        px.fill();

        // Border
        px.setColor(new Color(0.25f, 0.20f, 0.40f, 0.6f));
        px.drawRectangle(0, 0, w, h);
        px.drawRectangle(1, 1, w - 2, h - 2);

        // Subtle inner highlight
        px.setColor(new Color(0.35f, 0.28f, 0.55f, 0.15f));
        px.fillRectangle(2, 2, w - 4, 3);

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
