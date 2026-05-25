package com.starbots.starjump.patterns.observer;

import com.badlogic.gdx.utils.Array;

/**
 * Subject of the Observer pattern: a tiny publish/subscribe hub.
 *
 * <p>Decouples producers (the {@code World} firing jumps/deaths, the
 * {@link com.starbots.starjump.ScoreManager} firing score changes) from
 * consumers ({@code SoundController}, {@code AchievementManager}, HUD).</p>
 *
 * <p>Deliberately <b>not</b> a singleton — one instance is owned by
 * {@code StarJumpGame} and injected where needed, so the only global state in
 * the project remains {@code ScoreManager.INSTANCE}.</p>
 */
public final class EventBus {
    private final Array<GameEventListener> listeners = new Array<>();

    public void subscribe(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(GameEventListener listener) {
        listeners.removeValue(listener, true);
    }

    public void publish(GameEvent event) {
        // Snapshot iteration so observers may (un)subscribe during dispatch.
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onGameEvent(event);
        }
    }

    public void publish(GameEventType type, Object payload) {
        publish(new GameEvent(type, payload));
    }
}
