package com.gackstone.chase.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.input.IInputController;

/**
 * Handles physics updates, steering velocity integration, lateral clamping,
 * and visual bank angles for the Player.
 */
public class PlayerController {

    private final PlayerEntity player;
    private IInputController inputController;

    public PlayerController(PlayerEntity player, IInputController inputController) {
        this.player = player;
        this.inputController = inputController;
    }

    public void update(float delta) {
        if (!player.getState().isAlive()) {
            return;
        }

        PlayerState state = player.getState();
        Vector3 pos = player.getPosition();
        Vector3 vel = player.getVelocity();

        // 1. Process forward continuous speed & gradual acceleration
        if (state.getForwardSpeed() < GameConfig.PLAYER_MAX_FORWARD_SPEED) {
            float newSpeed = state.getForwardSpeed() + (GameConfig.PLAYER_FORWARD_ACCELERATION * delta);
            state.setForwardSpeed(Math.min(newSpeed, GameConfig.PLAYER_MAX_FORWARD_SPEED));
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

        vel.x = steer * GameConfig.PLAYER_LATERAL_SPEED;
        vel.z = state.getForwardSpeed();

        // 3. Integrate position
        pos.x += vel.x * delta;
        pos.z += vel.z * delta;

        // 4. Clamp to track boundary
        pos.x = MathUtils.clamp(pos.x, -GameConfig.BOUNDARY_LIMIT_X, GameConfig.BOUNDARY_LIMIT_X);

        // 5. Compute and smooth bank roll angle (tilts into turn)
        float targetBank = -steer * GameConfig.PLAYER_BANK_ANGLE_MAX;
        float currentBank = state.getCurrentBankAngle();
        currentBank = MathUtils.lerp(currentBank, targetBank, Math.min(1.0f, delta * GameConfig.PLAYER_BANK_SMOOTHING));
        state.setCurrentBankAngle(currentBank);

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
