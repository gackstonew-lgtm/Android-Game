package com.gackstone.chase.player;

import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates dynamic physical state, stats, damage, nitro adrenaline, and upgrades of the player.
 */
public class PlayerState {

    private CarDefinition carDefinition;
    private int engineTier = 0;
    private int handlingTier = 0;
    private int armourTier = 0;
    private int nitroTier = 0;

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
        applyCarDefinitionWithUpgrades(car, 0, 0, 0, 0);
    }

    public void applyCarDefinitionWithUpgrades(CarDefinition car, int engineTier, int handlingTier, int armourTier, int nitroTier) {
        this.carDefinition = car;
        this.engineTier = engineTier;
        this.handlingTier = handlingTier;
        this.armourTier = armourTier;
        this.nitroTier = nitroTier;

        float baseHp = (car != null) ? car.getMaxHealth() : GameConfig.PLAYER_INITIAL_HEALTH;
        float bonusHp = baseHp * (armourTier * GameConfig.UPGRADE_ARMOUR_BOOST_PER_TIER);
        this.maxHealth = baseHp + bonusHp;
        this.health = this.maxHealth;
        this.forwardSpeed = GameConfig.PLAYER_INITIAL_FORWARD_SPEED;
        this.nitroAmount = 100.0f;
    }

    public void reset() {
        if (carDefinition != null) {
            float baseHp = carDefinition.getMaxHealth();
            float bonusHp = baseHp * (armourTier * GameConfig.UPGRADE_ARMOUR_BOOST_PER_TIER);
            maxHealth = baseHp + bonusHp;
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

        // Regenerate nitro gradually (faster with higher nitroTier)
        if (!isBoosting && nitroAmount < 100.0f) {
            float regenRate = 8.0f * (1.0f + nitroTier * GameConfig.UPGRADE_NITRO_BOOST_PER_TIER);
            nitroAmount = Math.min(100.0f, nitroAmount + regenRate * delta);
        }
    }

    public void applyNearMissReward() {
        // Immediate adrenaline surge
        this.nitroAmount = Math.min(100.0f, this.nitroAmount + 22.0f);
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

    public int getEngineTier()   { return engineTier; }
    public int getHandlingTier() { return handlingTier; }
    public int getArmourTier()   { return armourTier; }
    public int getNitroTier()    { return nitroTier; }

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
