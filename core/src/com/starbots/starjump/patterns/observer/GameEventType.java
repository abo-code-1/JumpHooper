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
    ACHIEVEMENT_UNLOCKED,

    /** A cracked platform shattered after a bounce. Payload: world position (Vector2). */
    PLATFORM_BROKEN,

    /** An enemy was stomped/destroyed. Payload: world position (Vector2). */
    ENEMY_KILLED,
    /** A boss entered the arena. Payload: world position (Vector2). */
    BOSS_SPAWNED,
    /** The boss took a hit. Payload: world position (Vector2). */
    BOSS_HIT,
    /** The boss was defeated. Payload: world position (Vector2). */
    BOSS_DEFEATED,

    /** Player took a hit but survived (lost a life). Payload: lives remaining (Integer). */
    LIFE_LOST,
    /** Player gained a life from a heart. Payload: lives now (Integer). */
    LIFE_GAINED,
    /** Player launched off a spring. Payload: world position (Vector2). */
    SPRING_BOUNCE,
    /** Player picked up a jetpack. Payload: world position (Vector2). */
    JETPACK_START
}
