package com.starbots.starjump.audio;

import com.badlogic.gdx.audio.Sound;

import com.starbots.starjump.GameSettings;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;

/**
 * Observer that turns gameplay events into sound. The game logic never touches
 * audio directly — it just publishes events, and this listener maps each one to
 * a synthesized {@link Sound}. All playback is null-safe.
 */
public final class SoundController implements GameEventListener {

    private final Assets assets;
    private final GameSettings settings = GameSettings.INSTANCE;

    public SoundController(Assets assets) {
        this.assets = assets;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.type) {
            case PLAYER_JUMPED:   play(assets.sfxJump, 0.5f);   break;
            case PLATFORM_BROKEN: play(assets.sfxLand, 0.6f);   break;
            case ENEMY_KILLED:    play(assets.sfxHit, 0.7f);    break;
            case BOSS_SPAWNED:    play(assets.sfxBoss, 0.9f);   break;
            case BOSS_HIT:        play(assets.sfxHit, 0.8f);    break;
            case BOSS_DEFEATED:   play(assets.sfxExplosion, 1f); break;
            case NEW_RECORD:      play(assets.sfxPowerup, 0.8f); break;
            case PLAYER_DIED:     play(assets.sfxGameOver, 0.8f); break;
            case SPRING_BOUNCE:   play(assets.sfxSpring, 0.7f);  break;
            case JETPACK_START:   play(assets.sfxJetpack, 0.9f); break;
            case LIFE_GAINED:     play(assets.sfxPowerup, 0.7f); break;
            case LIFE_LOST:       play(assets.sfxHit, 0.9f);     break;
            default: break;
        }
    }

    private void play(Sound sound, float scale) {
        if (sound != null) sound.play(settings.getSfxVolume() * scale);
    }
}
