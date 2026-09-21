package com.gackstone.chase;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.gackstone.chase.audio.AudioManager;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameEvents;
import com.gackstone.chase.core.GameManager;
import com.gackstone.chase.core.GameState;
import com.gackstone.chase.core.GameStateMachine;
import com.gackstone.chase.input.TouchGestureController;
import com.gackstone.chase.save.ISaveStorage;
import com.gackstone.chase.save.SaveManager;
import com.gackstone.chase.ui.DebugOverlay;
import com.gackstone.chase.ui.GameHUDScreen;
import com.gackstone.chase.ui.GameOverScreen;
import com.gackstone.chase.ui.MainMenuScreen;
import com.gackstone.chase.ui.PauseMenuScreen;
import com.gackstone.chase.ui.SettingsScreen;
import com.gackstone.chase.ui.UIManager;

/**
 * Root ApplicationListener and Master Coordinator for Chase.
 * Connects the Android host shell to the 3D runtime, state machine,
 * input multiplexer, audio buses, and responsive UI layers.
 */
public class ChaseGame extends Game implements GameStateMachine.StateChangeListener, GameEvents.GameEventListener {

    private final ISaveStorage saveStorage;

    private GameStateMachine stateMachine;
    private SaveManager saveManager;
    private AudioManager audioManager;
    private UIManager uiManager;
    private TouchGestureController touchController;
    private GameManager gameManager;
    private DebugOverlay debugOverlay;

    // Screens and Overlay UI
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private GameHUDScreen hudScreen;
    private PauseMenuScreen pauseMenuScreen;
    private GameOverScreen gameOverScreen;

    private InputMultiplexer playingInputMultiplexer;

    public ChaseGame(ISaveStorage saveStorage) {
        this.saveStorage = saveStorage;
    }

    @Override
    public void create() {
        // 1. Initialize core managers
        stateMachine = new GameStateMachine();
        saveManager = new SaveManager(saveStorage);
        audioManager = AudioManager.getInstance();
        uiManager = new UIManager();
        debugOverlay = new DebugOverlay();
        debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());

        // Apply saved audio volumes
        audioManager.setMasterVolume(saveManager.getData().getMasterVolume());
        audioManager.setMusicVolume(saveManager.getData().getMusicVolume());
        audioManager.setSfxVolume(saveManager.getData().getSfxVolume());

        // 2. Initialize input controller and 3D game runtime
        touchController = new TouchGestureController();
        gameManager = new GameManager(touchController, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 3. Initialize screens
        mainMenuScreen = new MainMenuScreen(this);
        settingsScreen = new SettingsScreen(this);
        hudScreen = new GameHUDScreen(this, gameManager);
        pauseMenuScreen = new PauseMenuScreen(this);
        gameOverScreen = new GameOverScreen(this);

        // Configure input multiplexer for in-game play (HUD gestures + 3D touch steering)
        playingInputMultiplexer = new InputMultiplexer();
        playingInputMultiplexer.addProcessor(hudScreen.getStage());
        playingInputMultiplexer.addProcessor(touchController);

        // Register listeners
        stateMachine.addListener(this);
        GameEvents.addListener(this);

        // Transition to initial Main Menu
        stateMachine.transitionTo(GameState.MAIN_MENU);
    }

    @Override
    public void onStateChanged(GameState previousState, GameState newState) {
        switch (newState) {
            case MAIN_MENU:
                setScreen(mainMenuScreen);
                break;
            case SETTINGS:
                setScreen(settingsScreen);
                break;
            case PLAYING:
                setScreen(null); // Clear 2D full-screen, 3D world rendered in render()
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

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        GameState current = stateMachine.getCurrentState();

        if (current == GameState.PLAYING) {
            // Clear screen & depth buffer
            Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            // 1. Update and render 3D game world
            gameManager.update(delta);
            gameManager.render();

            // 2. Update and render HUD on top
            hudScreen.update(delta);
            hudScreen.render();

            // 3. Render Debug overlay if enabled
            debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());
            debugOverlay.render(gameManager);

        } else if (current == GameState.PAUSED) {
            // Render frozen 3D scene in background
            Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            gameManager.render();

            // Render HUD and Pause overlay modal
            hudScreen.render();
            pauseMenuScreen.update(delta);
            pauseMenuScreen.render();

        } else if (current == GameState.GAME_OVER) {
            // Render final 3D scene in background
            Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            gameManager.render();

            // Render Game Over modal
            gameOverScreen.update(delta);
            gameOverScreen.render();

        } else {
            // Render standard 2D Screen (Main Menu, Settings)
            super.render();
        }
    }

    @Override
    public void onPlayerCaught(float finalDistance, long finalScore) {
        boolean isNewRecord = saveManager.recordRunResult(finalScore, finalDistance);
        gameOverScreen.setSessionResults(finalScore, finalDistance, isNewRecord);
        stateMachine.transitionTo(GameState.GAME_OVER);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (gameManager != null) gameManager.resize(width, height);
        if (hudScreen != null) hudScreen.resize(width, height);
        if (pauseMenuScreen != null) pauseMenuScreen.resize(width, height);
        if (gameOverScreen != null) gameOverScreen.resize(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        if (stateMachine != null && stateMachine.getCurrentState() == GameState.PLAYING) {
            stateMachine.transitionTo(GameState.PAUSED);
        }
        if (audioManager != null) {
            audioManager.pauseMusic();
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (audioManager != null) {
            audioManager.resumeMusic();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        GameEvents.removeListener(this);
        if (gameManager != null) gameManager.dispose();
        if (uiManager != null) uiManager.dispose();
        if (audioManager != null) audioManager.dispose();
        if (debugOverlay != null) debugOverlay.dispose();
        if (hudScreen != null) hudScreen.dispose();
        if (pauseMenuScreen != null) pauseMenuScreen.dispose();
        if (gameOverScreen != null) gameOverScreen.dispose();
    }

    public GameStateMachine getStateMachine() { return stateMachine; }
    public SaveManager getSaveManager() { return saveManager; }
    public AudioManager getAudioManager() { return audioManager; }
    public UIManager getUiManager() { return uiManager; }
    public GameManager getGameManager() { return gameManager; }
}
