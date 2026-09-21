package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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
 * Settings and Configuration screen managing audio balance, graphics presets,
 * and developer metrics overlay.
 */
public class SettingsScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage stage;

    public SettingsScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();

        final GamePreferencesData prefs = game.getSaveManager().getData();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center().pad(20);
        stage.addActor(rootTable);

        Label titleLabel = new Label("SETTINGS", game.getUiManager().getSkin(), "title");

        // Audio controls
        Label masterVolLabel = new Label("MASTER VOLUME", game.getUiManager().getSkin(), "default");
        Slider masterVolSlider = new Slider(0.0f, 1.0f, 0.05f, false, game.getUiManager().getSkin(), "default-horizontal");
        masterVolSlider.setValue(prefs.getMasterVolume());

        Label sfxVolLabel = new Label("SFX VOLUME", game.getUiManager().getSkin(), "default");
        Slider sfxVolSlider = new Slider(0.0f, 1.0f, 0.05f, false, game.getUiManager().getSkin(), "default-horizontal");
        sfxVolSlider.setValue(prefs.getSfxVolume());

        // Graphics quality toggle button
        Label graphicsLabel = new Label("GRAPHICS PRESET", game.getUiManager().getSkin(), "default");
        TextButton graphicsBtn = new TextButton(prefs.getGraphicsQuality().name(), game.getUiManager().getSkin());

        // Debug toggle button
        Label debugLabel = new Label("DEBUG OVERLAY", game.getUiManager().getSkin(), "default");
        TextButton debugBtn = new TextButton(prefs.isDebugOverlayEnabled() ? "ENABLED" : "DISABLED", game.getUiManager().getSkin());

        // Back button
        TextButton backBtn = new TextButton("SAVE & BACK", game.getUiManager().getSkin());

        masterVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setMasterVolume(masterVolSlider.getValue());
                game.getAudioManager().setMasterVolume(masterVolSlider.getValue());
            }
        });

        sfxVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setSfxVolume(sfxVolSlider.getValue());
                game.getAudioManager().setSfxVolume(sfxVolSlider.getValue());
            }
        });

        graphicsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                GameConfig.GraphicsQuality current = prefs.getGraphicsQuality();
                GameConfig.GraphicsQuality next;
                if (current == GameConfig.GraphicsQuality.LOW) {
                    next = GameConfig.GraphicsQuality.MEDIUM;
                } else if (current == GameConfig.GraphicsQuality.MEDIUM) {
                    next = GameConfig.GraphicsQuality.HIGH;
                } else {
                    next = GameConfig.GraphicsQuality.LOW;
                }
                prefs.setGraphicsQuality(next);
                graphicsBtn.setText(next.name());
            }
        });

        debugBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                boolean newEnabled = !prefs.isDebugOverlayEnabled();
                prefs.setDebugOverlayEnabled(newEnabled);
                debugBtn.setText(newEnabled ? "ENABLED" : "DISABLED");
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getSaveManager().save();
                game.getStateMachine().transitionTo(game.getStateMachine().getPreviousState() == GameState.PAUSED 
                    ? GameState.PAUSED : GameState.MAIN_MENU);
            }
        });

        // Layout
        rootTable.add(titleLabel).colspan(2).padBottom(30).row();

        rootTable.add(masterVolLabel).left().padRight(20).padBottom(15);
        rootTable.add(masterVolSlider).width(250).padBottom(15).row();

        rootTable.add(sfxVolLabel).left().padRight(20).padBottom(15);
        rootTable.add(sfxVolSlider).width(250).padBottom(15).row();

        rootTable.add(graphicsLabel).left().padRight(20).padBottom(15);
        rootTable.add(graphicsBtn).width(250).height(45).padBottom(15).row();

        rootTable.add(debugLabel).left().padRight(20).padBottom(25);
        rootTable.add(debugBtn).width(250).height(45).padBottom(25).row();

        rootTable.add(backBtn).colspan(2).size(260, 55).padTop(10).row();
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
