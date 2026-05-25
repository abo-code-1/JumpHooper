package com.starbots.starjump.model.platform;

/**
 * Visual/behavioural variants of a platform. Maps to the original's random
 * texture pick plus the {@code effects} field ({@code null} / 'moving' / 'lava').
 */
public enum PlatformKind {
    GRASS,
    GRASS_BROKEN,
    STONE,
    STONE_BROKEN,
    LAVA
}
