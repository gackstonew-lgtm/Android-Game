package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gackstone.chase.ChaseGame;
import com.gackstone.chase.audio.SoundRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameState;
import com.gackstone.chase.world.EnvironmentRegistry;
import com.gackstone.chase.world.EnvironmentTheme;

/**
 * Environment / Track-Theme selection screen.
 *
 * <p>Shows all registered {@link EnvironmentTheme}s with their name, description,
 * and atmospheric colour swatch. The selected theme is persisted and applied
 * to the world renderer when a new game starts.
 */
public class EnvSelectScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage     stage;

    private String selectedThemeId;

    public EnvSelectScreen(ChaseGame game) {
        this.game  = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
        if (game.getSaveManager() != null && game.getSaveManager().getData() != null) {
            selectedThemeId = game.getSaveManager().getData().getSelectedThemeId();
        }
        if (selectedThemeId == null || selectedThemeId.isEmpty()) {
            selectedThemeId = EnvironmentRegistry.THEME_NEON_CITY;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(20);
        stage.addActor(root);

        Label title = new Label("SELECT TRACK", game.getUiManager().getSkin(), "title");
        TextButton backBtn = new TextButton("< GARAGE", game.getUiManager().getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                if (game.getStateMachine() != null) {
                    game.getStateMachine().transitionTo(GameState.GARAGE);
                }
            }
        });

        root.add(title).expandX().left().padBottom(20);
        root.add(backBtn).size(120, 44).right().padBottom(20).row();

        Array<EnvironmentTheme> themes = EnvironmentRegistry.getThemes();
        for (int i = 0; i < themes.size; i++) {
            root.add(buildThemeCard(themes.get(i))).colspan(2).expandX().fillX().padBottom(18).row();
        }

        TextButton playBtn = new TextButton("PLAY  ▶", game.getUiManager().getSkin());
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                }
                applySelectionAndPlay();
            }
        });
        root.add(playBtn).colspan(2).size(300, 58).padTop(10);
    }

    private Table buildThemeCard(EnvironmentTheme theme) {
        boolean isSelected = theme.getId().equals(selectedThemeId);

        Table card = new Table();
        card.pad(14);

        String nameText = isSelected ? "► " + theme.getName() : theme.getName();
        Label name = new Label(nameText, game.getUiManager().getSkin(), isSelected ? "title" : "default");
        card.add(name).left().padBottom(4).row();

        Label desc = new Label(theme.getDescription(), game.getUiManager().getSkin(), "default");
        card.add(desc).left().padBottom(8).row();

        // Atmosphere colour swatch using fog colour
        Color fogCol = theme.getFogColor();
        Label swatch = new Label("  FOG: " + toHex(fogCol) + "  |  DIST: "
                + (int) theme.getFogNear() + "–" + (int) theme.getFogFar() + "m",
                game.getUiManager().getSkin(), "default");
        swatch.setColor(theme.getSkyClearColor());
        card.add(swatch).left().padBottom(8).row();

        TextButton selectBtn = new TextButton(isSelected ? "SELECTED" : "SELECT",
                game.getUiManager().getSkin());
        selectBtn.setDisabled(isSelected);
        selectBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                selectTheme(theme.getId());
            }
        });
        card.add(selectBtn).size(150, 44).left();

        return card;
    }

    private void selectTheme(String themeId) {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
        }
        selectedThemeId = themeId;
        if (game.getSaveManager() != null && game.getSaveManager().getData() != null) {
            game.getSaveManager().getData().setSelectedThemeId(themeId);
            game.getSaveManager().save();
        }

        // Apply immediately to running world renderer if session exists
        if (game.getGameManager() != null) {
            game.getGameManager().selectEnvironment(themeId);
        }

        stage.clear();
        buildUI();
    }

    private void applySelectionAndPlay() {
        if (game.getGameManager() != null) {
            game.getGameManager().selectEnvironment(selectedThemeId);
        }
        if (game.getStateMachine() != null) {
            game.getStateMachine().transitionTo(GameState.PLAYING);
        }
    }

    /** Convert a libGDX Color to a compact hex string for display. */
    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.r * 255), (int)(c.g * 255), (int)(c.b * 255));
    }

    @Override
    public void render(float delta) {
        EnvironmentTheme currentTheme = EnvironmentRegistry.getById(selectedThemeId);
        Color sky = (currentTheme != null) ? currentTheme.getSkyClearColor() : Color.DARK_GRAY;
        Gdx.gl.glClearColor(sky.r * 0.3f, sky.g * 0.3f, sky.b * 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide()               { Gdx.input.setInputProcessor(null); }
    @Override public void dispose()            { stage.dispose(); }
}
