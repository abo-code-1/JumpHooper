package com.starbots.starjump.input;

/**
 * Target interface (Adapter pattern) for horizontal control.
 *
 * <p>The game logic only knows about {@link #getTilt()}, whose semantics match
 * the original's {@code accelerometerData.x}: the world applies
 * {@code player.x += getTilt() * Config.MOVE_FACTOR} (and {@code MOVE_FACTOR}
 * is negative, exactly as in {@code systems.js}). Each platform supplies its
 * own adaptee:</p>
 * <ul>
 *   <li>desktop  -> {@link KeyboardTiltAdapter} (adapts libGDX keyboard input)</li>
 *   <li>android  -> {@link AccelerometerTiltAdapter} (adapts the device sensor)</li>
 * </ul>
 */
public interface TiltControl {
    /**
     * @return horizontal control value with accelerometer-x semantics:
     *         positive ~ "tilt makes the astronaut go left" (because the world
     *         multiplies by the negative {@code MOVE_FACTOR}); 0 = no input.
     */
    float getTilt();
}
