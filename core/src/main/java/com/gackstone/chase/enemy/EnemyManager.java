package com.gackstone.chase.enemy;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Lifecycle manager for all enemy (police) pursuer entities and their AI controllers.
 *
 * <p>Supports multiple simultaneous pursuers with different {@link CarDefinition} variants
 * (Patrol Cruiser, Tactical SUV) spawning dynamically as difficulty scales.
 */
public class EnemyManager implements Disposable {

    private final ModelRegistry modelRegistry;
    private final Array<EnemyEntity>     enemies          = new Array<>(false, 8);
    private final Array<ChaseController> chaseControllers = new Array<>(false, 8);
    private final PlayerEntity    player;
    private final CollisionManager collisionManager;

    /** Difficulty escalation: extra enemies unlock after reaching distance thresholds. */
    private static final float[] EXTRA_ENEMY_DISTANCES = {600.0f, 1500.0f};
    private int extraEnemiesSpawned = 0;

    public EnemyManager(PlayerEntity player, CollisionManager collisionManager,
                        ModelRegistry modelRegistry) {
        this.player          = player;
        this.collisionManager = collisionManager;
        this.modelRegistry   = modelRegistry;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Spawning
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Spawns an enemy using the given car definition at the specified world position.
     */
    public EnemyEntity spawnEnemy(CarDefinition carDef, Vector3 position) {
        EnemyEntity enemy = new EnemyEntity(modelRegistry, carDef);
        enemy.reset(position);

        ChaseController controller = new ChaseController(enemy, player);
        enemies.add(enemy);
        chaseControllers.add(controller);
        collisionManager.register(enemy);
        return enemy;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Update / Render
    // ──────────────────────────────────────────────────────────────────────────

    public void update(float delta) {
        for (int i = 0; i < chaseControllers.size; i++) {
            chaseControllers.get(i).update(delta);
        }

        // Difficulty escalation: spawn additional pursuers at distance thresholds
        float distanceTraveled = player.getState().getDistanceTraveled();
        if (extraEnemiesSpawned < EXTRA_ENEMY_DISTANCES.length &&
                distanceTraveled >= EXTRA_ENEMY_DISTANCES[extraEnemiesSpawned]) {

            CarDefinition extraCarDef = getEscalationCarDef(extraEnemiesSpawned);
            Vector3 spawnPos = new Vector3(
                    MathUtils.random(-3.0f, 3.0f),
                    0.45f,
                    player.getPosition().z - GameConfig.ENEMY_INITIAL_DISTANCE_BEHIND - 10.0f
            );
            spawnEnemy(extraCarDef, spawnPos);
            extraEnemiesSpawned++;
        }
    }

    private CarDefinition getEscalationCarDef(int tier) {
        Array<CarDefinition> enemyCars = CarRegistry.getEnemyCars();
        // Tier 0 → Patrol Cruiser, Tier 1 → Tactical SUV (if available)
        int index = Math.min(tier + 1, enemyCars.size - 1);
        return enemyCars.get(index);
    }

    public void render(ModelBatch batch, Environment environment) {
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).render(batch, environment);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    public void reset() {
        for (int i = 0; i < enemies.size; i++) {
            collisionManager.unregister(enemies.get(i));
            enemies.get(i).dispose();
        }
        enemies.clear();
        chaseControllers.clear();
        extraEnemiesSpawned = 0;

        // Spawn primary patrol cruiser
        CarDefinition primaryDef = CarRegistry.getEnemyCars().first();
        Vector3 initialPos = new Vector3(0, 0.45f, -GameConfig.ENEMY_INITIAL_DISTANCE_BEHIND);
        spawnEnemy(primaryDef, initialPos);
    }

    @Override
    public void dispose() {
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).dispose();
        }
        enemies.clear();
        chaseControllers.clear();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────────

    public Array<EnemyEntity> getEnemies() { return enemies; }

    public ChaseController getPrimaryController() {
        return chaseControllers.size > 0 ? chaseControllers.get(0) : null;
    }
}
