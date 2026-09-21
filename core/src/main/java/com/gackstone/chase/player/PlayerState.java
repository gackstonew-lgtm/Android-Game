package com.gackstone.chase.player;

import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates the dynamic physical state of the Player.
 */
public class PlayerState {

    private float health = GameConfig.PLAYER_INITIAL_HEALTH;
    private float maxHealth = GameConfig.PLAYER_INITIAL_HEALTH;
    private float forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
    private float currentBankAngle = 0.0f;
    private float distanceTraveled = 0.0f;
    private boolean isAlive = true;
    private boolean isInvulnerable = false;
    private float invulnerabilityTimer = 0.0f;

    public void reset() {
        health = GameConfig.PLAYER_INITIAL_HEALTH;
        maxHealth = GameConfig.PLAYER_INITIAL_HEALTH;
        forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
        currentBankAngle = 0.0f;
        distanceTraveled = 0.0f;
        isAlive = true;
        isInvulnerable = false;
        invulnerabilityTimer = 0.0f;
    }

    public void update(float delta) {
        if (isInvulnerable) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer <= 0.0f) {
                isInvulnerable = false;
            }
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
}
