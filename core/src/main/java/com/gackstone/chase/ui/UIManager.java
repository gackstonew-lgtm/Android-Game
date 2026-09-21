package com.gackstone.chase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages responsive Scene2D skin, procedural stylized widgets, and typography
 * without requiring external proprietary texture atlases.
 */
public class UIManager implements Disposable {

    private final Skin skin;
    private final BitmapFont titleFont;
    private final BitmapFont defaultFont;

    public UIManager() {
        skin = new Skin();
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.2f);

        defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.4f);

        skin.add("title-font", titleFont);
        skin.add("default-font", defaultFont);

        generateProceduralSkin();
    }

    private void generateProceduralSkin() {
        // 1. Base solid colors
        Pixmap pixWhite = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixWhite.setColor(Color.WHITE);
        pixWhite.fill();
        Texture texWhite = new Texture(pixWhite);
        pixWhite.dispose();
        skin.add("white", texWhite);

        // 2. Button backgrounds
        Pixmap pixBtn = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixBtn.setColor(0.12f, 0.16f, 0.24f, 0.95f);
        pixBtn.fillRectangle(0, 0, 16, 16);
        pixBtn.setColor(0.0f, 0.85f, 1.0f, 0.9f);
        pixBtn.drawRectangle(0, 0, 16, 16);
        Texture texBtn = new Texture(pixBtn);
        pixBtn.dispose();

        Pixmap pixBtnDown = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixBtnDown.setColor(0.0f, 0.6f, 0.8f, 1.0f);
        pixBtnDown.fillRectangle(0, 0, 16, 16);
        Texture texBtnDown = new Texture(pixBtnDown);
        pixBtnDown.dispose();

        NinePatch patchBtn = new NinePatch(texBtn, 3, 3, 3, 3);
        NinePatch patchBtnDown = new NinePatch(texBtnDown, 3, 3, 3, 3);

        // 3. TextButton Styles
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new NinePatchDrawable(patchBtn);
        btnStyle.down = new NinePatchDrawable(patchBtnDown);
        btnStyle.font = defaultFont;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.downFontColor = Color.BLACK;
        skin.add("default", btnStyle);

        // Danger Button (for Exit / Abort)
        Pixmap pixBtnDanger = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixBtnDanger.setColor(0.3f, 0.08f, 0.1f, 0.95f);
        pixBtnDanger.fillRectangle(0, 0, 16, 16);
        pixBtnDanger.setColor(1.0f, 0.2f, 0.2f, 0.9f);
        pixBtnDanger.drawRectangle(0, 0, 16, 16);
        Texture texBtnDanger = new Texture(pixBtnDanger);
        pixBtnDanger.dispose();

        TextButton.TextButtonStyle dangerStyle = new TextButton.TextButtonStyle();
        dangerStyle.up = new NinePatchDrawable(new NinePatch(texBtnDanger, 3, 3, 3, 3));
        dangerStyle.down = new NinePatchDrawable(patchBtnDown);
        dangerStyle.font = defaultFont;
        dangerStyle.fontColor = new Color(1.0f, 0.4f, 0.4f, 1.0f);
        skin.add("danger", dangerStyle);

        // 4. Label Styles
        Label.LabelStyle labelStyle = new Label.LabelStyle(defaultFont, Color.WHITE);
        skin.add("default", labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(0.0f, 0.9f, 1.0f, 1.0f));
        skin.add("title", titleStyle);

        Label.LabelStyle alertStyle = new Label.LabelStyle(titleFont, new Color(1.0f, 0.2f, 0.2f, 1.0f));
        skin.add("alert", alertStyle);

        // 5. Slider Styles
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(texWhite);
        sliderStyle.knob = new TextureRegionDrawable(texBtnDown);
        sliderStyle.knob.setMinHeight(28);
        sliderStyle.knob.setMinWidth(14);
        sliderStyle.background.setMinHeight(8);
        skin.add("default-horizontal", sliderStyle);
    }

    public Skin getSkin() {
        return skin;
    }

    @Override
    public void dispose() {
        if (skin != null) {
            skin.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (defaultFont != null) {
            defaultFont.dispose();
        }
    }
}
