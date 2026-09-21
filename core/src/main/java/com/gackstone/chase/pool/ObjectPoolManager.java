package com.gackstone.chase.pool;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Centralized object pool provider to eliminate per-frame heap allocations
 * and prevent Android GC stutter.
 */
public class ObjectPoolManager {

    private static final Pool<Vector3> vector3Pool = new Pool<Vector3>(32, 256) {
        @Override
        protected Vector3 newObject() {
            return new Vector3();
        }
    };

    public static Vector3 obtainVector3() {
        return vector3Pool.obtain();
    }

    public static void freeVector3(Vector3 vec) {
        if (vec != null) {
            vec.set(0, 0, 0);
            vector3Pool.free(vec);
        }
    }

    public static <T> T obtain(Class<T> type) {
        return Pools.obtain(type);
    }

    public static void free(Object object) {
        if (object != null) {
            Pools.free(object);
        }
    }
}
