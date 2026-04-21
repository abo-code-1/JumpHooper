package com.duddlejump.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.Config;
import com.duddlejump.entities.Platform;
import com.duddlejump.managers.ScoreManager;
import com.duddlejump.screens.GameScreen;
import com.duddlejump.screens.MainMenuScreen;

public final class GameOverState implements GameState {

    private static final Color DIM = new Color(0f, 0f, 0f, 0.55f);
    private static final Color TITLE_COLOR = new Color(0.98f, 0.96f, 0.92f, 1f);
    private static final Color ACCENT = new Color(0.96f, 0.44f, 0.32f, 1f);
    private static final Color SUB_COLOR = new Color(0.85f, 0.82f, 0.74f, 1f);

    private final GameScreen screen;
    private final GlyphLayout layout = new GlyphLayout();

    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private Texture dimPixel;
    private float finalScore;
    private int finalHigh;

    public GameOverState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        screen.queueGameOver();
        finalScore = screen.getScore();
        ScoreManager.INSTANCE.setCurrent((int) finalScore);
        finalHigh = ScoreManager.INSTANCE.getHigh();
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
        boolean restart = Gdx.input.isKeyJustPressed(Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Keys.ENTER)
            || Gdx.input.justTouched();
        if (restart) {
            screen.getGame().setScreen(new GameScreen(screen.getGame()));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            screen.getGame().setScreen(new MainMenuScreen(screen.getGame()));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Array<Platform> platforms = screen.getSpawner().getPlatforms();
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).render(batch);
        }
        screen.getDoodle().render(batch);

        float cy = screen.getCamera().position.y;
        float left = (Config.VIEWPORT_WIDTH - Config.VIEWPORT_WIDTH) * 0.5f;
        batch.setColor(DIM);
        batch.draw(dimPixel, 0, cy - Config.VIEWPORT_HEIGHT * 0.5f,
            Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        batch.setColor(Color.WHITE);

        titleFont.setColor(TITLE_COLOR);
        drawCentered(batch, titleFont, "GAME OVER", cy + Config.VIEWPORT_HEIGHT * 0.20f);

        bodyFont.setColor(ACCENT);
        drawCentered(batch, bodyFont, "score  " + (int) finalScore,
            cy + Config.VIEWPORT_HEIGHT * 0.05f);

        bodyFont.setColor(SUB_COLOR);
        drawCentered(batch, bodyFont, "best  " + finalHigh,
            cy - Config.VIEWPORT_HEIGHT * 0.02f);

        bodyFont.setColor(TITLE_COLOR);
        drawCentered(batch, bodyFont, "SPACE / click to restart",
            cy - Config.VIEWPORT_HEIGHT * 0.18f);

        bodyFont.setColor(SUB_COLOR);
        drawCentered(batch, bodyFont, "ESC for menu",
            cy - Config.VIEWPORT_HEIGHT * 0.26f);
    }

    @Override
    public void exit() {
    }

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
