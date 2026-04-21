package com.duddlejump.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.managers.Assets;

public class LoadingScreen extends ScreenAdapter {

    private static final float MIN_DURATION = 0.6f;
    private static final float BAR_WIDTH = 320f;
    private static final float BAR_HEIGHT = 14f;
    private static final Color BAR_BG = new Color(0.85f, 0.82f, 0.74f, 1f);
    private static final Color BAR_FILL = new Color(0.96f, 0.44f, 0.32f, 1f);
    private static final Color TEXT_COLOR = new Color(0.30f, 0.30f, 0.36f, 1f);

    private final DuddleJumpGame game;
    private final Assets assets;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private float elapsed;

    public LoadingScreen(DuddleJumpGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.4f);
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        boolean loaded = assets.update();
        float progress = assets.progress();

        ScreenUtils.clear(0.97f, 0.95f, 0.88f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        float barX = (Config.VIEWPORT_WIDTH - BAR_WIDTH) * 0.5f;
        float barY = Config.VIEWPORT_HEIGHT * 0.45f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BAR_BG);
        shapes.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
        shapes.setColor(BAR_FILL);
        shapes.rect(barX, barY, BAR_WIDTH * progress, BAR_HEIGHT);
        shapes.end();

        batch.begin();
        font.setColor(TEXT_COLOR);
        drawCentered("loading…", barY + BAR_HEIGHT + 36f);
        drawCentered(Math.round(progress * 100f) + "%", barY - 24f);
        batch.end();

        if (loaded && elapsed >= MIN_DURATION) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }

    private void drawCentered(String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
