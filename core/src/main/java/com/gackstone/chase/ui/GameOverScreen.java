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
import com.gackstone.chase.core.GameState;

/**
 * Game Over dialog displaying actual run statistics, new record notification,
 * and restart options.
 */
public class GameOverScreen implements Disposable {

    private final ChaseGame game;
    private final Stage stage;

    private Label finalScoreLabel;
    private Label distanceLabel;
    private Label recordBadgeLabel;

    public GameOverScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));

        setupUI();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();
        stage.addActor(rootTable);

        Table modalBox = new Table();
        modalBox.pad(30);

        Label titleLabel = new Label("PURSUIT TERMINATED", game.getUiManager().getSkin(), "alert");
        recordBadgeLabel = new Label("★ NEW PERSONAL BEST! ★", game.getUiManager().getSkin(), "title");
        recordBadgeLabel.setColor(Color.GOLD);
        recordBadgeLabel.setVisible(false);

        finalScoreLabel = new Label("FINAL SCORE: 0", game.getUiManager().getSkin(), "default");
        distanceLabel = new Label("DISTANCE SURVIVED: 0m", game.getUiManager().getSkin(), "default");

        TextButton restartBtn = new TextButton("RETRY PURSUIT", game.getUiManager().getSkin());
        TextButton menuBtn = new TextButton("MAIN MENU", game.getUiManager().getSkin(), "danger");

        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getGameManager().restart();
                game.getStateMachine().transitionTo(GameState.PLAYING);
            }
        });

        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.MAIN_MENU);
            }
        });

        modalBox.add(titleLabel).padBottom(10).row();
        modalBox.add(recordBadgeLabel).padBottom(20).row();
        modalBox.add(finalScoreLabel).padBottom(10).row();
        modalBox.add(distanceLabel).padBottom(30).row();

        modalBox.add(restartBtn).size(280, 60).padBottom(15).row();
        modalBox.add(menuBtn).size(280, 55).row();

        rootTable.add(modalBox);
    }

    public void setSessionResults(long score, float distance, boolean isNewRecord) {
        finalScoreLabel.setText(String.format("FINAL SCORE: %d", score));
        distanceLabel.setText(String.format("DISTANCE SURVIVED: %.0fm", distance));
        recordBadgeLabel.setVisible(isNewRecord);
    }

    public void update(float delta) {
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
