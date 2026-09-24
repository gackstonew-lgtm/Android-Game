package com.gackstone.chase.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.input.IInputController;

/**
 * Handles vehicle arcade physics, acceleration curves, lateral tire grip,
 * drift slip angles, and visual bank roll.
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

        float maxSpeed = car != null ? car.getMaxSpeed() : GameConfig.PLAYER_MAX_FORWARD_SPEED;
        float accelRate = car != null ? car.getAcceleration() * 0.1f : GameConfig.PLAYER_FORWARD_ACCELERATION;
        float handlingRate = car != null ? car.getHandling() : GameConfig.PLAYER_LATERAL_SPEED;

        // 1. Process forward acceleration
        if (state.getForwardSpeed() < maxSpeed) {
            float newSpeed = state.getForwardSpeed() + (accelRate * delta);
            state.setForwardSpeed(Math.min(newSpeed, maxSpeed));
        }

        // 2. Lateral steering from input
        float steer = 0.0f;
        if (inputController != null) {
            inputController.update(delta);
            steer = inputController.getSteerInput();

            // Check for emergency dodge
            if (inputController.isDodgeTriggered()) {
                steer *= 1.8f;
                inputController.resetTriggers();
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
