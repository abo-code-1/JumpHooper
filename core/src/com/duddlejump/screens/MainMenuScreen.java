package com.duddlejump.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;

public class MainMenuScreen extends ScreenAdapter {
    private final DuddleJumpGame game;
    private final Viewport viewport;

    public MainMenuScreen(DuddleJumpGame game) {
        this.game = game;
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.97f, 0.95f, 0.88f, 1.0f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
