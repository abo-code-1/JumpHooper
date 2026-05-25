package com.starbots.starjump.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.input.AccelerometerTiltAdapter;

/**
 * Android entry point. Injects the {@link AccelerometerTiltAdapter} (Adapter
 * pattern) so the shared game reads phone tilt for horizontal movement — the
 * closest match to the original Expo build.
 */
public final class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = true;
        config.useCompass = false;
        config.useImmersiveMode = true;
        initialize(new StarJumpGame(new AccelerometerTiltAdapter()), config);
    }
}
