package com.starbots.starjump.patterns.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * One screen of the game expressed as a State (State pattern).
 *
 * <p>The original {@code App.js} chose what to show with a big {@code render()}
 * switch (loading / menu / game / game-over / achievements / prizes). Here each
 * branch becomes its own concrete state, and {@link GameStateManager} is the
 * context that delegates to the current one.</p>
 */
public interface GameState {
    /** Called once when this state becomes active. */
    void enter();

    /** Poll input + advance simulation. */
    void update(float delta);

    /** Draw the state. Each state owns its own {@code batch.begin()/end()}. */
    void render(SpriteBatch batch);

    /** Viewport changed. */
    void resize(int width, int height);

    /** Called once when this state is replaced. */
    void exit();
}
