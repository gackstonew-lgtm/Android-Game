package com.gackstone.chase.player;

import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates dynamic physical state, stats, damage and distance of the player.
 */
public class PlayerState {

    private CarDefinition carDefinition;
    private float health = GameConfig.PLAYER_INITIAL_HEALTH;
    private float maxHealth = GameConfig.PLAYER_INITIAL_HEALTH;
    private float forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
    private float currentBankAngle = 0.0f;
    private float currentSteerAngle = 0.0f;
    private float distanceTraveled = 0.0f;
    private boolean isAlive = true;
    private boolean isInvulnerable = false;
    private float invulnerabilityTimer = 0.0f;
    private float nitroAmount = 100.0f;
    private boolean isBoosting = false;

    public void applyCarDefinition(CarDefinition car) {
        this.carDefinition = car;
        this.maxHealth = car.getMaxHealth();
        this.health = this.maxHealth;
        this.forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
    }

    public void reset() {
        if (carDefinition != null) {
            maxHealth = carDefinition.getMaxHealth();
        } else {
            maxHealth = GameConfig.PLAYER_INITIAL_HEALTH;
        }
        health = maxHealth;
        forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
        currentBankAngle = 0.0f;
        currentSteerAngle = 0.0f;
        distanceTraveled = 0.0f;
        isAlive = true;
        isInvulnerable = false;
        invulnerabilityTimer = 0.0f;
        nitroAmount = 100.0f;
        isBoosting = false;
    }

    public void update(float delta) {
        if (isInvulnerable) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer <= 0.0f) {
                isInvulnerable = false;
            }
        }

        // Regenerate nitro gradually
        if (!isBoosting && nitroAmount < 100.0f) {
            nitroAmount = Math.min(100.0f, nitroAmount + 8.0f * delta);
        }
    }

    public void takeDamage(float amount) {
        if (isInvulnerable || !isAlive) return;

        health -= amount;
        if (health <= 0.0f) {
            health = 0.0f;
            isAlive = false;
        } else {
            isInvulnerable = true;
            invulnerabilityTimer = 0.8f;
        }
    }

    public CarDefinition getCarDefinition() {
        return carDefinition;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getForwardSpeed() {
        return forwardSpeed;
    }

    public void setForwardSpeed(float forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public float getCurrentBankAngle() {
        return currentBankAngle;
    }

    public void setCurrentBankAngle(float currentBankAngle) {
        this.currentBankAngle = currentBankAngle;
    }

    public float getCurrentSteerAngle() {
        return currentSteerAngle;
    }

    public void setCurrentSteerAngle(float currentSteerAngle) {
        this.currentSteerAngle = currentSteerAngle;
    }

    public float getDistanceTraveled() {
        return distanceTraveled;
    }

    public void addDistance(float deltaDistance) {
        this.distanceTraveled += deltaDistance;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    public float getNitroAmount() {
        return nitroAmount;
    }

    public void setNitroAmount(float nitroAmount) {
        this.nitroAmount = nitroAmount;
    }

    public boolean isBoosting() {
        return isBoosting;
    }

    public void setBoosting(boolean boosting) {
        isBoosting = boosting;
    }
}
