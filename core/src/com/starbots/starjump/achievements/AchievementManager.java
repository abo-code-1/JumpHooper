package com.starbots.starjump.achievements;

import com.badlogic.gdx.Gdx;

import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;
import com.starbots.starjump.patterns.observer.GameEventType;

/**
 * Observer that watches for the end of a run and detects newly unlocked
 * curiosities (the "Conquistas" of the original). When a milestone is crossed
 * it re-broadcasts an {@link GameEventType#ACHIEVEMENT_UNLOCKED} event.
 */
public final class AchievementManager implements GameEventListener {

    private final EventBus bus;
    private final ScoreManager scores = ScoreManager.INSTANCE;

    private int distanceUnlocked;
    private int jumpsUnlocked;

    public AchievementManager(EventBus bus) {
        this.bus = bus;
        this.distanceUnlocked = countUnlocked(scores.getTotalDistance());
        this.jumpsUnlocked = countUnlocked(scores.getTotalJumps());
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event.type != GameEventType.PLAYER_DIED) return;

        int newDistance = countUnlocked(scores.getTotalDistance());
        for (int i = distanceUnlocked; i < newDistance; i++) {
            announce("distância", Curiosities.MILESTONES[i]);
        }
        distanceUnlocked = newDistance;

        int newJumps = countUnlocked(scores.getTotalJumps());
        for (int i = jumpsUnlocked; i < newJumps; i++) {
            announce("pulos", Curiosities.MILESTONES[i]);
        }
        jumpsUnlocked = newJumps;
    }

    private void announce(String type, int milestone) {
        Gdx.app.log("Achievement", "Curiosidade desbloqueada em " + type + ": " + milestone);
        bus.publish(GameEventType.ACHIEVEMENT_UNLOCKED, milestone);
    }

    /** How many milestones a given lifetime total has reached. */
    public static int countUnlocked(int total) {
        int count = 0;
        for (int milestone : Curiosities.MILESTONES) {
            if (total >= milestone) count++;
        }
        return count;
    }
}
