package com.gackstone.chase;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.gackstone.chase.audio.AudioManager;
import com.gackstone.chase.camera.CameraMode;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameEvents;
import com.gackstone.chase.core.GameManager;
import com.gackstone.chase.core.GameState;
import com.gackstone.chase.core.GameStateMachine;
import com.gackstone.chase.input.TouchGestureController;
import com.gackstone.chase.save.ISaveStorage;
import com.gackstone.chase.save.SaveManager;
import com.gackstone.chase.ui.DebugOverlay;
import com.gackstone.chase.ui.EnvSelectScreen;
import com.gackstone.chase.ui.GameHUDScreen;
import com.gackstone.chase.ui.GameOverScreen;
import com.gackstone.chase.ui.GarageScreen;
import com.gackstone.chase.ui.MainMenuScreen;
import com.gackstone.chase.ui.PauseMenuScreen;
import com.gackstone.chase.ui.SettingsScreen;
import com.gackstone.chase.ui.UIManager;
import com.gackstone.chase.world.EnvironmentRegistry;

/**
 * Root {@link Game} and master coordinator for Chase.
 *
 * <p>Responsibility: connects the Android host shell to the 3D runtime, state
 * machine, input multiplexer, audio buses, and responsive UI layers.
 *
 * <p>New in v2.0:
 * <ul>
 *   <li>{@link GarageScreen} – car selection with unlock/coin logic.
 *   <li>{@link EnvSelectScreen} – environment / track theme selector.
 *   <li>Camera-cycle button wired into the HUD.
 *   <li>Saved car and theme applied to the game session on start.
 * </ul>
 */
