package com.duddlejump.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.duddlejump.Config;

public enum ScoreManager {
    INSTANCE;

    private static final String KEY_HIGH = "highScore";

    private int current = 0;
    private int high = 0;
    private boolean highLoaded = false;

    public void reset() {
        current = 0;
    }

    public int current() {
        return current;
    }

    public int getHigh() {
        ensureHighLoaded();
        return high;
    }

    public void add(int n) {
        if (n <= 0) {
            return;
        }
        current += n;
        ensureHighLoaded();
        if (current > high) {
            high = current;
            persistHigh();
        }
    }

    public void setCurrent(int value) {
        current = Math.max(0, value);
        ensureHighLoaded();
        if (current > high) {
            high = current;
            persistHigh();
        }
    }

    public void resetForTests() {
        current = 0;
        high = 0;
        highLoaded = false;
    }

    private void ensureHighLoaded() {
        if (highLoaded) {
            return;
        }
        Preferences prefs = prefs();
        if (prefs != null) {
            high = prefs.getInteger(KEY_HIGH, 0);
        }
        highLoaded = true;
    }

    private void persistHigh() {
        Preferences prefs = prefs();
        if (prefs != null) {
            prefs.putInteger(KEY_HIGH, high).flush();
        }
    }

    private Preferences prefs() {
        if (Gdx.app == null) {
            return null;
        }
        return Gdx.app.getPreferences(Config.PREFS_NAME);
    }
}
