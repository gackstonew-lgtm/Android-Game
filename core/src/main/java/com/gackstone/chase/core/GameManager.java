package com.gackstone.chase.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.camera.CameraMode;
import com.gackstone.chase.camera.ChaseCamera;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.enemy.EnemyManager;
import com.gackstone.chase.input.IInputController;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.player.PlayerController;
import com.gackstone.chase.player.PlayerEntity;
import com.gackstone.chase.world.EnvironmentRegistry;
import com.gackstone.chase.world.WorldManager;

/**
 * Coordinates active 3D gameplay systems: asset registry, physics, player
 * control, enemy pursuit AI, camera, scoring, and world generation.
 *
 * <p>Dependency graph: {@code GameManager} owns {@link ModelRegistry} and injects
 * it into every subsystem that needs 3D models, keeping a single source of truth
 * for model lifecycles.
 */
public class GameManager implements Disposable, GameEvents.GameEventListener {

    // ── Core asset pipeline ───────────────────────────────────────────────────
    private final ModelRegistry   modelRegistry;

    // ── Gameplay systems ──────────────────────────────────────────────────────
    private final PlayerEntity    player;
    private final PlayerController playerController;
    private final CollisionManager collisionManager;
    private final EnemyManager    enemyManager;
    private final WorldManager    worldManager;
    private final ChaseCamera     chaseCamera;
    private final ModelBatch      modelBatch;

    // ── Session state ─────────────────────────────────────────────────────────
    private long  currentScore       = 0;
    private float scoreAccumulator   = 0.0f;
    private boolean isGameOverTriggered = false;

    public GameManager(IInputController inputController, float viewportWidth, float viewportHeight) {
        // 1. Build asset registry and bake all models (synchronous on first boot)
        modelRegistry = new ModelRegistry(new AssetManager());
        modelRegistry.loadAllModels();

        // 2. Player
        collisionManager = new CollisionManager();
        player           = new PlayerEntity(modelRegistry);
        playerController = new PlayerController(player, inputController);
        collisionManager.register(player);

        // 3. Camera
        chaseCamera = new ChaseCamera(player, viewportWidth, viewportHeight);

        // 4. World
        worldManager = new WorldManager(player, collisionManager, modelRegistry);

        // 5. Enemies
        enemyManager = new EnemyManager(player, collisionManager, modelRegistry);
        enemyManager.reset();

        // 6. Rendering
        modelBatch = new ModelBatch();

        GameEvents.addListener(this);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Update / Render
    // ──────────────────────────────────────────────────────────────────────────

    public void update(float delta) {
        if (isGameOverTriggered) return;

        playerController.update(delta);
        enemyManager.update(delta);
        worldManager.update(delta);
        collisionManager.update(delta);
        chaseCamera.update(delta);

        if (player.getState().isAlive()) {
            scoreAccumulator += player.getState().getForwardSpeed() * GameConfig.SCORE_PER_METER * delta;
            currentScore      = (long) scoreAccumulator;
            GameEvents.fireScoreUpdated(currentScore, player.getState().getDistanceTraveled());
        } else if (!isGameOverTriggered) {
            isGameOverTriggered = true;
            GameEvents.firePlayerCaught(player.getState().getDistanceTraveled(), currentScore);
        }
    }

    public void render() {
        modelBatch.begin(chaseCamera.getPerspectiveCamera());

        // World, obstacles, traffic
        worldManager.render(modelBatch);

        // Player vehicle
        player.render(modelBatch, worldManager.getEnvironmentRenderer().getEnvironment());

        // Cockpit geometry only in first-person modes
        if (chaseCamera.getMode() == CameraMode.COCKPIT) {
            player.renderCockpit(modelBatch, worldManager.getEnvironmentRenderer().getEnvironment());
        }

        // Enemy pursuers
        enemyManager.render(modelBatch, worldManager.getEnvironmentRenderer().getEnvironment());

        modelBatch.end();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Session control
    // ──────────────────────────────────────────────────────────────────────────

    public void restart() {
        isGameOverTriggered = false;
        currentScore        = 0;
        scoreAccumulator    = 0.0f;

        collisionManager.clear();
        player.reset();
        collisionManager.register(player);

        enemyManager.reset();
        worldManager.reset();
        chaseCamera.reset();

        GameEvents.fireGameRestarted();
    }

    /**
     * Swaps the active player car at runtime without restarting the session.
     * Safe to call from the Garage screen.
     */
    public void selectPlayerCar(CarDefinition carDef) {
        player.setCarDefinition(carDef);
        player.reset();
    }

    /**
     * Switches the active environment theme without restarting the session.
     */
    public void selectEnvironment(String themeId) {
        worldManager.getEnvironmentRenderer().setTheme(
                EnvironmentRegistry.getById(themeId));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────────

    public float getEnemyDistance() {
        if (enemyManager.getPrimaryController() != null) {
            return enemyManager.getPrimaryController().getDistanceToPlayer();
        }
        return 999.0f;
    }

    public void resize(int width, int height) {
        chaseCamera.resize(width, height);
    }

    public PlayerEntity    getPlayer()         { return player; }
    public PlayerController getPlayerController() { return playerController; }
    public CollisionManager getCollisionManager() { return collisionManager; }
    public EnemyManager    getEnemyManager()   { return enemyManager; }
    public WorldManager    getWorldManager()   { return worldManager; }
    public ChaseCamera     getChaseCamera()    { return chaseCamera; }
    public ModelRegistry   getModelRegistry()  { return modelRegistry; }
    public long            getCurrentScore()   { return currentScore; }
    public boolean         isGameOver()        { return isGameOverTriggered; }

    @Override
    public void dispose() {
        GameEvents.removeListener(this);
        player.dispose();
        enemyManager.dispose();
        worldManager.dispose();
        modelBatch.dispose();
        modelRegistry.dispose();
    }
}
