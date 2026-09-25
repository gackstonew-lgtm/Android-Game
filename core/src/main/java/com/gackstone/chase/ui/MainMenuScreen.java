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
import com.gackstone.chase.save.GamePreferencesData;

/**
 * Main Menu Screen offering PLAY → GARAGE → SETTINGS → EXIT navigation,
 * plus best score and distance statistics with safe state machine transitions.
 */
public class MainMenuScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage     stage;

    public MainMenuScreen(ChaseGame game) {
        this.game  = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel    = new Label("CHASE", game.getUiManager().getSkin(), "title");
        Label subtitleLabel = new Label("HIGH SPEED 3D PURSUIT", game.getUiManager().getSkin(), "default");

        long  highScore = 0;
        float highDist  = 0;
        int   coins     = 0;
        if (game.getSaveManager() != null && game.getSaveManager().getData() != null) {
            GamePreferencesData data = game.getSaveManager().getData();
            highScore = data.getHighScore();
            highDist  = data.getHighestDistance();
            coins     = data.getCoins();
        }

        Label statsLabel = new Label(
                String.format("BEST: %d pts  |  %.0fm  |  COINS: %d", highScore, highDist, coins),
                game.getUiManager().getSkin(), "default");

        TextButton playBtn     = new TextButton("PLAY",     game.getUiManager().getSkin());
        TextButton garageBtn   = new TextButton("GARAGE",   game.getUiManager().getSkin());
        TextButton settingsBtn = new TextButton("SETTINGS", game.getUiManager().getSkin());
        TextButton exitBtn     = new TextButton("EXIT",     game.getUiManager().getSkin(), "danger");

        // PLAY routes through Garage so player can select car & track
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                if (game.getStateMachine() != null) {
                    game.getStateMachine().transitionTo(GameState.GARAGE);
                }
            }
        });

        garageBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                if (game.getStateMachine() != null) {
                    game.getStateMachine().transitionTo(GameState.GARAGE);
                }
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                if (game.getStateMachine() != null) {
                    game.getStateMachine().transitionTo(GameState.SETTINGS);
                }
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                Gdx.app.exit();
            }
        });

        root.add(titleLabel).padTop(40).padBottom(5).row();
        root.add(subtitleLabel).padBottom(30).row();
        root.add(statsLabel).padBottom(40).row();
        root.add(playBtn).size(280, 60).padBottom(15).row();
        root.add(garageBtn).size(280, 55).padBottom(15).row();
        root.add(settingsBtn).size(280, 55).padBottom(15).row();
        root.add(exitBtn).size(280, 55).padBottom(30).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.11f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
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
