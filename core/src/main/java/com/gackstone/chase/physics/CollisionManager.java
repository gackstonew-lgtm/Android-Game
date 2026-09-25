package com.gackstone.chase.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameEvents;

/**
 * Centralized spatial collision & near-miss detection manager.
 * Evaluates bounding-box intersections and near-miss proximity without per-frame allocations.
 */
public class CollisionManager {

    private final Array<ICollidable> collidables = new Array<>(false, 64);
    private final ObjectSet<ICollidable> scoredNearMisses = new ObjectSet<>();

    private final Vector3 centerA = new Vector3();
    private final Vector3 centerB = new Vector3();

    public void register(ICollidable collidable) {
        if (collidable != null && !collidables.contains(collidable, true)) {
            collidables.add(collidable);
        }
    }

    public void unregister(ICollidable collidable) {
        collidables.removeValue(collidable, true);
        scoredNearMisses.remove(collidable);
    }

    public void clear() {
        collidables.clear();
        scoredNearMisses.clear();
    }

    /**
     * Performs spatial collision checks and near-miss detections between all registered physical entities.
     */
    public void update(float delta) {
        final int size = collidables.size;

        for (int i = 0; i < size; i++) {
            ICollidable a = collidables.get(i);
            if (!a.isCollisionActive()) continue;

            for (int j = i + 1; j < size; j++) {
                ICollidable b = collidables.get(j);
                if (!b.isCollisionActive()) continue;

                // Check layer mask compatibility
                if (a.getCollisionLayer().canCollideWith(b.getCollisionLayer()) 
                    || b.getCollisionLayer().canCollideWith(a.getCollisionLayer())) {
                    
                    if (a.getBoundingBox().intersects(b.getBoundingBox())) {
                        a.onCollision(b);
                        b.onCollision(a);
                    } else if (a.getCollisionLayer() == CollisionLayer.PLAYER || b.getCollisionLayer() == CollisionLayer.PLAYER) {
                        // Near-miss check between Player and Traffic/Obstacle
                        checkNearMiss(a, b);
                    }
                }
            }
        }
    }

    private void checkNearMiss(ICollidable a, ICollidable b) {
        ICollidable other = (a.getCollisionLayer() == CollisionLayer.PLAYER) ? b : a;
        if (other.getCollisionLayer() != CollisionLayer.ENEMY && other.getCollisionLayer() != CollisionLayer.OBSTACLE) {
            return;
        }

        if (scoredNearMisses.contains(other)) {
            return;
        }

        a.getBoundingBox().getCenter(centerA);
        b.getBoundingBox().getCenter(centerB);

        float dx = Math.abs(centerA.x - centerB.x);
        float dz = Math.abs(centerA.z - centerB.z);

        if (dx < GameConfig.NEAR_MISS_LATERAL_DIST && dz < GameConfig.NEAR_MISS_FORWARD_DIST) {
            scoredNearMisses.add(other);
            GameEvents.fireNearMiss(GameConfig.CLOSE_CALL_BONUS_SCORE);
        }
    }

    public int getCollidableCount() {
        return collidables.size;
    }
}
