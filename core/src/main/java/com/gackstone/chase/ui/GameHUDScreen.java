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
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameManager;
import com.gackstone.chase.core.GameState;
import com.gackstone.chase.player.PlayerState;

/**
 * Real-time responsive HUD rendered above the 3D scene.
 * Displays speed, distance, score, player health, chase proximity alert, and pause controls.
 */
public class GameHUDScreen implements Disposable {

    private final ChaseGame game;
    private final Stage stage;
    private final GameManager gameManager;

    private Label scoreLabel;
    private Label distanceLabel;
    private Label speedLabel;
    private Label healthLabel;
    private Label warningLabel;
    private TextButton pauseButton;

    public GameHUDScreen(ChaseGame game, GameManager gameManager) {
        this.game = game;
        this.gameManager = gameManager;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));

        setupUI();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().pad(20);
        stage.addActor(rootTable);

        // Top Status Bar
        scoreLabel = new Label("SCORE: 0", game.getUiManager().getSkin(), "default");
        distanceLabel = new Label("DIST: 0m", game.getUiManager().getSkin(), "default");
        speedLabel = new Label("SPEED: 0 km/h", game.getUiManager().getSkin(), "default");
        healthLabel = new Label("HEALTH: 100%", game.getUiManager().getSkin(), "default");
        warningLabel = new Label("! PURSUIT INCOMING !", game.getUiManager().getSkin(), "alert");
        warningLabel.setVisible(false);

        pauseButton = new TextButton("PAUSE", game.getUiManager().getSkin());
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.PAUSED);
            }
        });

        // Top Row: Stats on left/center, Pause button on right
        Table statsTable = new Table();
        statsTable.add(scoreLabel).padRight(30);
        statsTable.add(distanceLabel).padRight(30);
        statsTable.add(speedLabel).padRight(30);
        statsTable.add(healthLabel);

        rootTable.add(statsTable).expandX().left();
        rootTable.add(pauseButton).size(120, 50).right().row();
        
        // Warning banner in center
        rootTable.add(warningLabel).colspan(2).center().padTop(40).row();
    }

    public void update(float delta) {
        if (gameManager == null) return;

        PlayerState pState = gameManager.getPlayer().getState();
        scoreLabel.setText(String.format("SCORE: %d", gameManager.getCurrentScore()));
        distanceLabel.setText(String.format("DIST: %.0fm", pState.getDistanceTraveled()));
        speedLabel.setText(String.format("SPEED: %.0f km/h", pState.getForwardSpeed() * 3.6f));
        
        float hp = pState.getHealth();
        healthLabel.setText(String.format("HEALTH: %.0f%%", hp));
        if (hp < 30.0f) {
            healthLabel.setColor(Color.RED);
        } else {
            healthLabel.setColor(Color.WHITE);
        }

        // Chaser proximity alert
        float chaserDist = gameManager.getEnemyDistance();
        if (chaserDist > 0 && chaserDist < 12.0f) {
            warningLabel.setVisible(true);
            warningLabel.setText(String.format("! CHASER PROXIMITY: %.1fm !", chaserDist));
        } else {
            warningLabel.setVisible(false);
        }

        stage.act(Math.min(delta, 1 / 30f));
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
