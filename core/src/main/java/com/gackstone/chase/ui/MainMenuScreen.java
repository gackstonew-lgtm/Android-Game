package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gackstone.chase.ChaseGame;
import com.gackstone.chase.audio.SoundRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameState;

/**
 * Main Menu Screen offering PLAY, SETTINGS, EXIT, and highest score statistics.
 */
public class MainMenuScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage stage;

    public MainMenuScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Title
        Label titleLabel = new Label("CHASE", game.getUiManager().getSkin(), "title");
        Label subtitleLabel = new Label("HIGH SPEED 3D PURSUIT", game.getUiManager().getSkin(), "default");

        // High score display
        long highScore = game.getSaveManager().getData().getHighScore();
        float highDist = game.getSaveManager().getData().getHighestDistance();
        Label statsLabel = new Label(String.format("BEST SCORE: %d   |   BEST DISTANCE: %.0fm", highScore, highDist), 
            game.getUiManager().getSkin(), "default");

        // Buttons
        TextButton playButton = new TextButton("PLAY", game.getUiManager().getSkin());
        TextButton settingsButton = new TextButton("SETTINGS", game.getUiManager().getSkin());
        TextButton exitButton = new TextButton("EXIT", game.getUiManager().getSkin(), "danger");

        // Listeners
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.PLAYING);
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.SETTINGS);
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                Gdx.app.exit();
            }
        });

        // Layout construction
        rootTable.add(titleLabel).padTop(40).padBottom(5).row();
        rootTable.add(subtitleLabel).padBottom(30).row();
        rootTable.add(statsLabel).padBottom(40).row();

        rootTable.add(playButton).size(280, 60).padBottom(15).row();
        rootTable.add(settingsButton).size(280, 55).padBottom(15).row();
        rootTable.add(exitButton).size(280, 55).padBottom(30).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.11f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
