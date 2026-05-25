package com.starbots.starjump.patterns.observer;

/** Observer side of the Observer pattern. */
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
