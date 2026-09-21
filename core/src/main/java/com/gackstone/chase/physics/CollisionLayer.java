package com.gackstone.chase.physics;

/**
 * Categorized collision layers to distinguish between entity types
 * and optimize spatial collision queries.
 */
public enum CollisionLayer {
    PLAYER(0x01),
    ENEMY(0x02),
    OBSTACLE(0x04),
    COLLECTIBLE(0x08),
    ROAD_BOUNDARY(0x10);

    private final int bitMask;

    CollisionLayer(int bitMask) {
        this.bitMask = bitMask;
    }

    public int getBitMask() {
        return bitMask;
    }

    public boolean canCollideWith(CollisionLayer other) {
        // Player can collide with Enemy, Obstacle, Collectible, Road Boundary
        if (this == PLAYER) {
            return other == ENEMY || other == OBSTACLE || other == COLLECTIBLE || other == ROAD_BOUNDARY;
        }
        // Enemy can collide with Player and Obstacles
        if (this == ENEMY) {
            return other == PLAYER || other == OBSTACLE;
        }
        return false;
    }
}
