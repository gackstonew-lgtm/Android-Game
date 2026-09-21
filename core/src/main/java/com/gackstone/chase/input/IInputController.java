package com.gackstone.chase.input;

/**
 * Platform-independent input abstraction for controlling the chase player.
 * Allows swapping between touch/swipe, on-screen virtual steering, gyroscope,
 * or keyboard/gamepad without modifying gameplay physics.
 */
public interface IInputController {

    /**
     * Normalized lateral steer input from -1.0 (full left) to +1.0 (full right).
     */
    float getSteerInput();

    /**
     * Normalized throttle/brake input from -1.0 (brake) to +1.0 (boost).
     */
    float getThrottleInput();

    /**
     * Indicates whether the player triggered an emergency dodge / drift action.
     */
    boolean isDodgeTriggered();

    /**
     * Reset frame-specific triggers.
     */
    void resetTriggers();

    /**
     * Update internal smoothing, drag vectors, or touch tracking.
     */
    void update(float delta);
}
