package com.starbots.starjump.audio;

import com.badlogic.gdx.audio.Sound;

import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;
import com.starbots.starjump.patterns.observer.GameEventType;

/**
 * Observer that plays the "swoosh" jump sound. The original shipped
 * {@code swoosh.mp3} and wired it to {@code jump()} but left the call
 * commented out; here it is enabled and driven purely by the event bus, so the
 * game logic never references audio directly.
 */
public final class SoundController implements GameEventListener {

    private final Sound jumpSound;
    private float volume = 0.6f;

    public SoundController(Sound jumpSound) {
        this.jumpSound = jumpSound;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event.type == GameEventType.PLAYER_JUMPED && jumpSound != null) {
            jumpSound.play(volume);
        }
    }
}
