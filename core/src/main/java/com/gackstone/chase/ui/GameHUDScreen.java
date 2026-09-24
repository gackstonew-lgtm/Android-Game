package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gackstone.chase.ChaseGame;
import com.gackstone.chase.audio.SoundRegistry;
import com.gackstone.chase.camera.CameraMode;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameManager;
import com.gackstone.chase.core.GameState;
import com.gackstone.chase.player.PlayerState;

/**
 * In-game HUD: speed, distance, score, health, nitro, proximity warning,
 * pause button, and camera-cycle button.
 */
public class GameHUDScreen implements Disposable {

    private final ChaseGame   game;
    private final Stage       stage;
    private final GameManager gameManager;

    private Label scoreLabel;
    private Label distanceLabel;
    private Label speedLabel;
    private Label healthLabel;
    private Label nitroLabel;
    private Label warningLabel;
    private Label camModeLabel;
    private TextButton pauseButton;
    private TextButton camButton;

    public GameHUDScreen(ChaseGame game, GameManager gameManager) {
        this.game        = game;
        this.gameManager = gameManager;
        this.stage       = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(18);
        stage.addActor(root);

        // ── Top stat bar ──────────────────────────────────────────────────────
        scoreLabel    = new Label("SCORE: 0",       game.getUiManager().getSkin(), "default");
        distanceLabel = new Label("DIST: 0m",       game.getUiManager().getSkin(), "default");
        speedLabel    = new Label("0 km/h",         game.getUiManager().getSkin(), "default");
        healthLabel   = new Label("HP: 100%",       game.getUiManager().getSkin(), "default");
        nitroLabel    = new Label("NITRO: 100%",    game.getUiManager().getSkin(), "default");
        nitroLabel.setColor(Color.CYAN);

        warningLabel = new Label("! PURSUIT INCOMING !", game.getUiManager().getSkin(), "alert");
        warningLabel.setVisible(false);

        camModeLabel = new Label("[CHASE]", game.getUiManager().getSkin(), "default");
        camModeLabel.setColor(new Color(0.6f, 0.7f, 0.9f, 1.0f));

        pauseButton = new TextButton("||", game.getUiManager().getSkin());
        pauseButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.PAUSED);
            }
        });

        camButton = new TextButton("CAM", game.getUiManager().getSkin());
        camButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (gameManager != null) {
                    gameManager.getChaseCamera().cycleMode();
                }
            }
        });

        Table statsRow = new Table();
        statsRow.add(scoreLabel).padRight(20);
        statsRow.add(distanceLabel).padRight(20);
        statsRow.add(speedLabel).padRight(20);
        statsRow.add(healthLabel).padRight(20);
        statsRow.add(nitroLabel);

        Table btnRow = new Table();
        btnRow.add(camButton).size(60, 44).padRight(8);
        btnRow.add(pauseButton).size(44, 44);

        root.add(statsRow).expandX().left();
        root.add(btnRow).right().row();

        // Camera mode indicator
        root.add(camModeLabel).colspan(2).right().padTop(2).padRight(4).row();

        // Warning banner
        root.add(warningLabel).colspan(2).center().padTop(30).row();
    }

    public void update(float delta) {
        if (gameManager == null) return;

        PlayerState ps = gameManager.getPlayer().getState();

        scoreLabel.setText(String.format("SCORE: %d", gameManager.getCurrentScore()));
        distanceLabel.setText(String.format("DIST: %.0fm", ps.getDistanceTraveled()));
        speedLabel.setText(String.format("%.0f km/h", ps.getForwardSpeed() * 3.6f));

        float hp = ps.getHealth();
        healthLabel.setText(String.format("HP: %.0f%%", hp));
        healthLabel.setColor(hp < 30.0f ? Color.RED : Color.WHITE);

        nitroLabel.setText(String.format("NOS: %.0f%%", ps.getNitroAmount()));
        nitroLabel.setColor(ps.isBoosting() ? Color.YELLOW : Color.CYAN);

        // Camera mode label
        CameraMode camMode = gameManager.getChaseCamera().getMode();
        camModeLabel.setText("[" + camMode.name() + "]");

        // Chaser proximity warning
        float dist = gameManager.getEnemyDistance();
        if (dist > 0 && dist < 12.0f) {
            warningLabel.setVisible(true);
            warningLabel.setText(String.format("! INTERCEPTOR: %.1fm !", dist));
        } else {
            warningLabel.setVisible(false);
        }

        stage.act(Math.min(delta, 1 / 30f));
    }

    public void render() { stage.draw(); }

    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    public Stage getStage() { return stage; }

    @Override
    public void dispose() { stage.dispose(); }
}
