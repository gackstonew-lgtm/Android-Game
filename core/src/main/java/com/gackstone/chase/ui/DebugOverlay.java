package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.camera.CameraMode;
import com.gackstone.chase.core.GameManager;
import com.gackstone.chase.world.EnvironmentTheme;

/**
 * Developer telemetry overlay providing real-time FPS, entity transforms,
 * memory consumption, camera mode, environment theme, and enemy count.
 */
public class DebugOverlay implements Disposable {

    private final SpriteBatch batch;
    private final BitmapFont  font;
    private boolean enabled = false;

    public DebugOverlay() {
        this.batch = new SpriteBatch();
        this.font  = new BitmapFont();
        this.font.setColor(Color.GREEN);
        this.font.getData().setScale(1.2f);
    }

    public void render(GameManager gameManager) {
        if (!enabled || gameManager == null) return;

        batch.begin();

        int  fps        = Gdx.graphics.getFramesPerSecond();
        long javaHeap   = Gdx.app.getJavaHeap()   / (1024 * 1024);
        long nativeHeap = Gdx.app.getNativeHeap()  / (1024 * 1024);

        Vector3 pPos     = gameManager.getPlayer().getPosition();
        float   pSpeed   = gameManager.getPlayer().getState().getForwardSpeed();
        float   enemyDist = gameManager.getEnemyDistance();
        int     enemies  = gameManager.getEnemyManager().getEnemies().size;
        CameraMode cam   = gameManager.getChaseCamera().getMode();

        EnvironmentTheme theme = gameManager.getWorldManager()
                .getEnvironmentRenderer().getCurrentTheme();
        String themeName = (theme != null) ? theme.getName() : "?";

        String carName = (gameManager.getPlayer().getCurrentCar() != null)
                ? gameManager.getPlayer().getCurrentCar().getDisplayName() : "?";

        int y = Gdx.graphics.getHeight() - 20;
        int ls = 22;

        font.draw(batch, String.format("DEBUG | FPS: %d | CAM: %s", fps, cam.name()), 20, y); y -= ls;
        font.draw(batch, String.format("MEM: Heap %dMB | Native %dMB", javaHeap, nativeHeap), 20, y); y -= ls;
        font.draw(batch, String.format("PLAYER: (%.1f,%.1f,%.1f) | %.1f m/s | %s", pPos.x, pPos.y, pPos.z, pSpeed, carName), 20, y); y -= ls;
        font.draw(batch, String.format("ENEMIES: %d | CLOSEST: %.1f m", enemies, enemyDist), 20, y); y -= ls;
        font.draw(batch, String.format("THEME: %s | COLLIDABLES: %d", themeName,
                gameManager.getCollisionManager().getCollidableCount()), 20, y);

        batch.end();
    }

    public boolean isEnabled()              { return enabled; }
    public void    setEnabled(boolean e)    { this.enabled = e; }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
