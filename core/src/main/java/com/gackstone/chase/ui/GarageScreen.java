package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gackstone.chase.ChaseGame;
import com.gackstone.chase.audio.SoundRegistry;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.core.GameState;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Premium Mobile Racing Garage & Customization Screen.
 *
 * <p>Layout & Hierarchy (matching reference design):
 * <ul>
 *   <li><b>Center 3D Studio:</b> Prominent real-time 3D car viewport with studio lighting and touch orbit.
 *   <li><b>Top Bar:</b> Title badge ("MODIFICATION"), currency pill counter, and close button.
 *   <li><b>Left Rail:</b> 2-column icon grid for 10 customization categories with vibrant orange selected states.
 *   <li><b>Right Telemetry:</b> 5-axis {@link RadarChartActor} spider chart with stock vs upgrade comparison.
 *   <li><b>Bottom Detail Panel:</b> Glassmorphism card with item badge, price pill, description, and primary CTA.
 *   <li><b>Bottom Carousel:</b> Horizontal pagination selector for vehicles and cosmetic presets.
 * </ul>
 */
public class GarageScreen extends ScreenAdapter {

    private final ChaseGame game;
    private final Stage stage;

    // 3D Car Viewport Renderer
    private final CarPreviewRenderer previewRenderer;

    // Right Radar Chart
    private RadarChartActor radarChart;

    // Active Selection State
    private CarDefinition selectedCar;
    private int selectedCarIndex = 0;
    private int activeCategoryIndex = 5; // Default: Livery / Decals (index 5 matching reference)
    private int activeCarouselIndex = 1; // Default item index

    // Category Definitions
    private static final String[] CATEGORY_NAMES = {
            "FRONT BUMPER", "REAR DIFFUSER", "SPOILER WING", "WHEELS & RIMS",
            "BODY PAINT",   "LIVERY & DECALS", "DRIVETRAIN",   "ENGINE TUNING",
            "TRANSMISSION", "BRAKE SYSTEM"
    };

    private static final String[] CATEGORY_ICON_KEYS = {
            "icon-bumper", "icon-diffuser", "icon-spoiler", "icon-wheel",
            "icon-paint",  "icon-livery",   "icon-chassis", "icon-engine",
            "icon-gear",   "icon-brake"
    };

    // UI Dynamic References for zero-allocation rebuilds
    private Table leftRailTable;
    private Table detailCardTable;
    private Table carouselTable;
    private Label currencyLabel;

