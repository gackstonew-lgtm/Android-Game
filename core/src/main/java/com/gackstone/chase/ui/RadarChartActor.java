package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * 5-Axis Spider / Radar Chart Actor matching the high-end mobile racing UI design.
 *
 * <p>Telemetry Axes:
 * <ul>
 *   <li>0 (Top - 90°): TORQUE (Acceleration)
 *   <li>1 (Top-Right - 18°): WEIGHT (Mass / Stability)
 *   <li>2 (Bottom-Right - 306°): GRIP (Handling / Traction)
 *   <li>3 (Bottom-Left - 234°): BRAKING (Armour / Decel)
 *   <li>4 (Top-Left - 162°): SPEED (Top Speed / RPM)
 * </ul>
 *
 * <p>Supports before/after (Stock vs Upgraded) stat comparison, smooth morph animations,
 * zero GC allocation during frame rendering, and glassmorphic vertex badges.
 */
public class RadarChartActor extends Actor {

    private static final int NUM_AXES = 5;
    private static final int NUM_GRID_LEVELS = 5;

    // Axis Angles in radians (starting from top, clockwise)
    private static final float[] ANGLES = new float[] {
            MathUtils.PI / 2.0f,                     // Top (90°) - Torque
            MathUtils.PI / 2.0f - (MathUtils.PI2 / 5.0f),     // Top-Right (18°) - Weight
            MathUtils.PI / 2.0f - 2.0f * (MathUtils.PI2 / 5.0f), // Bottom-Right (306°) - Grip
            MathUtils.PI / 2.0f - 3.0f * (MathUtils.PI2 / 5.0f), // Bottom-Left (234°) - Braking
            MathUtils.PI / 2.0f - 4.0f * (MathUtils.PI2 / 5.0f)  // Top-Left (162°) - Speed
    };

    private static final String[] AXIS_LABELS = new String[] {
            "TORQUE", "WEIGHT", "GRIP", "BRAKE", "SPEED"
    };

    // Stat ranges [0.0 - 1.0] normalized
    private final float[] targetStockValues   = new float[NUM_AXES];
    private final float[] currentStockValues  = new float[NUM_AXES];
    private final float[] targetUpgradeValues = new float[NUM_AXES];
    private final float[] currentUpgradeValues= new float[NUM_AXES];

    // Pre-allocated vectors for vertices to guarantee 0 GC
    private final Vector2[] gridVertices    = new Vector2[NUM_AXES];
    private final Vector2[] stockVertices   = new Vector2[NUM_AXES];
    private final Vector2[] upgradeVertices = new Vector2[NUM_AXES];

    // ShapeRenderer for polygon and line rendering
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    // Colors matching the reference screenshot
    private final Color colGridFill     = new Color(0.06f, 0.10f, 0.18f, 0.40f);
    private final Color colGridLines    = new Color(0.25f, 0.35f, 0.50f, 0.45f);
    private final Color colGridOuter    = new Color(0.40f, 0.65f, 0.90f, 0.60f);

    // Stock Polygon (Translucent Cyan/Blue)
    private final Color colStockFill    = new Color(0.0f, 0.65f, 0.95f, 0.25f);
    private final Color colStockLine    = new Color(0.2f, 0.85f, 1.0f, 0.90f);
    private final Color colStockDot     = new Color(0.4f, 0.95f, 1.0f, 1.0f);

    // Upgraded / Preview Polygon (Translucent Amber Gold/Orange)
    private final Color colUpgradeFill  = new Color(1.0f, 0.60f, 0.0f, 0.30f);
    private final Color colUpgradeLine  = new Color(1.0f, 0.78f, 0.15f, 0.95f);
    private final Color colUpgradeDot   = new Color(1.0f, 0.90f, 0.30f, 1.0f);

    // Badge styling
    private final Color colBadgeBg      = new Color(0.08f, 0.11f, 0.16f, 0.90f);
    private final Color colBadgeBorder  = new Color(0.22f, 0.30f, 0.42f, 0.85f);
    private final Color colText         = Color.WHITE;

    private boolean showUpgradeComparison = true;

    public RadarChartActor(BitmapFont font) {
        this.shapeRenderer = new ShapeRenderer();
        this.font = font;
        this.glyphLayout = new GlyphLayout();

        for (int i = 0; i < NUM_AXES; i++) {
            gridVertices[i]    = new Vector2();
            stockVertices[i]   = new Vector2();
            upgradeVertices[i] = new Vector2();
            targetStockValues[i]    = 0.5f;
            currentStockValues[i]   = 0.5f;
            targetUpgradeValues[i]  = 0.65f;
            currentUpgradeValues[i] = 0.65f;
        }

        setSize(240, 240);
    }

