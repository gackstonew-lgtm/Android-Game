package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
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
 * In-game pause modal dialog allowing RESUME, RESTART, and MAIN MENU navigation.
 */
public class PauseMenuScreen implements Disposable {

    private final ChaseGame game;
    private final Stage stage;

    public PauseMenuScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));

        setupUI();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();
        stage.addActor(rootTable);

        // Modal Frame Box
        Table modalBox = new Table();
        modalBox.pad(30);

        Label titleLabel = new Label("GAME PAUSED", game.getUiManager().getSkin(), "title");

        TextButton resumeBtn = new TextButton("RESUME", game.getUiManager().getSkin());
        TextButton restartBtn = new TextButton("RESTART", game.getUiManager().getSkin());
        TextButton menuBtn = new TextButton("MAIN MENU", game.getUiManager().getSkin(), "danger");

        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.PLAYING);
            }
        });

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

        modalBox.add(titleLabel).padBottom(30).row();
        modalBox.add(resumeBtn).size(260, 55).padBottom(15).row();
        modalBox.add(restartBtn).size(260, 55).padBottom(15).row();
        modalBox.add(menuBtn).size(260, 55).row();

        rootTable.add(modalBox);
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
