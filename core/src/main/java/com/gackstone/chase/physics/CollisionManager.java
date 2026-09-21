package com.gackstone.chase.physics;

import com.badlogic.gdx.utils.Array;

/**
 * Centralized spatial collision manager.
 * Evaluates bounding-box intersections among active collidables without per-frame allocations.
 */
public class CollisionManager {

    private final Array<ICollidable> collidables = new Array<>(false, 64);
    private final Array<CollisionPair> activePairs = new Array<>(false, 32);

    private static class CollisionPair {
        ICollidable first;
        ICollidable second;

        void set(ICollidable a, ICollidable b) {
            this.first = a;
            this.second = b;
        }
    }

    public void register(ICollidable collidable) {
        if (collidable != null && !collidables.contains(collidable, true)) {
            collidables.add(collidable);
        }
    }

    public void unregister(ICollidable collidable) {
        collidables.removeValue(collidable, true);
    }

    public void clear() {
        collidables.clear();
        activePairs.clear();
    }

    /**
     * Performs spatial collision checks between all registered physical entities.
     */
    public void update(float delta) {
        activePairs.clear();

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
                    }
                }
            }
        }
    }

    public int getCollidableCount() {
        return collidables.size;
    }
}
