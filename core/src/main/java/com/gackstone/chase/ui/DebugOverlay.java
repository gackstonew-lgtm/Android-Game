package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.core.GameManager;

/**
 * Developer telemetry overlay providing real-time FPS, entity transforms,
 * memory consumption, and state metrics during development.
 */
public class DebugOverlay implements Disposable {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private boolean enabled = false;

    public DebugOverlay() {
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.GREEN);
        this.font.getData().setScale(1.2f);
    }

    public void render(GameManager gameManager) {
        if (!enabled || gameManager == null) return;

        batch.begin();

        int fps = Gdx.graphics.getFramesPerSecond();
        long javaHeap = Gdx.app.getJavaHeap() / (1024 * 1024);
        long nativeHeap = Gdx.app.getNativeHeap() / (1024 * 1024);

        Vector3 pPos = gameManager.getPlayer().getPosition();
        float pSpeed = gameManager.getPlayer().getState().getForwardSpeed();
        float enemyDist = gameManager.getEnemyDistance();

        int y = Gdx.graphics.getHeight() - 20;
        int lineSpacing = 22;

        font.draw(batch, String.format("DEBUG METRICS | FPS: %d", fps), 20, y);
        y -= lineSpacing;
        font.draw(batch, String.format("MEM: Heap %dMB | Native %dMB", javaHeap, nativeHeap), 20, y);
        y -= lineSpacing;
        font.draw(batch, String.format("PLAYER: Pos (%.1f, %.1f, %.1f) | Spd: %.1f m/s", pPos.x, pPos.y, pPos.z, pSpeed), 20, y);
        y -= lineSpacing;
        font.draw(batch, String.format("ENEMY DIST: %.1f m | COLLIDABLES: %d", enemyDist, gameManager.getCollisionManager().getCollidableCount()), 20, y);

        batch.end();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
