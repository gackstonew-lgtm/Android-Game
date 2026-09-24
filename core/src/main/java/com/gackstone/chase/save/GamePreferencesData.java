package com.gackstone.chase.save;

import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates all persistent player data: settings, audio, graphics quality,
 * high score statistics, garage selection, and coin economy.
 */
public class GamePreferencesData {

    // ── Audio ─────────────────────────────────────────────────────────────────
    private float masterVolume = 1.0f;
    private float musicVolume  = 0.8f;
    private float sfxVolume    = 1.0f;
    private boolean isMuted    = false;

    // ── Display / Graphics ────────────────────────────────────────────────────
    private GameConfig.GraphicsQuality graphicsQuality = GameConfig.GraphicsQuality.HIGH;
    private boolean isDebugOverlayEnabled = false;

    // ── Statistics ────────────────────────────────────────────────────────────
    private long  highScore      = 0;
    private float highestDistance = 0.0f;
    private int   totalRuns      = 0;

    // ── Economy ───────────────────────────────────────────────────────────────
    private int coins = GameConfig.PLAYER_STARTING_COINS;

    // ── Garage / Environment selection ────────────────────────────────────────
    private String selectedCarId   = "";
    private String selectedThemeId = "";

    // ── Audio getters / setters ───────────────────────────────────────────────
    public float getMasterVolume()           { return masterVolume; }
    public void  setMasterVolume(float v)    { this.masterVolume = v; }

    public float getMusicVolume()            { return musicVolume; }
    public void  setMusicVolume(float v)     { this.musicVolume = v; }

    public float getSfxVolume()              { return sfxVolume; }
    public void  setSfxVolume(float v)       { this.sfxVolume = v; }

    public boolean isMuted()                 { return isMuted; }
    public void    setMuted(boolean muted)   { this.isMuted = muted; }

    // ── Graphics getters / setters ────────────────────────────────────────────
    public GameConfig.GraphicsQuality getGraphicsQuality()              { return graphicsQuality; }
    public void setGraphicsQuality(GameConfig.GraphicsQuality quality)  { this.graphicsQuality = quality; }

    public boolean isDebugOverlayEnabled()              { return isDebugOverlayEnabled; }
    public void    setDebugOverlayEnabled(boolean val)  { this.isDebugOverlayEnabled = val; }

    // ── Statistics getters / setters ──────────────────────────────────────────
    public long  getHighScore()                  { return highScore; }
    public void  setHighScore(long score)        { this.highScore = score; }

    public float getHighestDistance()            { return highestDistance; }
    public void  setHighestDistance(float d)     { this.highestDistance = d; }

    public int  getTotalRuns()                   { return totalRuns; }
    public void setTotalRuns(int runs)           { this.totalRuns = runs; }

    // ── Economy getters / setters ─────────────────────────────────────────────
    public int  getCoins()                       { return coins; }
    public void setCoins(int coins)              { this.coins = Math.max(0, coins); }
    public void addCoins(int amount)             { this.coins = Math.max(0, this.coins + amount); }
    public boolean spendCoins(int amount) {
        if (coins >= amount) { coins -= amount; return true; }
        return false;
    }

    // ── Selection getters / setters ───────────────────────────────────────────
    public String getSelectedCarId()             { return selectedCarId; }
    public void   setSelectedCarId(String id)    { this.selectedCarId = id != null ? id : ""; }

    public String getSelectedThemeId()           { return selectedThemeId; }
    public void   setSelectedThemeId(String id)  { this.selectedThemeId = id != null ? id : ""; }
}
