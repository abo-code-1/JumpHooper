package com.duddlejump.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.entities.Platform;
import com.duddlejump.managers.ScoreManager;
import com.duddlejump.screens.GameScreen;

public final class PlayingState implements GameState {

    private final GameScreen screen;

    public PlayingState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        ScoreManager.INSTANCE.reset();
    }

    @Override
    public void update(float dt) {
        screen.updateWorld(dt);

        if (screen.getInput().isPauseRequested()) {
            screen.setState(screen.getPausedState());
            return;
        }
        if (screen.isDoodleBelowCamera()) {
            screen.setState(screen.getGameOverState());
        }
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
