package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gackstone.chase.ChaseGame;
import com.gackstone.chase.audio.SoundRegistry;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameState;

/**
 * Garage / Car-Selection screen.
 *
 * <p>Displays all registered player cars in a vertically scrollable list.
 * Each entry shows:
 * <ul>
 *   <li>Car name and flavour description
 *   <li>Stat bars: Speed, Acceleration, Handling, Armour
 *   <li>Price / unlock status
 *   <li>SELECT button (disabled when locked and insufficient coins)
 * </ul>
 *
 * <p>Selected car is persisted via {@link com.gackstone.chase.save.SaveManager}
 * and applied to the player entity immediately.
 */
public class GarageScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage stage;

    /** Currently highlighted car definition in the list. */
    private CarDefinition selectedCar;

    public GarageScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));
        selectedCar = CarRegistry.getPlayerCars().first();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        buildUI();
    }

    private void buildUI() {
        // ── Root layout ───────────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.pad(20);
        stage.addActor(root);

        // Title row
        Label title = new Label("GARAGE", game.getUiManager().getSkin(), "title");
        TextButton backBtn = new TextButton("< BACK", game.getUiManager().getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.MAIN_MENU);
            }
        });

        root.add(title).expandX().left().padBottom(4);
        root.add(backBtn).size(120, 44).right().padBottom(4).row();

        Label coins = new Label("COINS: " + game.getSaveManager().getData().getCoins(),
                game.getUiManager().getSkin(), "default");
        root.add(coins).colspan(2).left().padBottom(18).row();

        // ── Car list (scrollable) ─────────────────────────────────────────────
        Table listTable = new Table();
        listTable.top().padLeft(4).padRight(4);

        Array<CarDefinition> cars = CarRegistry.getPlayerCars();
        for (int i = 0; i < cars.size; i++) {
            listTable.add(buildCarCard(cars.get(i))).expandX().fillX().padBottom(16).row();
        }

        ScrollPane scroll = new ScrollPane(listTable, game.getUiManager().getSkin());
        scroll.setFadeScrollBars(false);
        root.add(scroll).colspan(2).expand().fill().row();

        // ── Bottom: Go to ENV selection ───────────────────────────────────────
        TextButton playBtn = new TextButton("SELECT TRACK  >", game.getUiManager().getSkin());
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.ENV_SELECT);
            }
        });
        root.add(playBtn).colspan(2).size(320, 58).padTop(14);
    }

    /** Builds a single car-card widget for the scrollable list. */
    private Table buildCarCard(CarDefinition car) {
        Table card = new Table();
        card.pad(12);

        boolean isUnlocked = car.isUnlockedByDefault() ||
                game.getSaveManager().getData().getCoins() >= car.getUnlockPrice();
        boolean isSelected = car.getId().equals(
                game.getSaveManager().getData().getSelectedCarId());

        // Header
        String nameText = isSelected ? "► " + car.getDisplayName() : car.getDisplayName();
        Color  nameColor = isUnlocked ? Color.WHITE : Color.GRAY;
        Label  nameLabel = new Label(nameText, game.getUiManager().getSkin(), "default");
        nameLabel.setColor(nameColor);
        card.add(nameLabel).left().padBottom(2).row();

        Label descLabel = new Label(car.getDescription(), game.getUiManager().getSkin(), "default");
        descLabel.setColor(new Color(0.75f, 0.75f, 0.78f, 1.0f));
        card.add(descLabel).left().padBottom(6).row();

        // Stats table
        Table stats = new Table();
        stats.defaults().padRight(8).padBottom(4);
        stats.add(statBar("SPEED",  car.getMaxSpeed(),   55.0f, Color.CYAN,   game)).left().row();
        stats.add(statBar("ACCEL",  car.getAcceleration(),25.0f, Color.GREEN,  game)).left().row();
        stats.add(statBar("HANDLE", car.getHandling(),    25.0f, Color.YELLOW, game)).left().row();
        stats.add(statBar("ARMOUR", car.getMaxHealth(),  220.0f, Color.RED,    game)).left().row();
        card.add(stats).left().padBottom(8).row();

        // Price / Select button
        Table btnRow = new Table();
        if (!car.isUnlockedByDefault()) {
            String priceText = isUnlocked ? "OWNED" : car.getUnlockPrice() + " COINS";
            btnRow.add(new Label(priceText, game.getUiManager().getSkin(), "default"))
                  .padRight(12);
        }

        TextButton selectBtn = new TextButton(isSelected ? "SELECTED" : "SELECT",
                game.getUiManager().getSkin());
        selectBtn.setDisabled(isSelected);
        selectBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (isUnlocked) {
                    selectCar(car);
                }
            }
        });
        btnRow.add(selectBtn).size(140, 44);
        card.add(btnRow).left();

        return card;
    }

    private Table statBar(String label, float value, float maxVal, Color color, ChaseGame game) {
        Table row = new Table();
        row.add(new Label(label, game.getUiManager().getSkin(), "default")).width(70).left();

        // Use ProgressBar as a visual stat bar
        ProgressBar.ProgressBarStyle pbStyle = new ProgressBar.ProgressBarStyle();
        pbStyle.background = game.getUiManager().getSkin().getDrawable("white");
        pbStyle.knob       = game.getUiManager().getSkin().getDrawable("white");
        pbStyle.knobBefore = game.getUiManager().getSkin().getDrawable("white");

        ProgressBar bar = new ProgressBar(0, maxVal, 1, false, pbStyle);
        bar.setValue(value);
        bar.setColor(color);
        row.add(bar).width(180).height(14).padLeft(8);
        return row;
    }

    private void selectCar(CarDefinition car) {
        game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
        selectedCar = car;
        game.getSaveManager().getData().setSelectedCarId(car.getId());
        game.getSaveManager().save();

        // Apply to runtime player immediately if game session exists
        if (game.getGameManager() != null) {
            game.getGameManager().selectPlayerCar(car);
        }

        // Refresh UI
        stage.clear();
        buildUI();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.11f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