    public GarageScreen(ChaseGame game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT));

        // Initialize 3D Preview Renderer with ModelRegistry
        this.previewRenderer = new CarPreviewRenderer(game.getGameManager().getModelRegistry());

        // Resolve current saved car selection
        String savedCarId = game.getSaveManager().getData().getSelectedCarId();
        Array<CarDefinition> cars = CarRegistry.getPlayerCars();
        selectedCar = CarRegistry.getById(savedCarId);
        for (int i = 0; i < cars.size; i++) {
            if (cars.get(i).getId().equals(selectedCar.getId())) {
                selectedCarIndex = i;
                break;
            }
        }

        previewRenderer.setCar(selectedCar);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        buildUI();
        updateCarTelemetry();
    }

    private void buildUI() {
        // ── Full-screen Background Watermark Actor ────────────────────────────
        stage.addActor(new Actor() {
            private final BitmapFont bgFont = game.getUiManager().getDefaultFont();
            private final GlyphLayout layout = new GlyphLayout();

            @Override
            public void draw(Batch batch, float parentAlpha) {
                float oldScaleX = bgFont.getData().scaleX;
                float oldScaleY = bgFont.getData().scaleY;

                // Subtle studio background watermark text
                bgFont.getData().setScale(4.5f);
                bgFont.setColor(0.12f, 0.16f, 0.24f, 0.18f);
                layout.setText(bgFont, "AUTO MODDING");
                bgFont.draw(batch, layout,
                        (stage.getWidth() - layout.width) * 0.5f,
                        stage.getHeight() * 0.88f);

                // Footer watermark
                bgFont.getData().setScale(0.85f);
                bgFont.setColor(0.4f, 0.45f, 0.55f, 0.7f);
                bgFont.draw(batch, "* REAL-TIME 3D IN-ENGINE PREVIEW", 36, 28);

                bgFont.getData().setScale(oldScaleX, oldScaleY);
            }
        });

        // ── Interactive Touch Layer for 3D Car Orbit ───────────────────────────
        Table touchInterceptor = new Table();
        touchInterceptor.setFillParent(true);
        touchInterceptor.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Ignore touches over left rail and bottom panels
                if (x > 280 && x < stage.getWidth() - 280 && y > 180 && y < stage.getHeight() - 90) {
                    previewRenderer.handleTouchDown(x);
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                previewRenderer.handleTouchDragged(x);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                previewRenderer.handleTouchUp();
            }
        });
        stage.addActor(touchInterceptor);

        // ── Main UI Layout Structure ──────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(24);
        stage.addActor(root);

        // 1. TOP BAR ROW
        Table topBar = buildTopBar();
        root.add(topBar).expandX().fillX().colspan(2).padBottom(12).row();

        // 2. MIDDLE AREA (Left Rail + Right Radar Chart)
        Table middleArea = new Table();

        // Left Category Rail
        leftRailTable = new Table();
        buildCategoryRail(leftRailTable);
        middleArea.add(leftRailTable).width(250).top().left();

        // Center spacer for 3D car viewport
        middleArea.add().expandX().fillX();

        // Right Radar Chart + Quick Action Buttons
        Table rightTelemetryTable = buildRightTelemetry();
        middleArea.add(rightTelemetryTable).width(280).top().right();

        root.add(middleArea).expand().fill().colspan(2).row();

        // 3. BOTTOM ROW (Detail Panel + Carousel)
        Table bottomRow = buildBottomRow();
        root.add(bottomRow).expandX().fillX().colspan(2).padTop(8);
    }

    /**
     * Builds the top navigation bar with title badge, currency counter, and close button.
     */
    private Table buildTopBar() {
        Table topBar = new Table();

        // Top-Left: Garage Badge & Title
        Table titlePill = new Table();
        titlePill.setBackground(game.getUiManager().getSkin().getDrawable("badge-pill"));
        titlePill.pad(6, 14, 6, 18);

        Image gearIcon = new Image(game.getUiManager().getSkin().getDrawable("icon-gear"));
        gearIcon.setScaling(Scaling.fit);
        titlePill.add(gearIcon).size(22, 22).padRight(8);

        Label titleLabel = new Label("MODIFICATION", game.getUiManager().getSkin(), "sub-title");
        titleLabel.setColor(Color.WHITE);
        titlePill.add(titleLabel);

        topBar.add(titlePill).left();

        // Top spacer
        topBar.add().expandX();

        // Top-Right: Currency Counter Pill
        Table currencyPill = new Table();
        currencyPill.setBackground(game.getUiManager().getSkin().getDrawable("currency-pill"));
        currencyPill.pad(4, 12, 4, 16);

        Image coinIcon = new Image(game.getUiManager().getSkin().getDrawable("icon-coin"));
        coinIcon.setScaling(Scaling.fit);
        currencyPill.add(coinIcon).size(22, 22).padRight(8);

        long coins = game.getSaveManager().getData().getCoins();
        String formattedCoins = NumberFormat.getNumberInstance(Locale.US).format(coins);
        currencyLabel = new Label(formattedCoins, game.getUiManager().getSkin(), "currency");
        currencyPill.add(currencyLabel);

        topBar.add(currencyPill).padRight(14);

        // Top-Right: Close ("X") Button
        TextButton closeBtn = new TextButton("X", game.getUiManager().getSkin(), "close-btn");
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.MAIN_MENU);
            }
        });
        topBar.add(closeBtn).size(40, 40).right();

        return topBar;
    }

    /**
     * Builds the 2-column vertical category icon rail on the left.
     */
    private void buildCategoryRail(Table container) {
        container.clear();

        // Header Tab Pill
        Table headerPill = new Table();
        headerPill.setBackground(game.getUiManager().getSkin().getDrawable("badge-pill"));
        headerPill.pad(5, 12, 5, 12);
        Label headerLabel = new Label("MOD OPTIONS", game.getUiManager().getSkin(), "badge");
        headerLabel.setColor(new Color(0.8f, 0.85f, 0.95f, 1.0f));
        headerPill.add(headerLabel);
        container.add(headerPill).expandX().left().padBottom(10).row();

        // 2-Column Grid for Category Icons
        Table grid = new Table();
        grid.defaults().size(56, 56).pad(4);

        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            final int catIdx = i;
            boolean isSelected = (i == activeCategoryIndex);

            Button catBtn = new Button(game.getUiManager().getSkin(),
                    isSelected ? "category-btn-selected" : "category-btn-normal");

            Image iconImg = new Image(game.getUiManager().getSkin().getDrawable(CATEGORY_ICON_KEYS[i]));
            iconImg.setScaling(Scaling.fit);
            if (isSelected) {
                iconImg.setColor(new Color(0.08f, 0.10f, 0.15f, 1.0f)); // Dark icon on orange
            } else {
                iconImg.setColor(Color.WHITE);
            }

            catBtn.add(iconImg).size(34, 34);
            catBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                    activeCategoryIndex = catIdx;
                    buildCategoryRail(leftRailTable);
                    refreshDetailCard();
                    refreshCarousel();
                }
            });

            grid.add(catBtn);
            if (i % 2 == 1) {
                grid.row();
            }
        }

        container.add(grid).left();
    }

    /**
     * Builds the right-side 5-axis spider chart telemetry panel and floating quick action buttons.
     */
    private Table buildRightTelemetry() {
        Table table = new Table();
        table.top().right();

        // 5-Axis Spider Chart Actor
        radarChart = new RadarChartActor(game.getUiManager().getSmallFont());
        table.add(radarChart).size(250, 250).padBottom(12).row();

        // Quick Action Floating Buttons (360 Spin & Info Checklist)
        Table quickActions = new Table();
        quickActions.defaults().size(44, 44).pad(6);

        // Spin 360 Button
        Button rotateBtn = new Button(game.getUiManager().getSkin(), "circle-action");
        Image rotIcon = new Image(game.getUiManager().getSkin().getDrawable("icon-rotate"));
        rotIcon.setScaling(Scaling.fit);
        rotateBtn.add(rotIcon).size(26, 26);
        rotateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                previewRenderer.triggerSpin360();
            }
        });
        quickActions.add(rotateBtn);

        // Spec / Info Button
        Button infoBtn = new Button(game.getUiManager().getSkin(), "circle-action");
        Image infoIcon = new Image(game.getUiManager().getSkin().getDrawable("icon-info"));
        infoIcon.setScaling(Scaling.fit);
        infoBtn.add(infoIcon).size(24, 24);
        infoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                // Toggle upgrade preview boost in radar chart
                toggleUpgradePreview();
            }
        });
        quickActions.add(infoBtn);

        table.add(quickActions).right();

        return table;
    }

    /**
     * Builds the bottom row containing the glassmorphic detail card and the item carousel.
     */
    private Table buildBottomRow() {
        Table bottom = new Table();
        bottom.left().bottom();

        // Left: Item Detail Card
        detailCardTable = new Table();
        detailCardTable.setBackground(game.getUiManager().getSkin().getDrawable("glass-panel"));
        detailCardTable.pad(14, 18, 14, 18);
        refreshDetailCard();
        bottom.add(detailCardTable).width(360).height(145).left().padRight(16);

        // Center / Right: Horizontal Item Carousel
        carouselTable = new Table();
        refreshCarousel();
        bottom.add(carouselTable).expandX().left();

        return bottom;
    }

    /**
     * Populates the detail card with the current item name, price pill, description, and primary button.
     */
    private void refreshDetailCard() {
        detailCardTable.clear();

        Array<CarDefinition> cars = CarRegistry.getPlayerCars();
        CarDefinition activeCar = cars.get(selectedCarIndex);

        boolean isOwned = activeCar.isUnlockedByDefault() ||
                game.getSaveManager().getData().getCoins() >= activeCar.getUnlockPrice();
        boolean isCurrentSelected = activeCar.getId().equals(
                game.getSaveManager().getData().getSelectedCarId());

        // Header Row: Item Name Badge + Price Pill
        Table headerRow = new Table();

        Table nameBadge = new Table();
        nameBadge.setBackground(game.getUiManager().getSkin().getDrawable("badge-pill"));
        nameBadge.pad(3, 10, 3, 10);
        String displayName = activeCar.getDisplayName();
        if (activeCategoryIndex == 5) {
            displayName += " - Neon Mirage";
        }
        Label nameLabel = new Label(displayName, game.getUiManager().getSkin(), "badge");
        nameLabel.setColor(Color.WHITE);
        nameBadge.add(nameLabel);
        headerRow.add(nameBadge).left();

        headerRow.add().expandX();

        // Price Pill
        Table pricePill = new Table();
        pricePill.setBackground(game.getUiManager().getSkin().getDrawable("currency-pill"));
        pricePill.pad(3, 8, 3, 10);
        Image miniCoin = new Image(game.getUiManager().getSkin().getDrawable("icon-coin"));
        pricePill.add(miniCoin).size(16, 16).padRight(4);

        String priceText = activeCar.isUnlockedByDefault() ? "STOCK" :
                (isOwned ? "OWNED" : activeCar.getUnlockPrice() + " COINS");
        Label priceLabel = new Label(priceText, game.getUiManager().getSkin(), "badge");
        priceLabel.setColor(new Color(1.0f, 0.85f, 0.3f, 1.0f));
        pricePill.add(priceLabel);
        headerRow.add(pricePill).right();

        detailCardTable.add(headerRow).expandX().fillX().padBottom(6).row();

        // Descriptive Flavour Text
        Label descLabel = new Label(activeCar.getDescription(), game.getUiManager().getSkin(), "desc");
        descLabel.setWrap(true);
        detailCardTable.add(descLabel).expandX().fillX().padBottom(8).row();

        // Action Buttons Row: Select Car / Buy / Select Track
        Table actionRow = new Table();

        TextButton selectBtn = new TextButton(isCurrentSelected ? "INSTALLED" : "SELECT",
                game.getUiManager().getSkin(), "primary-action");
        selectBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyCarSelection(activeCar);
            }
        });
        actionRow.add(selectBtn).size(130, 38).left().padRight(12);

        TextButton trackBtn = new TextButton("SELECT TRACK >",
                game.getUiManager().getSkin());
        trackBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                game.getStateMachine().transitionTo(GameState.ENV_SELECT);
            }
        });
        actionRow.add(trackBtn).size(150, 38).left();

        detailCardTable.add(actionRow).left();
    }

    /**
     * Refreshes the bottom item selector carousel with left/right arrows and selectable cards.
     */
    private void refreshCarousel() {
        carouselTable.clear();

        // Left Arrow Button
        TextButton prevBtn = new TextButton("<", game.getUiManager().getSkin());
        prevBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                cycleCar(-1);
            }
        });
        carouselTable.add(prevBtn).size(36, 68).padRight(8);

        // Selectable Item Cards
        Array<CarDefinition> cars = CarRegistry.getPlayerCars();
        for (int i = 0; i < cars.size; i++) {
            final int carIdx = i;
            CarDefinition car = cars.get(i);
            boolean isSelected = (i == selectedCarIndex);

            Button cardBtn = new Button(game.getUiManager().getSkin(),
                    isSelected ? "card-selected" : "card-normal");
            cardBtn.pad(6);

            Table cardContent = new Table();

            // Card Thumbnail Icon
            String iconKey = (i == 0) ? "icon-none" : (i == 1 ? "icon-livery" : "icon-wheel");
            Image itemIcon = new Image(game.getUiManager().getSkin().getDrawable(iconKey));
            itemIcon.setScaling(Scaling.fit);
            if (isSelected) {
                itemIcon.setColor(new Color(1.0f, 0.75f, 0.2f, 1.0f));
            } else {
                itemIcon.setColor(Color.WHITE);
            }
            cardContent.add(itemIcon).size(42, 42).padBottom(4).row();

            // Card Mini Label
            Label nameLbl = new Label(car.getDisplayName().split(" ")[0],
                    game.getUiManager().getSkin(), "badge");
            nameLbl.setColor(isSelected ? Color.WHITE : Color.LIGHT_GRAY);
            cardContent.add(nameLbl);

            cardBtn.add(cardContent);

            cardBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                    selectedCarIndex = carIdx;
                    selectedCar = cars.get(carIdx);
                    previewRenderer.setCar(selectedCar);
                    updateCarTelemetry();
                    refreshDetailCard();
                    refreshCarousel();
                }
            });

            carouselTable.add(cardBtn).size(78, 78).padRight(8);
        }

        // Right Arrow Button
        TextButton nextBtn = new TextButton(">", game.getUiManager().getSkin());
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
                cycleCar(1);
            }
        });
        carouselTable.add(nextBtn).size(36, 68).padLeft(4);
    }

    private void cycleCar(int direction) {
        Array<CarDefinition> cars = CarRegistry.getPlayerCars();
        selectedCarIndex = (selectedCarIndex + direction + cars.size) % cars.size;
        selectedCar = cars.get(selectedCarIndex);
        previewRenderer.setCar(selectedCar);
        updateCarTelemetry();
        refreshDetailCard();
        refreshCarousel();
    }

    private void updateCarTelemetry() {
        if (radarChart == null || selectedCar == null) return;

        // Normalize stats to [0.2 - 1.0] range for clear radar chart visualization
        float torque = selectedCar.getAcceleration() / 25.0f;
        float weight = selectedCar.getMass() / 2600.0f;
        float grip   = selectedCar.getHandling() / 25.0f;
        float brake  = selectedCar.getMaxHealth() / 250.0f;
        float speed  = selectedCar.getMaxSpeed() / 55.0f;

        radarChart.setStats(torque, weight, grip, brake, speed);

        // Preview upgraded potential specs (+15% tuning)
        radarChart.setUpgradePreview(
                Math.min(1.0f, torque * 1.18f),
                Math.min(1.0f, weight * 0.92f),
                Math.min(1.0f, grip * 1.15f),
                Math.min(1.0f, brake * 1.12f),
                Math.min(1.0f, speed * 1.16f)
        );
    }

    private void toggleUpgradePreview() {
        updateCarTelemetry();
    }

    private void applyCarSelection(CarDefinition car) {
        game.getAudioManager().playSound(SoundRegistry.SND_UI_CLICK, SoundRegistry.AudioCategory.UI);
        selectedCar = car;
        game.getSaveManager().getData().setSelectedCarId(car.getId());
        game.getSaveManager().save();

        if (game.getGameManager() != null) {
            game.getGameManager().selectPlayerCar(car);
        }

        refreshDetailCard();
        refreshCarousel();
    }

    @Override
    public void render(float delta) {
        // 1. Dark futuristic background clear
        Gdx.gl.glClearColor(0.04f, 0.06f, 0.09f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 2. Update & Render 3D Car Studio Pedestal
        previewRenderer.update(delta);
        previewRenderer.render();

        // 3. Render 2D Scene2D UI Stage
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        previewRenderer.resize(width, height);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        previewRenderer.dispose();
        if (radarChart != null) {
            radarChart.dispose();
        }
    }
}
