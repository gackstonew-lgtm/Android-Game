package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerEntity;

/**
 * High-level orchestrator of the 3D world, managing continuous track generation,
 * dynamic obstacle spawning/recycling, and environment rendering.
 */
public class WorldManager {

    private final EnvironmentRenderer environmentRenderer;
    private final CollisionManager collisionManager;
    private final PlayerEntity player;

    private final Array<ObstacleEntity> activeObstacles = new Array<>(false, 32);
    private float nextObstacleSpawnZ = 40.0f;
    private static final float OBSTACLE_SPAWN_INTERVAL = 30.0f;

    public WorldManager(PlayerEntity player, CollisionManager collisionManager) {
        this.player = player;
        this.collisionManager = collisionManager;
        this.environmentRenderer = new EnvironmentRenderer();

        spawnInitialObstacles();
    }

    private void spawnInitialObstacles() {
        for (int i = 0; i < 6; i++) {
            spawnObstacleAt(nextObstacleSpawnZ);
            nextObstacleSpawnZ += OBSTACLE_SPAWN_INTERVAL;
        }
    }

    private void spawnObstacleAt(float z) {
        // Choose one of three lanes (-4.0, 0.0, 4.0) with slight random jitter
        float[] lanes = {-4.0f, 0.0f, 4.0f};
        float laneX = lanes[MathUtils.random(0, lanes.length - 1)];

        ObstacleEntity obstacle = new ObstacleEntity(new Vector3(laneX, 0.6f, z));
        activeObstacles.add(obstacle);
        collisionManager.register(obstacle);
    }

    public void update(float delta) {
        float playerZ = player.getPosition().z;

        // 1. Update endless environment segments
        environmentRenderer.update(playerZ);

        // 2. Recycle obstacles that fell far behind
        for (int i = activeObstacles.size - 1; i >= 0; i--) {
            ObstacleEntity obstacle = activeObstacles.get(i);
            if (obstacle.getPosition().z < playerZ - 20.0f) {
                collisionManager.unregister(obstacle);
                obstacle.dispose();
                activeObstacles.removeIndex(i);
            }
        }

        // 3. Spawn new obstacles ahead
        while (nextObstacleSpawnZ < playerZ + 200.0f) {
            spawnObstacleAt(nextObstacleSpawnZ);
            nextObstacleSpawnZ += OBSTACLE_SPAWN_INTERVAL + MathUtils.random(-5.0f, 10.0f);
        }
    }

    public void render(ModelBatch modelBatch) {
        // Render 3D environment with lighting
        environmentRenderer.render(modelBatch);

        // Render obstacles
        for (int i = 0; i < activeObstacles.size; i++) {
            activeObstacles.get(i).render(modelBatch);
        }
    }

    public void reset() {
        for (int i = 0; i < activeObstacles.size; i++) {
            collisionManager.unregister(activeObstacles.get(i));
            activeObstacles.get(i).dispose();
        }
        activeObstacles.clear();

        nextObstacleSpawnZ = 40.0f;
        spawnInitialObstacles();
        environmentRenderer.reset();
    }

    public void dispose() {
        environmentRenderer.dispose();
        for (int i = 0; i < activeObstacles.size; i++) {
            activeObstacles.get(i).dispose();
        }
        activeObstacles.clear();
    }

    public EnvironmentRenderer getEnvironmentRenderer() {
        return environmentRenderer;
    }
}
