package com.starbots.starjump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.observer.GameEventType;

/**
 * Singleton holding the live run score plus the persisted lifetime stats
 * (record, total distance, total jumps).
 *
 * <p>This is the project's <b>single</b> piece of global state — every other
 * shared object is injected. It mirrors the AsyncStorage logic from the
 * original {@code App.js} ({@code @Game:record}, {@code @Game:distance},
 * {@code @Game:jumps}) using libGDX {@link Preferences}.</p>
 *
 * <p>When score/jumps change it also acts as an Observer-pattern subject by
 * publishing through the injected {@link EventBus}.</p>
 */
public final class ScoreManager {

    /** Eager Singleton instance. */
    public static final ScoreManager INSTANCE = new ScoreManager();

    private static final String PREFS = "starjump";
    private static final String KEY_RECORD = "record";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_JUMPS = "jumps";

    // Live run state.
    private int score;
    private int jumps;

    // Persisted lifetime state.
    private int record;
    private int totalDistance;
    private int totalJumps;

    private boolean newRecord;

    private Preferences prefs;
    private EventBus bus;

    private ScoreManager() {}

    /**
     * Wire up the event bus and load persisted stats. Persistence is optional:
     * when there is no libGDX application (e.g. the headless smoke test), the
     * bus is still wired but loading/saving is skipped.
     */
    public void init(EventBus bus) {
        this.bus = bus;
        if (Gdx.app != null) {
            this.prefs = Gdx.app.getPreferences(PREFS);
            this.record = prefs.getInteger(KEY_RECORD, 0);
            this.totalDistance = prefs.getInteger(KEY_DISTANCE, 0);
            this.totalJumps = prefs.getInteger(KEY_JUMPS, 0);
        }
    }

    // --- live run --------------------------------------------------------------

    /** Begin a fresh run. */
    public void startRun() {
        score = 0;
        jumps = 0;
        newRecord = false;
        publish(GameEventType.SCORE_CHANGED, score);
    }

    /** Add to the run score (the original increments by {@code -speed/5} then rounds). */
    public void addScore(int delta) {
        if (delta == 0) return;
        score += delta;
        publish(GameEventType.SCORE_CHANGED, score);
    }

    public void registerJump() {
        jumps++;
        publish(GameEventType.PLAYER_JUMPED, jumps);
    }

    public int getScore()  { return score; }
    public int getJumps()  { return jumps; }

    // --- end of run / persistence ---------------------------------------------

    /**
     * Mirrors {@code App.endGame}: updates the record if beaten, then folds the
     * run into the lifetime totals and persists everything.
     */
    public void endRun() {
        newRecord = false;
        if (score > record) {
            record = score;
            newRecord = true;
            publish(GameEventType.NEW_RECORD, record);
        }
        totalDistance += score;     // saveDistance(score)
        totalJumps += jumps;        // saveJumps(jumps)
        persist();
        publish(GameEventType.PLAYER_DIED, score);
    }

    private void persist() {
        if (prefs == null) return;
        prefs.putInteger(KEY_RECORD, record);
        prefs.putInteger(KEY_DISTANCE, totalDistance);
        prefs.putInteger(KEY_JUMPS, totalJumps);
        prefs.flush();
    }

    public int getRecord()        { return record; }
    public int getTotalDistance() { return totalDistance; }
    public int getTotalJumps()    { return totalJumps; }
    public boolean wasNewRecord() { return newRecord; }

    private void publish(GameEventType type, Object payload) {
        if (bus != null) bus.publish(type, payload);
    }
}
