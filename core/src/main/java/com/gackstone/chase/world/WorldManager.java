package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerEntity;

/**
 * High-level orchestrator of the continuous 3D world:
 * - Dynamic continuous track generation & theming
 * - Roadside prop spawning and recycling
 * - Obstacle spawning (cones, jersey barriers)
 * - Dynamic civilian traffic simulation
 */
public class WorldManager implements Disposable {

    private final ModelRegistry modelRegistry;
    private final EnvironmentRenderer environmentRenderer;
    private final CollisionManager collisionManager;
    private final PlayerEntity player;

    // Obstacles
    private final Array<ObstacleEntity> activeObstacles = new Array<>(false, 32);
    private float nextObstacleSpawnZ = 40.0f;
    private static final float OBSTACLE_SPAWN_INTERVAL = 32.0f;

    // Traffic vehicles
    private final Array<TrafficVehicleEntity> activeTraffic = new Array<>(false, 16);
    private final Pool<TrafficVehicleEntity> trafficPool = new Pool<TrafficVehicleEntity>(8, 32) {
        @Override
        protected TrafficVehicleEntity newObject() {
            return new TrafficVehicleEntity();
        }
    };
    private float nextTrafficSpawnZ = 60.0f;
    private static final float TRAFFIC_SPAWN_INTERVAL = 45.0f;

    public WorldManager(PlayerEntity player, CollisionManager collisionManager, ModelRegistry modelRegistry) {
        this.player = player;
        this.collisionManager = collisionManager;
        this.modelRegistry = modelRegistry;
        this.environmentRenderer = new EnvironmentRenderer(modelRegistry);

        spawnInitialSpawns();
    }

    private void spawnInitialSpawns() {
        for (int i = 0; i < 5; i++) {
            spawnObstacleAt(nextObstacleSpawnZ);
            nextObstacleSpawnZ += OBSTACLE_SPAWN_INTERVAL;
        }

        for (int i = 0; i < 3; i++) {
            spawnTrafficAt(nextTrafficSpawnZ);
            nextTrafficSpawnZ += TRAFFIC_SPAWN_INTERVAL;
        }
    }

    private void spawnObstacleAt(float z) {
        float[] lanes = {-4.5f, 0.0f, 4.5f};
        float laneX = lanes[MathUtils.random(0, lanes.length - 1)];

        boolean isCone = MathUtils.randomBoolean(0.4f);
        ObstacleEntity.ObstacleType type = isCone ? ObstacleEntity.ObstacleType.TRAFFIC_CONE : ObstacleEntity.ObstacleType.CONCRETE_BARRIER;
        String modelKey = isCone ? ModelRegistry.KEY_OBS_CONE : ModelRegistry.KEY_OBS_BARRIER;

        ObstacleEntity obstacle = new ObstacleEntity(
                modelRegistry.createInstance(modelKey),
                type,
                new Vector3(laneX, type == ObstacleEntity.ObstacleType.TRAFFIC_CONE ? 0.35f : 0.5f, z)
        );

        activeObstacles.add(obstacle);
        collisionManager.register(obstacle);
    }

    private void spawnTrafficAt(float z) {
        float[] lanes = {-4.0f, 0.0f, 4.0f};
        float laneX = lanes[MathUtils.random(0, lanes.length - 1)];

        boolean isTruck = MathUtils.randomBoolean(0.35f);
        String modelKey = isTruck ? ModelRegistry.KEY_TRAFFIC_TRUCK : ModelRegistry.KEY_TRAFFIC_SEDAN;
        float trafficSpeed = MathUtils.random(14.0f, 20.0f);

        TrafficVehicleEntity traffic = trafficPool.obtain();
        traffic.init(
                modelRegistry.createInstance(modelKey),
                laneX,
                0.45f,
                z,
                trafficSpeed,
                isTruck
        );

        activeTraffic.add(traffic);
        collisionManager.register(traffic);
    }

    public void update(float delta) {
        float playerZ = player.getPosition().z;

        // 1. Update endless environment segments & props
        environmentRenderer.update(playerZ);

        // 2. Update and recycle traffic vehicles
        for (int i = activeTraffic.size - 1; i >= 0; i--) {
            TrafficVehicleEntity traffic = activeTraffic.get(i);
            traffic.update(delta);

            if (traffic.getPosition().z < playerZ - 25.0f || !traffic.isActive()) {
                collisionManager.unregister(traffic);
                activeTraffic.removeIndex(i);
                trafficPool.free(traffic);
            }
        }

        // 3. Recycle obstacles behind player
        for (int i = activeObstacles.size - 1; i >= 0; i--) {
            ObstacleEntity obstacle = activeObstacles.get(i);
            if (obstacle.getPosition().z < playerZ - 25.0f || !obstacle.isActive()) {
                collisionManager.unregister(obstacle);
                obstacle.dispose();
                activeObstacles.removeIndex(i);
            }
        }

        // 4. Spawn new obstacles ahead
        while (nextObstacleSpawnZ < playerZ + 220.0f) {
            spawnObstacleAt(nextObstacleSpawnZ);
            nextObstacleSpawnZ += OBSTACLE_SPAWN_INTERVAL + MathUtils.random(-4.0f, 8.0f);
        }

        // 5. Spawn new traffic ahead
        while (nextTrafficSpawnZ < playerZ + 240.0f) {
            spawnTrafficAt(nextTrafficSpawnZ);
            nextTrafficSpawnZ += TRAFFIC_SPAWN_INTERVAL + MathUtils.random(-5.0f, 12.0f);
        }
    }

    public void render(ModelBatch modelBatch) {
        // 1. Render 3D environment & roadside props
        environmentRenderer.render(modelBatch);

        // 2. Render static obstacles
        for (int i = 0; i < activeObstacles.size; i++) {
            activeObstacles.get(i).render(modelBatch, environmentRenderer.getEnvironment());
        }

        // 3. Render traffic vehicles
        for (int i = 0; i < activeTraffic.size; i++) {
            activeTraffic.get(i).render(modelBatch, environmentRenderer.getEnvironment());
        }
    }

    public void reset() {
        for (int i = 0; i < activeObstacles.size; i++) {
            collisionManager.unregister(activeObstacles.get(i));
            activeObstacles.get(i).dispose();
        }
        activeObstacles.clear();

        for (int i = 0; i < activeTraffic.size; i++) {
            collisionManager.unregister(activeTraffic.get(i));
            trafficPool.free(activeTraffic.get(i));
        }
        activeTraffic.clear();

        nextObstacleSpawnZ = 40.0f;
        nextTrafficSpawnZ = 60.0f;
        spawnInitialSpawns();

        environmentRenderer.reset();
    }

    @Override
    public void dispose() {
        environmentRenderer.dispose();
        for (int i = 0; i < activeObstacles.size; i++) {
            activeObstacles.get(i).dispose();
        }
        activeObstacles.clear();

        for (int i = 0; i < activeTraffic.size; i++) {
            trafficPool.free(activeTraffic.get(i));
        }
        activeTraffic.clear();
    }

    public EnvironmentRenderer getEnvironmentRenderer() {
        return environmentRenderer;
    }
}
