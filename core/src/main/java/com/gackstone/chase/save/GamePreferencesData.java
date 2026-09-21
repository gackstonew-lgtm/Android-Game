package com.gackstone.chase.save;

import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates game settings, audio parameters, graphics quality preset,
 * and high score statistics.
 */
public class GamePreferencesData {

    private float masterVolume = 1.0f;
    private float musicVolume = 0.8f;
    private float sfxVolume = 1.0f;
    private boolean isMuted = false;

    private GameConfig.GraphicsQuality graphicsQuality = GameConfig.GraphicsQuality.HIGH;
    private boolean isDebugOverlayEnabled = false;

    private long highScore = 0;
    private float highestDistance = 0.0f;
    private int totalRuns = 0;

    public float getMasterVolume() { return masterVolume; }
    public void setMasterVolume(float masterVolume) { this.masterVolume = masterVolume; }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = musicVolume; }

    public float getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(float sfxVolume) { this.sfxVolume = sfxVolume; }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public GameConfig.GraphicsQuality getGraphicsQuality() { return graphicsQuality; }
    public void setGraphicsQuality(GameConfig.GraphicsQuality graphicsQuality) { this.graphicsQuality = graphicsQuality; }

    public boolean isDebugOverlayEnabled() { return isDebugOverlayEnabled; }
    public void setDebugOverlayEnabled(boolean debugOverlayEnabled) { isDebugOverlayEnabled = debugOverlayEnabled; }

    public long getHighScore() { return highScore; }
    public void setHighScore(long highScore) { this.highScore = highScore; }

    public float getHighestDistance() { return highestDistance; }
    public void setHighestDistance(float highestDistance) { this.highestDistance = highestDistance; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }
}
