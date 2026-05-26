package com.starbots.starjump.audio;

import com.badlogic.gdx.audio.Music;

import com.starbots.starjump.GameSettings;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;

/** Controls the looped background music and switches to the boss theme. */
public final class MusicController implements GameEventListener {

    private final Assets assets;
    private final GameSettings settings = GameSettings.INSTANCE;

    private Music current;

    public MusicController(Assets assets) {
        this.assets = assets;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.type) {
            case RUN_STARTED:
                play(assets.musicRegular);
                break;
            case BOSS_SPAWNED:
                play(assets.musicBoss);
                break;
            case BOSS_DEFEATED:
                play(assets.musicRegular);
                break;
            case PLAYER_DIED:
                stop();
                break;
            default:
                break;
        }
    }

    public void update() {
        if (current != null) current.setVolume(settings.getMusicVolume());
    }

    public void stop() {
        if (current != null) {
            current.stop();
            current = null;
        }
    }

    private void play(Music next) {
        if (next == null) return;
        if (current == next) {
            current.setVolume(settings.getMusicVolume());
            if (!current.isPlaying()) current.play();
            return;
        }
        if (current != null) current.stop();
        current = next;
        current.setLooping(true);
        current.setVolume(settings.getMusicVolume());
        current.play();
    }
}
