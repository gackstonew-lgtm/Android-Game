package com.gackstone.chase.physics;

import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Interface implemented by all 3D physical entities in the Chase world.
 */
public interface ICollidable {

    /**
     * Get the current Axis-Aligned Bounding Box (AABB) in world space.
     */
    BoundingBox getBoundingBox();

    /**
     * Get the collision layer of this physical entity.
     */
    CollisionLayer getCollisionLayer();

    /**
     * Whether this collidable is currently active in the physics simulation.
     */
    boolean isCollisionActive();

    /**
     * Callback triggered when collision with another entity occurs.
     */
    void onCollision(ICollidable other);
}
