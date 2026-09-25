package com.gackstone.chase.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.input.IInputController;

/**
 * Handles vehicle arcade physics, acceleration curves, lateral tire grip,
 * drift slip angles, and visual bank roll with vehicle upgrade boosts.
 */
public class PlayerController {

    private final PlayerEntity player;
    private IInputController inputController;

    private float currentLateralVelocity = 0.0f;

    public PlayerController(PlayerEntity player, IInputController inputController) {
        this.player = player;
        this.inputController = inputController;
    }

    public void update(float delta) {
        if (!player.getState().isAlive()) {
            return;
        }

        PlayerState state = player.getState();
        CarDefinition car = player.getCurrentCar();
        Vector3 pos = player.getPosition();
        Vector3 vel = player.getVelocity();

        float baseMaxSpeed = car != null ? car.getMaxSpeed() : GameConfig.PLAYER_MAX_FORWARD_SPEED;
        float baseAccelRate = car != null ? car.getAcceleration() * 0.1f : GameConfig.PLAYER_FORWARD_ACCELERATION;
        float baseHandlingRate = car != null ? car.getHandling() : GameConfig.PLAYER_LATERAL_SPEED;

        // Apply Upgrade Multipliers
        float maxSpeed = baseMaxSpeed * (1.0f + state.getEngineTier() * GameConfig.UPGRADE_SPEED_BOOST_PER_TIER);
        float accelRate = baseAccelRate * (1.0f + state.getEngineTier() * GameConfig.UPGRADE_ACCEL_BOOST_PER_TIER);
        float handlingRate = baseHandlingRate * (1.0f + state.getHandlingTier() * GameConfig.UPGRADE_HANDLING_BOOST_PER_TIER);

        // 1. Process forward acceleration & Nitro Boost
        boolean wantsBoost = false;
        float steer = 0.0f;
        if (inputController != null) {
            inputController.update(delta);
            steer = inputController.getSteerInput();
            wantsBoost = inputController.getThrottleInput() > 0.1f;

            // Check for emergency dodge
            if (inputController.isDodgeTriggered()) {
                steer *= 2.0f;
                inputController.resetTriggers();
            }
        }

        if (wantsBoost && state.getNitroAmount() > 0.0f) {
            state.setBoosting(true);
            state.setNitroAmount(Math.max(0.0f, state.getNitroAmount() - delta * 35.0f));
            float boostMaxSpeed = maxSpeed * 1.25f;
            float boostAccelRate = accelRate * 2.5f;
            if (state.getForwardSpeed() < boostMaxSpeed) {
                float newSpeed = state.getForwardSpeed() + (boostAccelRate * delta);
                state.setForwardSpeed(Math.min(newSpeed, boostMaxSpeed));
            }
        } else {
            state.setBoosting(false);
            if (state.getForwardSpeed() < maxSpeed) {
                float newSpeed = state.getForwardSpeed() + (accelRate * delta);
                state.setForwardSpeed(Math.min(newSpeed, maxSpeed));
            } else if (state.getForwardSpeed() > maxSpeed) {
                float decayedSpeed = state.getForwardSpeed() - (accelRate * 1.8f * delta);
                state.setForwardSpeed(Math.max(decayedSpeed, maxSpeed));
            }
        }

        // Apply handling and lateral tire inertia
        float targetLateralVel = steer * handlingRate;
        currentLateralVelocity = MathUtils.lerp(currentLateralVelocity, targetLateralVel, Math.min(1.0f, delta * 14.0f));

        vel.x = currentLateralVelocity;
        vel.z = state.getForwardSpeed();

        // 3. Integrate position
        pos.x += vel.x * delta;
        pos.z += vel.z * delta;

        // 4. Clamp to road boundaries
        pos.x = MathUtils.clamp(pos.x, -GameConfig.BOUNDARY_LIMIT_X, GameConfig.BOUNDARY_LIMIT_X);

        // 5. Compute bank roll & steering angle
        float targetBank = -steer * GameConfig.PLAYER_BANK_ANGLE_MAX;
        float currentBank = state.getCurrentBankAngle();
        currentBank = MathUtils.lerp(currentBank, targetBank, Math.min(1.0f, delta * GameConfig.PLAYER_BANK_SMOOTHING));
        state.setCurrentBankAngle(currentBank);
        state.setCurrentSteerAngle(steer);

        // 6. Accumulate distance traveled
        state.addDistance(vel.z * delta);

        // 7. Update player entity internals
        player.update(delta);
    }

    public void setInputController(IInputController inputController) {
        this.inputController = inputController;
    }

    public IInputController getInputController() {
        return inputController;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
