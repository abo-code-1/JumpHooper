package com.duddlejump.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.entities.Doodle;

public class GameScreen extends ScreenAdapter {

    private final DuddleJumpGame game;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final Doodle doodle;

    public GameScreen(DuddleJumpGame game) {
        this.game = game;
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
        this.batch = new SpriteBatch();

        Texture sprite = createPlaceholderSprite();
        float startX = (Config.VIEWPORT_WIDTH - Doodle.WIDTH) * 0.5f;
        float startY = Config.VIEWPORT_HEIGHT * 0.25f;
        this.doodle = new Doodle(sprite, startX, startY, true);
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.97f, 0.95f, 0.88f, 1.0f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        doodle.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        doodle.dispose();
        batch.dispose();
    }

    private static Texture createPlaceholderSprite() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.36f, 0.72f, 0.36f, 1f));
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 2);
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle((int) (size * 0.35f), (int) (size * 0.4f), 4);
        pixmap.fillCircle((int) (size * 0.65f), (int) (size * 0.4f), 4);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
