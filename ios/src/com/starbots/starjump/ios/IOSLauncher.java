package com.starbots.starjump.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.input.AccelerometerTiltAdapter;

/**
 * iOS entry point (RoboVM/MobiVM). Injects the {@link AccelerometerTiltAdapter}
 * (Adapter pattern), so the shared game uses phone tilt for horizontal movement
 * — just like the Android launcher.
 */
public final class IOSLauncher extends IOSApplication.Delegate {

    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationPortrait = true;
        config.orientationLandscape = false;
        return new IOSApplication(new StarJumpGame(new AccelerometerTiltAdapter()), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
