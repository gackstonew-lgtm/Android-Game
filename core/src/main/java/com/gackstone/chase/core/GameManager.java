package com.gackstone.chase.core;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.camera.ChaseCamera;
import com.gackstone.chase.enemy.EnemyManager;
import com.gackstone.chase.input.IInputController;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerController;
import com.gackstone.chase.player.PlayerEntity;
import com.gackstone.chase.world.WorldManager;

/**
 * Coordinates active 3D gameplay systems: physics, player control, enemy pursuit AI,
 * camera interpolation, scoring, and world generation.
 */
public class GameManager implements Disposable, GameEvents.GameEventListener {

    private final PlayerEntity player;
    private final PlayerController playerController;
    private final CollisionManager collisionManager;
    private final EnemyManager enemyManager;
    private final WorldManager worldManager;
    private final ChaseCamera chaseCamera;
    private final ModelBatch modelBatch;

    private long currentScore = 0;
    private float scoreAccumulator = 0.0f;
    private boolean isGameOverTriggered = false;

    public GameManager(IInputController inputController, float viewportWidth, float viewportHeight) {
        this.collisionManager = new CollisionManager();
        this.player = new PlayerEntity();
        this.playerController = new PlayerController(player, inputController);
        this.chaseCamera = new ChaseCamera(player, viewportWidth, viewportHeight);
        this.worldManager = new WorldManager(player, collisionManager);
        this.enemyManager = new EnemyManager(player, collisionManager);
        this.modelBatch = new ModelBatch();

        // Register initial collidables
        collisionManager.register(player);
        enemyManager.reset();

        GameEvents.addListener(this);
    }

    public void update(float delta) {
        if (isGameOverTriggered) return;

        // 1. Update player physics & movement
        playerController.update(delta);

        // 2. Update pursuit enemies
        enemyManager.update(delta);

        // 3. Update continuous world & obstacle spawning
        worldManager.update(delta);

        // 4. Update collision detection
        collisionManager.update(delta);

        // 5. Update chase camera
        chaseCamera.update(delta);

        // 6. Update score based on distance and speed
        if (player.getState().isAlive()) {
            scoreAccumulator += player.getState().getForwardSpeed() * GameConfig.SCORE_PER_METER * delta;
            currentScore = (long) scoreAccumulator;
            GameEvents.fireScoreUpdated(currentScore, player.getState().getDistanceTraveled());
        } else if (!isGameOverTriggered) {
            isGameOverTriggered = true;
            GameEvents.firePlayerCaught(player.getState().getDistanceTraveled(), currentScore);
        }
    }

    public void render() {
        // Clear 3D depth buffer
        modelBatch.begin(chaseCamera.getPerspectiveCamera());
        
        // 1. Render World & Obstacles
        worldManager.render(modelBatch);

        // 2. Render Player
        player.render(modelBatch);

        // 3. Render Enemies
        enemyManager.render(modelBatch);

        modelBatch.end();
    }

    public void resize(int width, int height) {
        chaseCamera.resize(width, height);
    }

    public void restart() {
        isGameOverTriggered = false;
        currentScore = 0;
        scoreAccumulator = 0.0f;

        collisionManager.clear();
        player.reset();
        collisionManager.register(player);

        enemyManager.reset();
        worldManager.reset();
        chaseCamera.reset();

        GameEvents.fireGameRestarted();
    }

    public float getEnemyDistance() {
        if (enemyManager.getPrimaryController() != null) {
            return enemyManager.getPrimaryController().getDistanceToPlayer();
        }
        return 999.0f;
    }

    public PlayerEntity getPlayer() { return player; }
    public PlayerController getPlayerController() { return playerController; }
    public CollisionManager getCollisionManager() { return collisionManager; }
    public EnemyManager getEnemyManager() { return enemyManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public ChaseCamera getChaseCamera() { return chaseCamera; }
    public long getCurrentScore() { return currentScore; }
    public boolean isGameOver() { return isGameOverTriggered; }

    @Override
    public void dispose() {
        GameEvents.removeListener(this);
        player.dispose();
        enemyManager.dispose();
        worldManager.dispose();
        modelBatch.dispose();
    }
}
