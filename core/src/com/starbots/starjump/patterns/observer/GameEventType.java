package com.starbots.starjump.patterns.observer;

/** Kinds of in-game events broadcast through the {@link EventBus}. */
public enum GameEventType {
    /** Player bounced off a platform. Payload: the new jump count (Integer). */
    PLAYER_JUMPED,
    /** Run score changed. Payload: current run score (Integer). */
    SCORE_CHANGED,
    /** Player died (fell off the bottom or touched lava). Payload: final score (Integer). */
    PLAYER_DIED,
    /** A new high score was set during the last run. Payload: the record (Integer). */
    NEW_RECORD,
    /** A curiosity/achievement milestone was reached. Payload: milestone value (Integer). */
    ACHIEVEMENT_UNLOCKED
}
