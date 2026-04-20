package com.duddlejump;

public final class Config {
    public static final String WINDOW_TITLE = "DuddleJump";
    public static final int VIEWPORT_WIDTH = 480;
    public static final int VIEWPORT_HEIGHT = 800;
    public static final float WORLD_WIDTH = VIEWPORT_WIDTH;
    public static final float WORLD_HEIGHT = VIEWPORT_HEIGHT;

    public static final float GRAVITY = -2000f;
    public static final float HORIZONTAL_ACCEL = 2400f;
    public static final float MAX_HORIZONTAL_SPEED = 420f;
    public static final float HORIZONTAL_DRAG = 0.90f;
    public static final float JUMP_VELOCITY = 900f;
    public static final float JETPACK_VELOCITY = 1800f;
    public static final float SPRING_VELOCITY = 1500f;

    public static final float CAMERA_FOLLOW_THRESHOLD = 0.5f;

    public static final String PREFS_NAME = "duddlejump";

    private Config() {
    }
}
