package com.duddlejump.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(Config.WINDOW_TITLE);
        config.setWindowedMode(Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setResizable(false);

        new Lwjgl3Application(new DuddleJumpGame(), config);
    }
}
