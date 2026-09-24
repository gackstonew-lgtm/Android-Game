package com.gackstone.chase.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * High-performance, mobile-optimized touch gesture controller supporting:
 * - Drag steering (smooth lateral position mapping)
 * - Swipe gestures (rapid lane change / evasive maneuver)
 * - Dual-zone touch (left/right screen sides)
 * - Desktop keyboard arrow / WASD fallback during development
 */
public class TouchGestureController extends InputAdapter implements IInputController {

    private float steerInput = 0.0f;
    private float targetSteer = 0.0f;
    private float throttleInput = 0.0f;
    private boolean dodgeTriggered = false;

    // Touch tracking
    private boolean isTouching = false;
    private final Vector2 touchStart = new Vector2();
    private final Vector2 touchCurrent = new Vector2();
    private static final float SWIPE_THRESHOLD_X = 50.0f;
    private static final float STEER_SENSITIVITY = 0.0035f;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            isTouching = true;
            touchStart.set(screenX, screenY);
            touchCurrent.set(screenX, screenY);
            
            // If tapped directly on left/right half, apply direct bias
            float screenHalf = Gdx.graphics.getWidth() * 0.5f;
            if (screenX < screenHalf * 0.7f) {
                targetSteer = -0.75f;
            } else if (screenX > screenHalf * 1.3f) {
                targetSteer = 0.75f;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0 && isTouching) {
            touchCurrent.set(screenX, screenY);
            float deltaX = touchCurrent.x - touchStart.x;
            
            // Continuous drag steering
            targetSteer = MathUtils.clamp(deltaX * STEER_SENSITIVITY, -1.0f, 1.0f);
            
            // Rapid swipe detection
            if (Math.abs(deltaX) > SWIPE_THRESHOLD_X * 2.0f) {
                dodgeTriggered = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            isTouching = false;
            targetSteer = 0.0f;
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta) {
        // Keyboard controls
        float keyboardSteer = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            keyboardSteer -= 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            keyboardSteer += 1.0f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            dodgeTriggered = true;
        }

        // Keyboard throttle / Nitro boost
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W) ||
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            throttleInput = 1.0f;
        } else if (!isTouching) {
            throttleInput = 0.0f;
        }

        if (keyboardSteer != 0.0f) {
            targetSteer = keyboardSteer;
        } else if (!isTouching) {
            targetSteer = 0.0f;
        }

        // Smooth interpolation of steering input to avoid jitter
        steerInput = MathUtils.lerp(steerInput, targetSteer, Math.min(1.0f, delta * 15.0f));
    }

    @Override
    public float getSteerInput() {
        return steerInput;
    }

    @Override
    public float getThrottleInput() {
        return throttleInput;
    }

    public void setThrottleInput(float throttleInput) {
        this.throttleInput = throttleInput;
    }

    @Override
    public boolean isDodgeTriggered() {
        return dodgeTriggered;
    }

    public void triggerDodge() {
        this.dodgeTriggered = true;
    }

    @Override
    public void resetTriggers() {
        dodgeTriggered = false;
    }
}
