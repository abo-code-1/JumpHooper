package com.starbots.starjump.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import com.starbots.starjump.Config;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.input.KeyboardTiltAdapter;

/**
 * Desktop entry point. Injects the {@link KeyboardTiltAdapter} (Adapter
 * pattern) so the shared game uses arrow / A-D keys for horizontal movement.
 */
public final class Lwjgl3Launcher {

    public static void main(String[] args) {
        // macOS needs the GLFW window on the first JVM thread.
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new StarJumpGame(new KeyboardTiltAdapter()), config());
    }

    private static Lwjgl3ApplicationConfiguration config() {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Star Jump");
        cfg.setWindowedMode((int) Config.WORLD_WIDTH, (int) Config.WORLD_HEIGHT);
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);
        cfg.setResizable(true);
        cfg.setWindowIcon("icon.png");
        return cfg;
    }
}
