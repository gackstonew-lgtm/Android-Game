package com.gackstone.chase.enemy;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Manages enemies lifecycle, updates, rendering, and collision registration.
 */
public class EnemyManager {

    private final Array<EnemyEntity> enemies = new Array<>(false, 8);
    private final Array<ChaseController> chaseControllers = new Array<>(false, 8);
    private final PlayerEntity player;
    private final CollisionManager collisionManager;

    public EnemyManager(PlayerEntity player, CollisionManager collisionManager) {
        this.player = player;
        this.collisionManager = collisionManager;
    }

    public EnemyEntity spawnEnemy(Vector3 position) {
        EnemyEntity enemy = new EnemyEntity();
        enemy.reset(position);
        ChaseController controller = new ChaseController(enemy, player);

        enemies.add(enemy);
        chaseControllers.add(controller);
        collisionManager.register(enemy);
        return enemy;
    }

    public void update(float delta) {
        for (int i = 0; i < chaseControllers.size; i++) {
            chaseControllers.get(i).update(delta);
        }
    }

    public void render(ModelBatch modelBatch) {
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).render(modelBatch);
        }
    }

    public void reset() {
        for (int i = 0; i < enemies.size; i++) {
            collisionManager.unregister(enemies.get(i));
            enemies.get(i).dispose();
        }
        enemies.clear();
        chaseControllers.clear();

        // Spawn initial primary pursuit enemy
        Vector3 initialEnemyPos = new Vector3(0, 0.45f, -GameConfig.ENEMY_INITIAL_DISTANCE_BEHIND);
        spawnEnemy(initialEnemyPos);
    }

    public void dispose() {
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).dispose();
        }
        enemies.clear();
        chaseControllers.clear();
    }

    public Array<EnemyEntity> getEnemies() {
        return enemies;
    }

    public ChaseController getPrimaryController() {
        return chaseControllers.size > 0 ? chaseControllers.get(0) : null;
    }
}