    /**
     * Updates the stat values displayed on the radar chart.
     * Values are normalized [0.0 - 1.0].
     */
    public void setStats(float torque, float weight, float grip, float brake, float speed) {
        targetStockValues[0] = MathUtils.clamp(torque, 0.15f, 1.0f);
        targetStockValues[1] = MathUtils.clamp(weight, 0.15f, 1.0f);
        targetStockValues[2] = MathUtils.clamp(grip, 0.15f, 1.0f);
        targetStockValues[3] = MathUtils.clamp(brake, 0.15f, 1.0f);
        targetStockValues[4] = MathUtils.clamp(speed, 0.15f, 1.0f);

        // By default, upgrade matches stock until an upgrade preview is set
        for (int i = 0; i < NUM_AXES; i++) {
            targetUpgradeValues[i] = targetStockValues[i];
        }
    }

    /**
     * Sets preview / upgraded comparison stats.
     */
    public void setUpgradePreview(float torque, float weight, float grip, float brake, float speed) {
        targetUpgradeValues[0] = MathUtils.clamp(torque, 0.15f, 1.0f);
        targetUpgradeValues[1] = MathUtils.clamp(weight, 0.15f, 1.0f);
        targetUpgradeValues[2] = MathUtils.clamp(grip, 0.15f, 1.0f);
        targetUpgradeValues[3] = MathUtils.clamp(brake, 0.15f, 1.0f);
        targetUpgradeValues[4] = MathUtils.clamp(speed, 0.15f, 1.0f);
        showUpgradeComparison = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Smooth interpolation of stat vertices
        float lerpFactor = Math.min(1.0f, delta * 10.0f);
        for (int i = 0; i < NUM_AXES; i++) {
            currentStockValues[i]   = MathUtils.lerp(currentStockValues[i], targetStockValues[i], lerpFactor);
            currentUpgradeValues[i] = MathUtils.lerp(currentUpgradeValues[i], targetUpgradeValues[i], lerpFactor);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float centerX = getX() + getWidth() * 0.5f;
        float centerY = getY() + getHeight() * 0.52f;
        float radius  = Math.min(getWidth(), getHeight()) * 0.36f;

        // End SpriteBatch to draw vector shapes
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

        // 1. Draw Background Pentagons
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colGridFill);
        drawPolygonFill(centerX, centerY, radius, 1.0f);
        shapeRenderer.end();

        // 2. Draw Concentric Grid Rings & Radial Spoke Lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int step = 1; step <= NUM_GRID_LEVELS; step++) {
            float frac = (float) step / NUM_GRID_LEVELS;
            if (step == NUM_GRID_LEVELS) {
                shapeRenderer.setColor(colGridOuter);
            } else {
                shapeRenderer.setColor(colGridLines);
            }
            drawPolygonOutline(centerX, centerY, radius * frac);
        }

        // Radial axis lines
        shapeRenderer.setColor(colGridLines);
        for (int i = 0; i < NUM_AXES; i++) {
            float cos = MathUtils.cos(ANGLES[i]);
            float sin = MathUtils.sin(ANGLES[i]);
            shapeRenderer.line(centerX, centerY, centerX + cos * radius, centerY + sin * radius);
        }
        shapeRenderer.end();

        // 3. Compute Polygon Vertex Positions
        for (int i = 0; i < NUM_AXES; i++) {
            float cos = MathUtils.cos(ANGLES[i]);
            float sin = MathUtils.sin(ANGLES[i]);

            float sR = radius * currentStockValues[i];
            stockVertices[i].set(centerX + cos * sR, centerY + sin * sR);

            float uR = radius * currentUpgradeValues[i];
            upgradeVertices[i].set(centerX + cos * uR, centerY + sin * uR);
        }

        // 4. Draw Upgraded/Preview Polygon (if comparison is active)
        if (showUpgradeComparison) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(colUpgradeFill);
            drawCustomPolygonFill(centerX, centerY, upgradeVertices);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(colUpgradeLine);
            drawCustomPolygonOutline(upgradeVertices);
            shapeRenderer.end();

