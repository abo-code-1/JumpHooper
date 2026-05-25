package com.starbots.starjump.model;

/**
 * A free-floating collectible (heart or jetpack) that scrolls with the world
 * and is grabbed on overlap. Position is the top-left corner in y-down space.
 */
public final class Pickup {

    public enum Type { HEART, JETPACK }

    public final Type type;
    public float x, y;
    public final float width;
    public final float height;
    public float bobPhase;   // gentle floating animation

    public Pickup(Type type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = type == Type.JETPACK ? 30f : 34f;
        this.height = type == Type.JETPACK ? 38f : 30f;
    }

    public float centerX() { return x + width / 2f; }
    public float centerY() { return y + height / 2f; }
    public float right()   { return x + width; }
    public float bottom()  { return y + height; }
}