public class ChaseGame extends Game implements GameStateMachine.StateChangeListener,
        GameEvents.GameEventListener {

    private final ISaveStorage saveStorage;

    // ── Core managers ─────────────────────────────────────────────────────────
    private GameStateMachine      stateMachine;
    private SaveManager           saveManager;
    private AudioManager          audioManager;
    private UIManager             uiManager;
    private TouchGestureController touchController;
    private GameManager           gameManager;
    private DebugOverlay          debugOverlay;

    // ── Screens ───────────────────────────────────────────────────────────────
    private MainMenuScreen   mainMenuScreen;
    private SettingsScreen   settingsScreen;
    private GarageScreen     garageScreen;
    private EnvSelectScreen  envSelectScreen;
    private GameHUDScreen    hudScreen;
    private PauseMenuScreen  pauseMenuScreen;
    private GameOverScreen   gameOverScreen;

    private InputMultiplexer playingInputMultiplexer;

    public ChaseGame(ISaveStorage saveStorage) {
        this.saveStorage = saveStorage;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void create() {
        // 1. Core managers
        stateMachine  = new GameStateMachine();
        saveManager   = new SaveManager(saveStorage);
        audioManager  = AudioManager.getInstance();
        uiManager     = new UIManager();
        debugOverlay  = new DebugOverlay();
        debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());

        audioManager.setMasterVolume(saveManager.getData().getMasterVolume());
        audioManager.setMusicVolume(saveManager.getData().getMusicVolume());
        audioManager.setSfxVolume(saveManager.getData().getSfxVolume());

        // 2. Input + 3D runtime
        touchController = new TouchGestureController();
        gameManager = new GameManager(touchController,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Apply saved car selection
        String savedCarId = saveManager.getData().getSelectedCarId();
        if (savedCarId != null && !savedCarId.isEmpty()) {
            CarDefinition savedCar = CarRegistry.getById(savedCarId);
            gameManager.selectPlayerCar(savedCar);
        }

        // Apply saved environment theme
        String savedThemeId = saveManager.getData().getSelectedThemeId();
        if (savedThemeId != null && !savedThemeId.isEmpty()) {
            gameManager.selectEnvironment(savedThemeId);
        } else {
            gameManager.selectEnvironment(EnvironmentRegistry.THEME_NEON_CITY);
        }

        // 3. Screens
        mainMenuScreen  = new MainMenuScreen(this);
        settingsScreen  = new SettingsScreen(this);
        garageScreen    = new GarageScreen(this);
        envSelectScreen = new EnvSelectScreen(this);
        hudScreen       = new GameHUDScreen(this, gameManager);
        pauseMenuScreen = new PauseMenuScreen(this);
        gameOverScreen  = new GameOverScreen(this);

        // 4. In-game input (HUD gestures + 3D touch steering)
        playingInputMultiplexer = new InputMultiplexer();
        playingInputMultiplexer.addProcessor(hudScreen.getStage());
        playingInputMultiplexer.addProcessor(touchController);

        // 5. Events
        stateMachine.addListener(this);
        GameEvents.addListener(this);

        stateMachine.transitionTo(GameState.MAIN_MENU);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // State machine
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onStateChanged(GameState previousState, GameState newState) {
        switch (newState) {
            case MAIN_MENU:
                setScreen(mainMenuScreen);
                break;
            case SETTINGS:
                setScreen(settingsScreen);
                break;
            case GARAGE:
                setScreen(garageScreen);
                break;
            case ENV_SELECT:
                setScreen(envSelectScreen);
                break;
            case PLAYING:
                setScreen(null);   // 3D scene rendered manually in render()
                // Restart session if starting fresh (not unpausing)
                if (previousState != GameState.PAUSED) {
                    gameManager.restart();
                }
                Gdx.input.setInputProcessor(playingInputMultiplexer);
                break;
            case PAUSED:
                Gdx.input.setInputProcessor(pauseMenuScreen.getStage());
                break;
            case GAME_OVER:
                Gdx.input.setInputProcessor(gameOverScreen.getStage());
                break;
            default:
                break;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Render
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void render() {
        float delta     = Gdx.graphics.getDeltaTime();
        GameState state = stateMachine.getCurrentState();

        if (state == GameState.PLAYING) {
            clearScreen();
            gameManager.update(delta);
            gameManager.render();
            hudScreen.update(delta);
            hudScreen.render();
            debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());
            debugOverlay.render(gameManager);

        } else if (state == GameState.PAUSED) {
            clearScreen();
            gameManager.render();
            hudScreen.render();
            pauseMenuScreen.update(delta);
            pauseMenuScreen.render();

        } else if (state == GameState.GAME_OVER) {
            clearScreen();
            gameManager.render();
            gameOverScreen.update(delta);
            gameOverScreen.render();

        } else {
            super.render();   // Scene2D screens handle themselves
        }
    }

    private void clearScreen() {
        com.badlogic.gdx.graphics.Color sky = null;
        if (gameManager != null && gameManager.getWorldManager() != null 
                && gameManager.getWorldManager().getEnvironmentRenderer() != null
                && gameManager.getWorldManager().getEnvironmentRenderer().getCurrentTheme() != null) {
            sky = gameManager.getWorldManager().getEnvironmentRenderer().getCurrentTheme().getSkyClearColor();
        }
        if (sky != null) {
            Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1.0f);
        } else {
            Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1.0f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Game events
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onPlayerCaught(float finalDistance, long finalScore) {
        boolean isNewRecord = saveManager.recordRunResult(finalScore, finalDistance);
        gameOverScreen.setSessionResults(finalScore, finalDistance, isNewRecord);
        stateMachine.transitionTo(GameState.GAME_OVER);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Resize / Pause / Resume / Dispose
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (gameManager    != null) gameManager.resize(width, height);
        if (hudScreen       != null) hudScreen.resize(width, height);
        if (pauseMenuScreen != null) pauseMenuScreen.resize(width, height);
        if (gameOverScreen  != null) gameOverScreen.resize(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        if (stateMachine != null && stateMachine.getCurrentState() == GameState.PLAYING) {
            stateMachine.transitionTo(GameState.PAUSED);
        }
        if (audioManager != null) audioManager.pauseMusic();
    }

    @Override
    public void resume() {
        super.resume();
        if (audioManager != null) audioManager.resumeMusic();
    }

    @Override
    public void dispose() {
        super.dispose();
        GameEvents.removeListener(this);
        if (gameManager     != null) gameManager.dispose();
        if (uiManager       != null) uiManager.dispose();
        if (audioManager    != null) audioManager.dispose();
        if (debugOverlay    != null) debugOverlay.dispose();
        if (hudScreen       != null) hudScreen.dispose();
        if (pauseMenuScreen != null) pauseMenuScreen.dispose();
        if (gameOverScreen  != null) gameOverScreen.dispose();
        if (garageScreen    != null) garageScreen.dispose();
        if (envSelectScreen != null) envSelectScreen.dispose();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────────

    public GameStateMachine getStateMachine() { return stateMachine; }
    public SaveManager      getSaveManager()  { return saveManager; }
    public AudioManager     getAudioManager() { return audioManager; }
    public UIManager        getUiManager()    { return uiManager; }
    public GameManager      getGameManager()  { return gameManager; }
}
