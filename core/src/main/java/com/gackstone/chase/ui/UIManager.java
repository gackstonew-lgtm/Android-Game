package com.gackstone.chase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages responsive Scene2D skin, procedural stylized widgets, high-contrast
 * glassmorphic panels, category icons, and typography without requiring external
 * texture atlases.
 */
public class UIManager implements Disposable {

    private final Skin skin;
    private final BitmapFont titleFont;
    private final BitmapFont subTitleFont;
    private final BitmapFont defaultFont;
    private final BitmapFont smallFont;
    private final BitmapFont currencyFont;

    private final Array<Texture> managedTextures = new Array<>();

    public UIManager() {
        skin = new Skin();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.2f);

        subTitleFont = new BitmapFont();
        subTitleFont.getData().setScale(1.7f);

        defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.3f);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.95f);

        currencyFont = new BitmapFont();
        currencyFont.getData().setScale(1.5f);

        skin.add("title-font", titleFont);
        skin.add("sub-title-font", subTitleFont);
        skin.add("default-font", defaultFont);
        skin.add("small-font", smallFont);
        skin.add("currency-font", currencyFont);

        generateProceduralSkin();
    }

    private void generateProceduralSkin() {
        // 1. Base solid colors
        Texture texWhite = createSolidTexture(1, 1, Color.WHITE);
        skin.add("white", texWhite);

        Texture texTransparent = createSolidTexture(1, 1, new Color(0, 0, 0, 0));
        skin.add("transparent", texTransparent);

        // 2. Glassmorphism Panels & NinePatches
        // Standard Dark Glass Panel
        Texture texGlassPanel = createRoundedRectTexture(24, 24, 4,
                new Color(0.06f, 0.09f, 0.14f, 0.82f),
                new Color(0.20f, 0.30f, 0.45f, 0.70f), 1);
        NinePatch patchGlass = new NinePatch(texGlassPanel, 6, 6, 6, 6);
        skin.add("glass-panel", new NinePatchDrawable(patchGlass));

        // Darker Card Glass Panel
        Texture texGlassCard = createRoundedRectTexture(24, 24, 4,
                new Color(0.04f, 0.06f, 0.10f, 0.88f),
                new Color(0.28f, 0.40f, 0.60f, 0.80f), 1);
        NinePatch patchGlassCard = new NinePatch(texGlassCard, 6, 6, 6, 6);
        skin.add("glass-card", new NinePatchDrawable(patchGlassCard));

        // Selected Orange Panel (with vibrant amber-orange fill)
        Texture texOrangePanel = createRoundedRectTexture(24, 24, 4,
                new Color(1.0f, 0.62f, 0.10f, 0.95f),
                new Color(1.0f, 0.85f, 0.35f, 1.0f), 1);
        NinePatch patchOrange = new NinePatch(texOrangePanel, 6, 6, 6, 6);
        skin.add("orange-panel", new NinePatchDrawable(patchOrange));

        // Selected Orange Border Panel (dark interior, orange glowing border)
        Texture texOrangeBorderPanel = createRoundedRectTexture(24, 24, 4,
                new Color(0.08f, 0.10f, 0.16f, 0.90f),
                new Color(1.0f, 0.62f, 0.10f, 1.0f), 2);
        NinePatch patchOrangeBorder = new NinePatch(texOrangeBorderPanel, 6, 6, 6, 6);
        skin.add("orange-border-panel", new NinePatchDrawable(patchOrangeBorder));

        // Capsule Badge Panel (Top headers / pills)
        Texture texBadgePill = createRoundedRectTexture(24, 24, 8,
                new Color(0.10f, 0.14f, 0.20f, 0.92f),
                new Color(0.35f, 0.48f, 0.65f, 0.85f), 1);
        NinePatch patchBadge = new NinePatch(texBadgePill, 8, 8, 8, 8);
        skin.add("badge-pill", new NinePatchDrawable(patchBadge));

        // Currency Pill Badge (Gold accent border)
        Texture texCurrencyPill = createRoundedRectTexture(24, 24, 8,
                new Color(0.07f, 0.10f, 0.15f, 0.92f),
                new Color(1.0f, 0.75f, 0.20f, 0.90f), 1);
        NinePatch patchCurrency = new NinePatch(texCurrencyPill, 8, 8, 8, 8);
        skin.add("currency-pill", new NinePatchDrawable(patchCurrency));

        // Primary Action Button Fill (High contrast white-cyan pill)
        Texture texPrimaryBtn = createRoundedRectTexture(24, 24, 10,
                new Color(0.95f, 0.97f, 1.0f, 1.0f),
                new Color(0.40f, 0.80f, 1.0f, 1.0f), 1);
        Texture texPrimaryBtnDown = createRoundedRectTexture(24, 24, 10,
                new Color(0.60f, 0.85f, 1.0f, 1.0f),
                new Color(0.20f, 0.60f, 0.9f, 1.0f), 1);
        NinePatch patchPrimaryBtn = new NinePatch(texPrimaryBtn, 10, 10, 10, 10);
        NinePatch patchPrimaryBtnDown = new NinePatch(texPrimaryBtnDown, 10, 10, 10, 10);
        skin.add("primary-btn", new NinePatchDrawable(patchPrimaryBtn));
        skin.add("primary-btn-down", new NinePatchDrawable(patchPrimaryBtnDown));

        // 3. Category & Utility Icons
        createProceduralIcons();

        // 4. Button Styles
        // Default Button Style
        Texture texBtn = createRoundedRectTexture(16, 16, 3,
                new Color(0.12f, 0.16f, 0.24f, 0.95f),
                new Color(0.0f, 0.85f, 1.0f, 0.9f), 1);
        Texture texBtnDown = createRoundedRectTexture(16, 16, 3,
                new Color(0.0f, 0.6f, 0.8f, 1.0f),
                new Color(0.0f, 0.9f, 1.0f, 1.0f), 1);
        NinePatch patchBtn = new NinePatch(texBtn, 4, 4, 4, 4);
        NinePatch patchBtnDown = new NinePatch(texBtnDown, 4, 4, 4, 4);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new NinePatchDrawable(patchBtn);
        btnStyle.down = new NinePatchDrawable(patchBtnDown);
        btnStyle.font = defaultFont;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.downFontColor = Color.BLACK;
        skin.add("default", btnStyle);

        // Primary Select / Action Button Style (White pill with bold dark text)
        TextButton.TextButtonStyle primaryActionStyle = new TextButton.TextButtonStyle();
        primaryActionStyle.up = new NinePatchDrawable(patchPrimaryBtn);
        primaryActionStyle.down = new NinePatchDrawable(patchPrimaryBtnDown);
        primaryActionStyle.font = defaultFont;
        primaryActionStyle.fontColor = new Color(0.08f, 0.12f, 0.18f, 1.0f);
        primaryActionStyle.downFontColor = Color.BLACK;
        skin.add("primary-action", primaryActionStyle);

        // Category Icon Button Normal Style
        Button.ButtonStyle catNormalStyle = new Button.ButtonStyle();
        catNormalStyle.up = skin.getDrawable("glass-panel");
        catNormalStyle.down = skin.getDrawable("orange-panel");
        skin.add("category-btn-normal", catNormalStyle);

        // Category Icon Button Selected Style (Bright orange)
        Button.ButtonStyle catSelectedStyle = new Button.ButtonStyle();
        catSelectedStyle.up = skin.getDrawable("orange-panel");
        catSelectedStyle.down = skin.getDrawable("orange-panel");
        skin.add("category-btn-selected", catSelectedStyle);

        // Carousel Card Button Normal & Selected Styles
        Button.ButtonStyle cardNormalStyle = new Button.ButtonStyle();
        cardNormalStyle.up = skin.getDrawable("glass-card");
        cardNormalStyle.down = skin.getDrawable("orange-border-panel");
        skin.add("card-normal", cardNormalStyle);

        Button.ButtonStyle cardSelectedStyle = new Button.ButtonStyle();
        cardSelectedStyle.up = skin.getDrawable("orange-border-panel");
        cardSelectedStyle.down = skin.getDrawable("orange-border-panel");
        skin.add("card-selected", cardSelectedStyle);

        // Circular Close Button Style (Red accent)
        Texture texCloseUp = createCircleTexture(36, new Color(0.85f, 0.22f, 0.28f, 0.95f), Color.WHITE, 1);
        Texture texCloseDown = createCircleTexture(36, new Color(1.0f, 0.4f, 0.45f, 1.0f), Color.WHITE, 1);
        TextButton.TextButtonStyle closeBtnStyle = new TextButton.TextButtonStyle();
        closeBtnStyle.up = new TextureRegionDrawable(texCloseUp);
        closeBtnStyle.down = new TextureRegionDrawable(texCloseDown);
        closeBtnStyle.font = defaultFont;
        closeBtnStyle.fontColor = Color.WHITE;
        skin.add("close-btn", closeBtnStyle);

        // Circular Action Button Style
        Texture texCircleAction = createCircleTexture(38, new Color(0.12f, 0.16f, 0.24f, 0.92f), new Color(0.4f, 0.6f, 0.85f, 0.9f), 1);
        Texture texCircleActionDown = createCircleTexture(38, new Color(0.2f, 0.3f, 0.45f, 1.0f), Color.CYAN, 1);
        Button.ButtonStyle circleActionStyle = new Button.ButtonStyle();
        circleActionStyle.up = new TextureRegionDrawable(texCircleAction);
        circleActionStyle.down = new TextureRegionDrawable(texCircleActionDown);
        skin.add("circle-action", circleActionStyle);

        // Danger Button (for Exit / Abort)
        Texture texBtnDanger = createRoundedRectTexture(16, 16, 3,
                new Color(0.3f, 0.08f, 0.1f, 0.95f),
                new Color(1.0f, 0.2f, 0.2f, 0.9f), 1);
        TextButton.TextButtonStyle dangerStyle = new TextButton.TextButtonStyle();
        dangerStyle.up = new NinePatchDrawable(new NinePatch(texBtnDanger, 3, 3, 3, 3));
        dangerStyle.down = new NinePatchDrawable(patchBtnDown);
        dangerStyle.font = defaultFont;
        dangerStyle.fontColor = new Color(1.0f, 0.4f, 0.4f, 1.0f);
        skin.add("danger", dangerStyle);

        // 5. Label Styles
        Label.LabelStyle labelStyle = new Label.LabelStyle(defaultFont, Color.WHITE);
        skin.add("default", labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(0.0f, 0.9f, 1.0f, 1.0f));
        skin.add("title", titleStyle);

        Label.LabelStyle subTitleStyle = new Label.LabelStyle(subTitleFont, Color.WHITE);
        skin.add("sub-title", subTitleStyle);

        Label.LabelStyle currencyStyle = new Label.LabelStyle(currencyFont, new Color(1.0f, 0.82f, 0.25f, 1.0f));
        skin.add("currency", currencyStyle);

        Label.LabelStyle badgeStyle = new Label.LabelStyle(smallFont, new Color(0.85f, 0.90f, 0.95f, 1.0f));
        skin.add("badge", badgeStyle);

        Label.LabelStyle descStyle = new Label.LabelStyle(smallFont, new Color(0.70f, 0.75f, 0.82f, 1.0f));
        skin.add("desc", descStyle);

        Label.LabelStyle alertStyle = new Label.LabelStyle(titleFont, new Color(1.0f, 0.2f, 0.2f, 1.0f));
        skin.add("alert", alertStyle);

        // 6. Slider Styles
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(texWhite);
        sliderStyle.knob = new TextureRegionDrawable(texBtnDown);
        sliderStyle.knob.setMinHeight(28);
        sliderStyle.knob.setMinWidth(14);
        sliderStyle.background.setMinHeight(8);
        skin.add("default-horizontal", sliderStyle);

        // 7. ScrollPane Styles
        Texture texScrollTrack = createSolidTexture(8, 16, new Color(0.08f, 0.10f, 0.16f, 0.85f));
        Texture texScrollKnob = createSolidTexture(8, 16, new Color(0.0f, 0.85f, 1.0f, 0.95f));

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = new TextureRegionDrawable(texScrollTrack);
        scrollStyle.vScrollKnob = new TextureRegionDrawable(texScrollKnob);
        scrollStyle.hScroll = new TextureRegionDrawable(texScrollTrack);
        scrollStyle.hScrollKnob = new TextureRegionDrawable(texScrollKnob);
        skin.add("default", scrollStyle);

        // 8. ProgressBar Styles (Stat bars & loading bars)
        Texture texProgressBg = createRoundedRectTexture(16, 16, 2,
                new Color(0.10f, 0.12f, 0.18f, 0.95f),
                new Color(0.25f, 0.30f, 0.40f, 0.8f), 1);
        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
        progressBarStyle.background = new TextureRegionDrawable(texProgressBg);
        progressBarStyle.knobBefore = new TextureRegionDrawable(texWhite);
        progressBarStyle.knob = new TextureRegionDrawable(texWhite);
        skin.add("default-horizontal", progressBarStyle);
        skin.add("default", progressBarStyle);
    }

    /**
     * Generates crisp procedural category icons matching the reference garage screen.
     */
    private void createProceduralIcons() {
        int sz = 48;

        // 1. Front Bumper / Body Kit icon
        Pixmap pixBumper = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixBumper.setColor(0, 0, 0, 0);
        pixBumper.fill();
        pixBumper.setColor(Color.WHITE);
        // Stylized aerodynamic bumper curve
        pixBumper.fillRectangle(8, 14, 32, 6);
        pixBumper.fillRectangle(6, 18, 10, 16);
        pixBumper.fillRectangle(32, 18, 10, 16);
        pixBumper.fillRectangle(12, 30, 24, 6);
        Texture texBumper = new Texture(pixBumper);
        pixBumper.dispose();
        managedTextures.add(texBumper);
        skin.add("icon-bumper", new TextureRegionDrawable(texBumper));

        // 2. Rear Diffuser icon
        Pixmap pixDiffuser = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixDiffuser.setColor(0, 0, 0, 0);
        pixDiffuser.fill();
        pixDiffuser.setColor(Color.WHITE);
        pixDiffuser.fillRectangle(6, 16, 36, 6);
        pixDiffuser.fillRectangle(12, 22, 6, 14);
        pixDiffuser.fillRectangle(21, 22, 6, 14);
        pixDiffuser.fillRectangle(30, 22, 6, 14);
        Texture texDiffuser = new Texture(pixDiffuser);
        pixDiffuser.dispose();
        managedTextures.add(texDiffuser);
        skin.add("icon-diffuser", new TextureRegionDrawable(texDiffuser));

        // 3. Spoiler / Wing icon
        Pixmap pixSpoiler = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixSpoiler.setColor(0, 0, 0, 0);
        pixSpoiler.fill();
        pixSpoiler.setColor(Color.WHITE);
        // Wing blade
        pixSpoiler.fillRectangle(6, 12, 36, 5);
        pixSpoiler.fillRectangle(4, 10, 6, 9);
        pixSpoiler.fillRectangle(38, 10, 6, 9);
        // Upright mounts
        pixSpoiler.fillRectangle(14, 17, 4, 18);
        pixSpoiler.fillRectangle(30, 17, 4, 18);
        Texture texSpoiler = new Texture(pixSpoiler);
        pixSpoiler.dispose();
        managedTextures.add(texSpoiler);
        skin.add("icon-spoiler", new TextureRegionDrawable(texSpoiler));

        // 4. Wheels & Rims icon
        Pixmap pixWheel = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixWheel.setColor(0, 0, 0, 0);
        pixWheel.fill();
        pixWheel.setColor(Color.WHITE);
        pixWheel.drawCircle(sz / 2, sz / 2, 18);
        pixWheel.drawCircle(sz / 2, sz / 2, 17);
        pixWheel.drawCircle(sz / 2, sz / 2, 8);
        pixWheel.fillCircle(sz / 2, sz / 2, 4);
        // 5 Spokes
        for (int i = 0; i < 5; i++) {
            float angle = i * (float) (Math.PI * 2 / 5.0);
            int x2 = (int) (sz / 2 + Math.cos(angle) * 16);
            int y2 = (int) (sz / 2 + Math.sin(angle) * 16);
            pixWheel.drawLine(sz / 2, sz / 2, x2, y2);
        }
        Texture texWheel = new Texture(pixWheel);
        pixWheel.dispose();
        managedTextures.add(texWheel);
        skin.add("icon-wheel", new TextureRegionDrawable(texWheel));

        // 5. Spray Paint Can icon
        Pixmap pixPaint = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixPaint.setColor(0, 0, 0, 0);
        pixPaint.fill();
        pixPaint.setColor(Color.WHITE);
        // Can body
        pixPaint.fillRectangle(14, 18, 20, 22);
        pixPaint.fillRectangle(18, 12, 12, 6);
        // Nozzle & spray dots
        pixPaint.fillRectangle(20, 8, 8, 4);
        pixPaint.fillCircle(12, 8, 2);
        pixPaint.fillCircle(8, 12, 2);
        Texture texPaint = new Texture(pixPaint);
        pixPaint.dispose();
        managedTextures.add(texPaint);
        skin.add("icon-paint", new TextureRegionDrawable(texPaint));

        // 6. Livery / Decal Chequered icon
        Pixmap pixLivery = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixLivery.setColor(0, 0, 0, 0);
        pixLivery.fill();
        pixLivery.setColor(Color.WHITE);
        int cs = 7;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if ((r + c) % 2 == 0) {
                    pixLivery.fillRectangle(10 + c * cs, 10 + r * cs, cs, cs);
                }
            }
        }
        Texture texLivery = new Texture(pixLivery);
        pixLivery.dispose();
        managedTextures.add(texLivery);
        skin.add("icon-livery", new TextureRegionDrawable(texLivery));

        // 7. Drivetrain & Suspension icon
        Pixmap pixChassis = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixChassis.setColor(0, 0, 0, 0);
        pixChassis.fill();
        pixChassis.setColor(Color.WHITE);
        // Axles
        pixChassis.fillRectangle(8, 14, 32, 4);
        pixChassis.fillRectangle(8, 32, 32, 4);
        // Drive shaft
        pixChassis.fillRectangle(22, 14, 4, 22);
        // Hubs
        pixChassis.fillRectangle(6, 11, 4, 10);
        pixChassis.fillRectangle(38, 11, 4, 10);
        pixChassis.fillRectangle(6, 29, 4, 10);
        pixChassis.fillRectangle(38, 29, 4, 10);
        Texture texChassis = new Texture(pixChassis);
        pixChassis.dispose();
        managedTextures.add(texChassis);
        skin.add("icon-chassis", new TextureRegionDrawable(texChassis));

        // 8. Engine / Pistons icon
        Pixmap pixEngine = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixEngine.setColor(0, 0, 0, 0);
        pixEngine.fill();
        pixEngine.setColor(Color.WHITE);
        // V-pistons
        pixEngine.fillRectangle(10, 10, 10, 14);
        pixEngine.fillRectangle(28, 10, 10, 14);
        pixEngine.drawLine(15, 24, 24, 36);
        pixEngine.drawLine(33, 24, 24, 36);
        pixEngine.fillCircle(24, 36, 5);
        Texture texEngine = new Texture(pixEngine);
        pixEngine.dispose();
        managedTextures.add(texEngine);
        skin.add("icon-engine", new TextureRegionDrawable(texEngine));

        // 9. Transmission / Tuning Gear icon
        Pixmap pixGear = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixGear.setColor(0, 0, 0, 0);
        pixGear.fill();
        pixGear.setColor(Color.WHITE);
        pixGear.drawCircle(sz / 2, sz / 2, 14);
        pixGear.drawCircle(sz / 2, sz / 2, 13);
        pixGear.fillCircle(sz / 2, sz / 2, 5);
        // Gear teeth
        for (int i = 0; i < 8; i++) {
            float angle = i * (float) (Math.PI / 4.0);
            int gx = (int) (sz / 2 + Math.cos(angle) * 16);
            int gy = (int) (sz / 2 + Math.sin(angle) * 16);
            pixGear.fillCircle(gx, gy, 3);
        }
        Texture texGear = new Texture(pixGear);
        pixGear.dispose();
        managedTextures.add(texGear);
        skin.add("icon-gear", new TextureRegionDrawable(texGear));

        // 10. Brake Caliper / Disc icon
        Pixmap pixBrake = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixBrake.setColor(0, 0, 0, 0);
        pixBrake.fill();
        pixBrake.setColor(Color.WHITE);
        pixBrake.drawCircle(sz / 2, sz / 2, 16);
        pixBrake.drawCircle(sz / 2, sz / 2, 8);
        // Caliper block
        pixBrake.fillRectangle(8, 12, 12, 24);
        Texture texBrake = new Texture(pixBrake);
        pixBrake.dispose();
        managedTextures.add(texBrake);
        skin.add("icon-brake", new TextureRegionDrawable(texBrake));

        // 11. Currency Coin / Gold Bar icon
        Pixmap pixCoin = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixCoin.setColor(0, 0, 0, 0);
        pixCoin.fill();
        pixCoin.setColor(new Color(1.0f, 0.8f, 0.1f, 1.0f));
        pixCoin.fillCircle(sz / 2, sz / 2, 16);
        pixCoin.setColor(new Color(0.85f, 0.6f, 0.05f, 1.0f));
        pixCoin.drawCircle(sz / 2, sz / 2, 16);
        pixCoin.setColor(Color.WHITE);
        pixCoin.fillRectangle(22, 14, 4, 20);
        pixCoin.fillRectangle(16, 22, 16, 4);
        Texture texCoin = new Texture(pixCoin);
        pixCoin.dispose();
        managedTextures.add(texCoin);
        skin.add("icon-coin", new TextureRegionDrawable(texCoin));

        // 12. Rotate 360 icon
        Pixmap pixRotate = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixRotate.setColor(0, 0, 0, 0);
        pixRotate.fill();
        pixRotate.setColor(Color.WHITE);
        pixRotate.drawCircle(sz / 2, sz / 2, 14);
        pixRotate.fillTriangle(sz / 2 + 14, sz / 2, sz / 2 + 20, sz / 2 - 8, sz / 2 + 8, sz / 2 - 8);
        Texture texRotate = new Texture(pixRotate);
        pixRotate.dispose();
        managedTextures.add(texRotate);
        skin.add("icon-rotate", new TextureRegionDrawable(texRotate));

        // 13. Spec / Checklist icon
        Pixmap pixInfo = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixInfo.setColor(0, 0, 0, 0);
        pixInfo.fill();
        pixInfo.setColor(Color.WHITE);
        pixInfo.drawRectangle(12, 8, 24, 32);
        pixInfo.drawLine(18, 16, 30, 16);
        pixInfo.drawLine(18, 24, 30, 24);
        pixInfo.drawLine(18, 32, 26, 32);
        Texture texInfo = new Texture(pixInfo);
        pixInfo.dispose();
        managedTextures.add(texInfo);
        skin.add("icon-info", new TextureRegionDrawable(texInfo));

        // 14. Stock "None / Null" slashed circle icon
        Pixmap pixNone = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pixNone.setColor(0, 0, 0, 0);
        pixNone.fill();
        pixNone.setColor(Color.GRAY);
        pixNone.drawCircle(sz / 2, sz / 2, 16);
        pixNone.drawLine(16, 16, 32, 32);
        Texture texNone = new Texture(pixNone);
        pixNone.dispose();
        managedTextures.add(texNone);
        skin.add("icon-none", new TextureRegionDrawable(texNone));
    }

    private Texture createSolidTexture(int width, int height, Color color) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        managedTextures.add(tex);
        return tex;
    }

    private Texture createRoundedRectTexture(int width, int height, int radius, Color fill, Color border, int borderWidth) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        // Fill rounded rect
        pix.setColor(fill);
        pix.fillRectangle(radius, 0, width - 2 * radius, height);
        pix.fillRectangle(0, radius, width, height - 2 * radius);
        pix.fillCircle(radius, radius, radius);
        pix.fillCircle(width - radius - 1, radius, radius);
        pix.fillCircle(radius, height - radius - 1, radius);
        pix.fillCircle(width - radius - 1, height - radius - 1, radius);

        // Border
        if (border != null && borderWidth > 0) {
            pix.setColor(border);
            for (int b = 0; b < borderWidth; b++) {
                pix.drawRectangle(b, b, width - 2 * b, height - 2 * b);
            }
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        managedTextures.add(tex);
        return tex;
    }

    private Texture createCircleTexture(int size, Color fill, Color border, int borderWidth) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        int r = size / 2 - 1;
        pix.setColor(fill);
        pix.fillCircle(size / 2, size / 2, r);

        if (border != null && borderWidth > 0) {
            pix.setColor(border);
            for (int b = 0; b < borderWidth; b++) {
                pix.drawCircle(size / 2, size / 2, r - b);
            }
        }

        Texture tex = new Texture(pix);
        pix.dispose();
        managedTextures.add(tex);
        return tex;
    }

    public Skin getSkin() {
        return skin;
    }

    public BitmapFont getDefaultFont() {
        return defaultFont;
    }

    public BitmapFont getSmallFont() {
        return smallFont;
    }

    @Override
    public void dispose() {
        if (skin != null) skin.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subTitleFont != null) subTitleFont.dispose();
        if (defaultFont != null) defaultFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (currencyFont != null) currencyFont.dispose();

        for (Texture tex : managedTextures) {
            if (tex != null) tex.dispose();
        }
        managedTextures.clear();
    }
}
