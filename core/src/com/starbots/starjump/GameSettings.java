package com.starbots.starjump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/**
 * Persisted player-facing settings: effects volume, music volume and phone
 * tilt tuning. Kept separate from {@link ScoreManager} because these are local
 * preferences, not run/progression state.
 */
public final class GameSettings {

    public static final GameSettings INSTANCE = new GameSettings();

    private static final String PREFS = "starjump-settings";
    private static final String KEY_SFX = "sfxVolume";
    private static final String KEY_MUSIC = "musicVolume";
    private static final String KEY_TILT = "tiltSensitivity";
    private static final String KEY_TILT_INVERTED = "tiltInverted";
    private static final String KEY_TILT_AXIS = "tiltAxis";
    private static final String KEY_TILT_CENTER = "tiltCenter";
    private static final String KEY_CONTROL_MODE = "controlMode";
    private static final String KEY_VERSION = "settingsVersion";

    public static final float MIN_VOLUME = 0f;
    public static final float MAX_VOLUME = 1f;
    public static final float MIN_TILT = 0.45f;
    public static final float MAX_TILT = 2.2f;
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;
    public static final int AXIS_ROLL = 3;
    public static final int AXIS_PITCH = 4;
    public static final int CONTROL_TOUCH = 0;
    public static final int CONTROL_TILT = 1;
    public static final int CONTROL_HYBRID = 2;
    private static final int CURRENT_VERSION = 4;

    private float sfxVolume = 0.65f;
    private float musicVolume = 0.42f;
    private float tiltSensitivity = 1.0f;
    private boolean tiltInverted;
    // Accelerometer X is the reliable left/right tilt axis on iOS (roll/pitch
    // depend on the compass and read 0 if it isn't running); default to it.
    private int tiltAxis = AXIS_X;
    private float tiltCenter;
    // Tilt by default so motion steering works out of the box; Touch/Hybrid
    // remain selectable in the settings screen.
    private int controlMode = CONTROL_TILT;

    private Preferences prefs;

    private GameSettings() {}

    public void init() {
        if (Gdx.app == null) return;
        prefs = Gdx.app.getPreferences(PREFS);
        int version = prefs.getInteger(KEY_VERSION, 0);
        sfxVolume = prefs.getFloat(KEY_SFX, sfxVolume);
        musicVolume = prefs.getFloat(KEY_MUSIC, musicVolume);
        tiltSensitivity = prefs.getFloat(KEY_TILT, tiltSensitivity);
        tiltInverted = prefs.getBoolean(KEY_TILT_INVERTED, tiltInverted);
        tiltAxis = prefs.getInteger(KEY_TILT_AXIS, tiltAxis);
        tiltCenter = prefs.getFloat(KEY_TILT_CENTER, tiltCenter);
        controlMode = prefs.getInteger(KEY_CONTROL_MODE, controlMode);
        if (version < CURRENT_VERSION) {
            tiltAxis = AXIS_X;
            tiltCenter = 0f;
            tiltInverted = false;
            controlMode = CONTROL_TILT;
            persist();
        }
        clampAll();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float value) {
        sfxVolume = MathUtils.clamp(value, MIN_VOLUME, MAX_VOLUME);
        persist();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float value) {
        musicVolume = MathUtils.clamp(value, MIN_VOLUME, MAX_VOLUME);
        persist();
    }

    public float getTiltSensitivity() {
        return tiltSensitivity;
    }

    public void setTiltSensitivity(float value) {
        tiltSensitivity = MathUtils.clamp(value, MIN_TILT, MAX_TILT);
        persist();
    }

    public boolean isTiltInverted() {
        return tiltInverted;
    }

    public void setTiltInverted(boolean value) {
        tiltInverted = value;
        persist();
    }

    public void toggleTiltInverted() {
        setTiltInverted(!tiltInverted);
    }

    public int getTiltAxis() {
        return tiltAxis;
    }

    public void setTiltAxis(int axis) {
        tiltAxis = MathUtils.clamp(axis, AXIS_X, AXIS_PITCH);
        persist();
    }

    public void nextTiltAxis() {
        setTiltAxis((tiltAxis + 1) % 5);
    }

    public String getTiltAxisLabel() {
        switch (tiltAxis) {
            case AXIS_Y: return "Y";
            case AXIS_Z: return "Z";
            case AXIS_ROLL: return "Roll";
            case AXIS_PITCH: return "Pitch";
            default: return "X";
        }
    }

    public float getTiltCenter() {
        return tiltCenter;
    }

    public void setTiltCenter(float value) {
        tiltCenter = MathUtils.clamp(value, -1f, 1f);
        persist();
    }

    public void resetTiltCenter() {
        setTiltCenter(0f);
    }

    public int getControlMode() {
        return controlMode;
    }

    public void setControlMode(int mode) {
        controlMode = MathUtils.clamp(mode, CONTROL_TOUCH, CONTROL_HYBRID);
        persist();
    }

    public void nextControlMode() {
        setControlMode((controlMode + 1) % 3);
    }

    public String getControlModeLabel() {
        switch (controlMode) {
            case CONTROL_TILT: return "Tilt";
            case CONTROL_HYBRID: return "Hibrido";
            default: return "Toque";
        }
    }

    private void clampAll() {
        sfxVolume = MathUtils.clamp(sfxVolume, MIN_VOLUME, MAX_VOLUME);
        musicVolume = MathUtils.clamp(musicVolume, MIN_VOLUME, MAX_VOLUME);
        tiltSensitivity = MathUtils.clamp(tiltSensitivity, MIN_TILT, MAX_TILT);
        tiltAxis = MathUtils.clamp(tiltAxis, AXIS_X, AXIS_PITCH);
        tiltCenter = MathUtils.clamp(tiltCenter, -1f, 1f);
        controlMode = MathUtils.clamp(controlMode, CONTROL_TOUCH, CONTROL_HYBRID);
    }

    private void persist() {
        if (prefs == null) return;
        prefs.putFloat(KEY_SFX, sfxVolume);
        prefs.putFloat(KEY_MUSIC, musicVolume);
        prefs.putFloat(KEY_TILT, tiltSensitivity);
        prefs.putBoolean(KEY_TILT_INVERTED, tiltInverted);
        prefs.putInteger(KEY_TILT_AXIS, tiltAxis);
        prefs.putFloat(KEY_TILT_CENTER, tiltCenter);
        prefs.putInteger(KEY_CONTROL_MODE, controlMode);
        prefs.putInteger(KEY_VERSION, CURRENT_VERSION);
        prefs.flush();
    }
}
