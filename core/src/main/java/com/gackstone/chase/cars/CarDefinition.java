package com.gackstone.chase.cars;

import com.badlogic.gdx.graphics.Color;
import com.gackstone.chase.assets.ModelRegistry;

/**
 * Data-driven representation of a vehicle in Chase.
 * Defines performance stats, pricing, 3D model link, and visual customization.
 */
public class CarDefinition {

    public enum VehicleType {
        PLAYER,
        ENEMY,
        TRAFFIC
    }

    private final String id;
    private final String displayName;
    private final VehicleType type;
    private final String description;
    private final String modelKey;

    // Performance Stats
    private final float maxSpeed;
    private final float acceleration;
    private final float handling;
    private final float maxHealth;
    private final float mass;

    // Economy & Unlocks
    private final boolean unlockedByDefault;
    private final int unlockPrice;

    // Visuals
    private final Color primaryColor;
    private final Color secondaryColor;
    private final Color glowColor;

    public CarDefinition(String id, String displayName, VehicleType type, String description,
                         String modelKey, float maxSpeed, float acceleration, float handling,
                         float maxHealth, float mass, boolean unlockedByDefault, int unlockPrice,
                         Color primaryColor, Color secondaryColor, Color glowColor) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.description = description;
        this.modelKey = modelKey;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.handling = handling;
        this.maxHealth = maxHealth;
        this.mass = mass;
        this.unlockedByDefault = unlockedByDefault;
        this.unlockPrice = unlockPrice;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.glowColor = glowColor;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public VehicleType getType() { return type; }
    public String getDescription() { return description; }
    public String getModelKey() { return modelKey; }

    public float getMaxSpeed() { return maxSpeed; }
    public float getAcceleration() { return acceleration; }
    public float getHandling() { return handling; }
    public float getMaxHealth() { return maxHealth; }
    public float getMass() { return mass; }

    public boolean isUnlockedByDefault() { return unlockedByDefault; }
    public int getUnlockPrice() { return unlockPrice; }

    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public Color getGlowColor() { return glowColor; }
}
