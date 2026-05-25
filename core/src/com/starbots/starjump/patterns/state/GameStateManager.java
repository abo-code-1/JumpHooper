package com.starbots.starjump.patterns.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Context of the State pattern. Holds the active {@link GameState} and forwards
 * the game loop to it, handling the enter/exit transition bookkeeping.
 */
public final class GameStateManager {
    private GameState current;
    private GameState pending;

    /** Queue a transition; applied at the start of the next {@link #update}. */
    public void set(GameState next) {
        this.pending = next;
    }

    public GameState current() {
        return current;
    }

    public void update(float delta) {
        if (pending != null) {
            if (current != null) current.exit();
            current = pending;
            pending = null;
            current.enter();
        }
        if (current != null) current.update(delta);
    }

    public void render(SpriteBatch batch) {
        if (current != null) current.render(batch);
    }

    public void resize(int width, int height) {
        if (current != null) current.resize(width, height);
    }
}
