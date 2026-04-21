package com.duddlejump.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.entities.Platform;
import com.duddlejump.screens.GameScreen;

public final class GameOverState implements GameState {

    private final GameScreen screen;

    public GameOverState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        screen.queueGameOver();
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void render(SpriteBatch batch) {
        Array<Platform> platforms = screen.getSpawner().getPlatforms();
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).render(batch);
        }
        screen.getDoodle().render(batch);
    }

    @Override
    public void exit() {
    }
}
