package com.gackstone.chase;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.gackstone.chase.audio.AudioManager;
import com.gackstone.chase.audio.SoundRegistry;
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
 * <p>Connects platform runtime shell to the 3D game engine, state machine,
 * input controllers, multi-tier upgrades, and responsive Scene2D UI screens.
 */
public class ChaseGame extends Game implements GameStateMachine.StateChangeListener,
        GameEvents.GameEventListener {

    private final ISaveStorage saveStorage;

    // ── Core Managers ─────────────────────────────────────────────────────────
    private GameStateMachine       stateMachine;
    private SaveManager            saveManager;
    private AudioManager           audioManager;
    private UIManager              uiManager;
    private TouchGestureController touchController;
    private GameManager            gameManager;
    private DebugOverlay           debugOverlay;

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
        stateMachine = new GameStateMachine();
        saveManager  = new SaveManager(saveStorage);
        audioManager = AudioManager.getInstance();
        uiManager    = new UIManager();
        debugOverlay = new DebugOverlay();

        if (saveManager.getData() != null) {
            debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());
            audioManager.setMasterVolume(saveManager.getData().getMasterVolume());
            audioManager.setMusicVolume(saveManager.getData().getMusicVolume());
            audioManager.setSfxVolume(saveManager.getData().getSfxVolume());
        }

        // 2. Input + 3D runtime
        touchController = new TouchGestureController();
        if (saveManager.getData() != null) {
            touchController.setSensitivity(saveManager.getData().getSteeringSensitivity());
        }

        gameManager = new GameManager(touchController,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Apply saved car selection & upgrades
        applySavedVehicleAndTheme();

        // 3. Initialize UI Screens
        mainMenuScreen  = new MainMenuScreen(this);
        settingsScreen  = new SettingsScreen(this);
        garageScreen    = new GarageScreen(this);
        envSelectScreen = new EnvSelectScreen(this);
        hudScreen       = new GameHUDScreen(this, gameManager);
        pauseMenuScreen = new PauseMenuScreen(this);
        gameOverScreen  = new GameOverScreen(this);

        // 4. In-game input multiplexer (HUD touch gestures + 3D vehicle control)
        playingInputMultiplexer = new InputMultiplexer();
        playingInputMultiplexer.addProcessor(hudScreen.getStage());
        playingInputMultiplexer.addProcessor(touchController);

        // 5. Register Listeners & Initial Transition
        stateMachine.addListener(this);
        GameEvents.addListener(this);

        stateMachine.transitionTo(GameState.MAIN_MENU);
    }

    private void applySavedVehicleAndTheme() {
        if (saveManager == null || saveManager.getData() == null || gameManager == null) return;

        String savedCarId = saveManager.getData().getSelectedCarId();
        CarDefinition savedCar = (savedCarId != null && !savedCarId.isEmpty())
                ? CarRegistry.getById(savedCarId) : CarRegistry.getPlayerCars().first();

        int eTier = saveManager.getData().getUpgradeTier(savedCar.getId(), "engine");
        int hTier = saveManager.getData().getUpgradeTier(savedCar.getId(), "handling");
        int aTier = saveManager.getData().getUpgradeTier(savedCar.getId(), "armour");
        int nTier = saveManager.getData().getUpgradeTier(savedCar.getId(), "nitro");
        gameManager.selectPlayerCarWithUpgrades(savedCar, eTier, hTier, aTier, nTier);

        String savedThemeId = saveManager.getData().getSelectedThemeId();
        if (savedThemeId != null && !savedThemeId.isEmpty()) {
            gameManager.selectEnvironment(savedThemeId);
        } else {
            gameManager.selectEnvironment(EnvironmentRegistry.THEME_NEON_CITY);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // State Machine Transitions
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onStateChanged(GameState previousState, GameState newState) {
        if (newState == null) return;

        switch (newState) {
            case MAIN_MENU:
                if (mainMenuScreen != null) {
                    setScreen(mainMenuScreen);
                }
                break;

            case SETTINGS:
                if (settingsScreen != null) {
                    setScreen(settingsScreen);
                }
                break;

            case GARAGE:
                if (garageScreen != null) {
                    setScreen(garageScreen);
                }
                break;

            case ENV_SELECT:
                if (envSelectScreen != null) {
                    setScreen(envSelectScreen);
                }
                break;

            case PLAYING:
                setScreen(null); // 3D gameplay scene rendered manually in render()

                // Apply latest vehicle upgrades and theme before starting
                applySavedVehicleAndTheme();

                if (gameManager != null && previousState != GameState.PAUSED) {
                    gameManager.restart();
                }

                if (playingInputMultiplexer != null) {
                    Gdx.input.setInputProcessor(playingInputMultiplexer);
                }
                break;

            case PAUSED:
                if (pauseMenuScreen != null) {
                    Gdx.input.setInputProcessor(pauseMenuScreen.getStage());
                }
                break;

            case GAME_OVER:
                if (gameOverScreen != null) {
                    Gdx.input.setInputProcessor(gameOverScreen.getStage());
                }
                break;

            default:
                break;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Render Loop
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        GameState state = (stateMachine != null) ? stateMachine.getCurrentState() : null;

        if (state == GameState.PLAYING) {
            clearScreen();
            if (gameManager != null) {
                gameManager.update(delta);
                gameManager.render();
            }
            if (hudScreen != null) {
                hudScreen.update(delta);
                hudScreen.render();
            }
            if (debugOverlay != null && saveManager != null && saveManager.getData() != null) {
                debugOverlay.setEnabled(saveManager.getData().isDebugOverlayEnabled());
                debugOverlay.render(gameManager);
            }

        } else if (state == GameState.PAUSED) {
            clearScreen();
            if (gameManager != null) {
                gameManager.render();
            }
            if (hudScreen != null) {
                hudScreen.render();
            }
            if (pauseMenuScreen != null) {
                pauseMenuScreen.update(delta);
                pauseMenuScreen.render();
            }

        } else if (state == GameState.GAME_OVER) {
            clearScreen();
            if (gameManager != null) {
                gameManager.render();
            }
            if (gameOverScreen != null) {
                gameOverScreen.update(delta);
                gameOverScreen.render();
            }

        } else {
            super.render(); // Active ScreenAdapter handles its own rendering
        }
    }

    private void clearScreen() {
        Color sky = null;
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
    // Game Events
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onPlayerCaught(float finalDistance, long finalScore) {
        if (saveManager != null) {
            boolean isNewRecord = saveManager.recordRunResult(finalScore, finalDistance);
            if (gameOverScreen != null) {
                gameOverScreen.setSessionResults(finalScore, finalDistance, isNewRecord);
            }
        }
        if (stateMachine != null) {
            stateMachine.transitionTo(GameState.GAME_OVER);
        }
    }

    @Override
    public void onNearMiss(float bonusScore) {
        if (audioManager != null) {
            audioManager.playSound(SoundRegistry.SND_DODGE, SoundRegistry.AudioCategory.EFFECTS);
        }
        if (saveManager != null && saveManager.getData() != null) {
            saveManager.getData().addNearMiss();
        }
    }

    @Override
    public void onChallengeCompleted(String title, int coinReward) {
        if (saveManager != null && saveManager.getData() != null) {
            saveManager.getData().addCoins(coinReward);
            saveManager.save();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Resize / Pause / Resume / Dispose
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (gameManager     != null) gameManager.resize(width, height);
        if (hudScreen       != null) hudScreen.resize(width, height);
        if (pauseMenuScreen != null) pauseMenuScreen.resize(width, height);
        if (gameOverScreen  != null) gameOverScreen.resize(width, height);
        if (mainMenuScreen  != null) mainMenuScreen.resize(width, height);
        if (garageScreen    != null) garageScreen.resize(width, height);
        if (envSelectScreen != null) envSelectScreen.resize(width, height);
        if (settingsScreen  != null) settingsScreen.resize(width, height);
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
        if (gameManager     != null) gameManager.dispose();
        if (uiManager       != null) uiManager.dispose();
        if (audioManager    != null) audioManager.dispose();
        if (debugOverlay    != null) debugOverlay.dispose();
        if (hudScreen       != null) hudScreen.dispose();
        if (pauseMenuScreen != null) pauseMenuScreen.dispose();
        if (gameOverScreen  != null) gameOverScreen.dispose();
        if (mainMenuScreen  != null) mainMenuScreen.dispose();
        if (garageScreen    != null) garageScreen.dispose();
        if (envSelectScreen != null) envSelectScreen.dispose();
        if (settingsScreen  != null) settingsScreen.dispose();
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