            // Vertex dots
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(colUpgradeDot);
            for (int i = 0; i < NUM_AXES; i++) {
                shapeRenderer.circle(upgradeVertices[i].x, upgradeVertices[i].y, 3.5f, 12);
            }
            shapeRenderer.end();
        }

        // 5. Draw Stock / Current Polygon
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colStockFill);
        drawCustomPolygonFill(centerX, centerY, stockVertices);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(colStockLine);
        drawCustomPolygonOutline(stockVertices);
        shapeRenderer.end();

        // Stock Vertex dots
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colStockDot);
        for (int i = 0; i < NUM_AXES; i++) {
            shapeRenderer.circle(stockVertices[i].x, stockVertices[i].y, 3.0f, 12);
        }
        shapeRenderer.end();

        // 6. Draw Badge Backgrounds for Axis Labels
        float badgeRadius = radius + 22.0f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < NUM_AXES; i++) {
            float cos = MathUtils.cos(ANGLES[i]);
            float sin = MathUtils.sin(ANGLES[i]);
            float bx = centerX + cos * badgeRadius;
            float by = centerY + sin * badgeRadius;

            shapeRenderer.setColor(colBadgeBg);
            shapeRenderer.rect(bx - 32, by - 10, 64, 20);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NUM_AXES; i++) {
            float cos = MathUtils.cos(ANGLES[i]);
            float sin = MathUtils.sin(ANGLES[i]);
            float bx = centerX + cos * badgeRadius;
            float by = centerY + sin * badgeRadius;

            shapeRenderer.setColor(colBadgeBorder);
            shapeRenderer.rect(bx - 32, by - 10, 64, 20);
        }
        shapeRenderer.end();

        // Resume SpriteBatch to render text labels and legend
        batch.begin();

        if (font != null) {
            float oldScaleX = font.getData().scaleX;
            float oldScaleY = font.getData().scaleY;
            font.getData().setScale(0.9f);
            font.setColor(colText);

            for (int i = 0; i < NUM_AXES; i++) {
                float cos = MathUtils.cos(ANGLES[i]);
                float sin = MathUtils.sin(ANGLES[i]);
                float bx = centerX + cos * badgeRadius;
                float by = centerY + sin * badgeRadius;

                glyphLayout.setText(font, AXIS_LABELS[i]);
                font.draw(batch, glyphLayout, bx - glyphLayout.width * 0.5f, by + glyphLayout.height * 0.5f);
            }

            // Draw Legend at the bottom
            float legendY = getY() + 6;
            font.getData().setScale(0.85f);

            // Stock legend dot + text
            font.setColor(colStockLine);
            font.draw(batch, "● STOCK", centerX - 75, legendY);

            // Upgrade legend dot + text
            font.setColor(colUpgradeLine);
            font.draw(batch, "● UPGRADE", centerX + 10, legendY);

            // Restore font scale
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }

    private void drawPolygonFill(float cx, float cy, float r, float scale) {
        for (int i = 0; i < NUM_AXES; i++) {
            int next = (i + 1) % NUM_AXES;
            float x1 = cx + MathUtils.cos(ANGLES[i]) * r * scale;
            float y1 = cy + MathUtils.sin(ANGLES[i]) * r * scale;
            float x2 = cx + MathUtils.cos(ANGLES[next]) * r * scale;
            float y2 = cy + MathUtils.sin(ANGLES[next]) * r * scale;
            shapeRenderer.triangle(cx, cy, x1, y1, x2, y2);
        }
    }

    private void drawPolygonOutline(float cx, float cy, float r) {
        for (int i = 0; i < NUM_AXES; i++) {
            int next = (i + 1) % NUM_AXES;
            float x1 = cx + MathUtils.cos(ANGLES[i]) * r;
            float y1 = cy + MathUtils.sin(ANGLES[i]) * r;
            float x2 = cx + MathUtils.cos(ANGLES[next]) * r;
            float y2 = cy + MathUtils.sin(ANGLES[next]) * r;
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    private void drawCustomPolygonFill(float cx, float cy, Vector2[] verts) {
        for (int i = 0; i < NUM_AXES; i++) {
            int next = (i + 1) % NUM_AXES;
            shapeRenderer.triangle(cx, cy, verts[i].x, verts[i].y, verts[next].x, verts[next].y);
        }
    }

    private void drawCustomPolygonOutline(Vector2[] verts) {
        for (int i = 0; i < NUM_AXES; i++) {
            int next = (i + 1) % NUM_AXES;
            shapeRenderer.line(verts[i].x, verts[i].y, verts[next].x, verts[next].y);
        }
    }

    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
