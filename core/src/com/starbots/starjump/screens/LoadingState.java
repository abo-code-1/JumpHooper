package com.starbots.starjump.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.starbots.starjump.Config;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Painter;

/**
 * Brief splash shown on startup (the original used Expo's {@code AppLoading}),
 * then transitions to the menu. Assets are already loaded by the game, so this
 * is essentially a one-beat State that demonstrates the State flow.
 */
public final class LoadingState implements GameState {

    private final StarJumpGame game;
    private final Painter painter;
    private float elapsed;

    public LoadingState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
    }

    @Override public void enter() { elapsed = 0f; }

    @Override
    public void update(float delta) {
        elapsed += delta;
        if (elapsed > 0.6f) {
            game.gsm().set(new MenuState(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Assets a = game.assets();
        painter.begin();
        painter.fullscreen(a.splashBackground);
        painter.textCentered(a.font(Assets.NASALIZATION, 28), "Star Jump",
                Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT / 2f - 60);
        painter.textCentered(a.font(Assets.DYSLEXIC, 16), "carregando...",
                Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT / 2f);
        painter.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
