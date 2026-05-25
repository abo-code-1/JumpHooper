package com.starbots.starjump.patterns.observer;

/** Immutable event carried from a subject to its observers. */
public final class GameEvent {
    public final GameEventType type;
    public final Object payload;

    public GameEvent(GameEventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public GameEvent(GameEventType type) {
        this(type, null);
    }

    /** Convenience: payload as int (0 when absent / non-numeric). */
    public int asInt() {
        return payload instanceof Number ? ((Number) payload).intValue() : 0;
    }

    @Override
    public String toString() {
        return "GameEvent{" + type + (payload == null ? "" : ", " + payload) + '}';
    }
}
