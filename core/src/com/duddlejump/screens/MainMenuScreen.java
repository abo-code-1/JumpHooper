package com.duddlejump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.managers.ScoreManager;

public class MainMenuScreen extends ScreenAdapter {

    private static final Color TITLE_COLOR = new Color(0.12f, 0.12f, 0.18f, 1f);
    private static final Color SUBTITLE_COLOR = new Color(0.30f, 0.30f, 0.36f, 1f);
    private static final Color ACCENT = new Color(0.96f, 0.44f, 0.32f, 1f);

    private final DuddleJumpGame game;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final GlyphLayout layout = new GlyphLayout();

    public MainMenuScreen(DuddleJumpGame game) {
        this.game = game;
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
        this.batch = new SpriteBatch();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3f);
        this.bodyFont = new BitmapFont();
        this.bodyFont.getData().setScale(1.4f);
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
            return;
        }

        ScreenUtils.clear(0.97f, 0.95f, 0.88f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        titleFont.setColor(TITLE_COLOR);
        drawCentered(titleFont, "JUMPHOOPER", Config.VIEWPORT_HEIGHT * 0.72f);

        bodyFont.setColor(SUBTITLE_COLOR);
        drawCentered(bodyFont, "doodle jump, but up to the sun", Config.VIEWPORT_HEIGHT * 0.62f);

        bodyFont.setColor(ACCENT);
        drawCentered(bodyFont, "press SPACE or click to play", Config.VIEWPORT_HEIGHT * 0.38f);

        bodyFont.setColor(SUBTITLE_COLOR);
        drawCentered(bodyFont, "high score: " + ScoreManager.INSTANCE.getHigh(), Config.VIEWPORT_HEIGHT * 0.28f);

        bodyFont.setColor(new Color(0.5f, 0.5f, 0.55f, 1f));
        drawCentered(bodyFont, "A/D or arrows · P to pause", Config.VIEWPORT_HEIGHT * 0.18f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
    }

    private void drawCentered(BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
