package com.gackstone.chase.save;

import com.badlogic.gdx.utils.ObjectMap;
import com.gackstone.chase.core.GameConfig;

/**
 * Encapsulates all persistent player data: audio, graphics, control preferences,
 * high score statistics, garage upgrades per vehicle, economy, and career ranks.
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

    // ── Controls ──────────────────────────────────────────────────────────────
    private GameConfig.ControlScheme controlScheme = GameConfig.ControlScheme.TOUCH_DRAG;
    private float steeringSensitivity = 1.0f;

    // ── Statistics & Progression ──────────────────────────────────────────────
    private long  highScore        = 0;
    private float highestDistance  = 0.0f;
    private int   totalRuns        = 0;
    private int   nearMissesTotal  = 0;
    private boolean tutorialCompleted = false;

    // ── Economy & Career ───────────────────────────────────────────────────────
    private int coins = GameConfig.PLAYER_STARTING_COINS;
    private int careerRankPoints = 0;

    // ── Garage / Environment selection ────────────────────────────────────────
    private String selectedCarId   = "";
    private String selectedThemeId = "";

    // ── Vehicle Upgrades Storage (carId_upgradeType -> tier 0..5) ─────────────
    private final ObjectMap<String, Integer> upgradeTiers = new ObjectMap<>();

    // ── Audio Getters / Setters ───────────────────────────────────────────────
    public float getMasterVolume()           { return masterVolume; }
    public void  setMasterVolume(float v)    { this.masterVolume = v; }

    public float getMusicVolume()            { return musicVolume; }
    public void  setMusicVolume(float v)     { this.musicVolume = v; }

    public float getSfxVolume()              { return sfxVolume; }
    public void  setSfxVolume(float v)       { this.sfxVolume = v; }

    public boolean isMuted()                 { return isMuted; }
    public void    setMuted(boolean muted)   { this.isMuted = muted; }

    // ── Graphics Getters / Setters ────────────────────────────────────────────
    public GameConfig.GraphicsQuality getGraphicsQuality()              { return graphicsQuality; }
    public void setGraphicsQuality(GameConfig.GraphicsQuality quality)  { this.graphicsQuality = quality; }

    public boolean isDebugOverlayEnabled()              { return isDebugOverlayEnabled; }
    public void    setDebugOverlayEnabled(boolean val)  { this.isDebugOverlayEnabled = val; }

    // ── Controls Getters / Setters ────────────────────────────────────────────
    public GameConfig.ControlScheme getControlScheme()             { return controlScheme; }
    public void setControlScheme(GameConfig.ControlScheme scheme)  { this.controlScheme = scheme; }

    public float getSteeringSensitivity()                          { return steeringSensitivity; }
    public void  setSteeringSensitivity(float sens)                { this.steeringSensitivity = Math.max(0.4f, Math.min(2.5f, sens)); }

    // ── Statistics Getters / Setters ──────────────────────────────────────────
    public long  getHighScore()                  { return highScore; }
    public void  setHighScore(long score)        { this.highScore = score; }

    public float getHighestDistance()            { return highestDistance; }
    public void  setHighestDistance(float d)     { this.highestDistance = d; }

    public int  getTotalRuns()                   { return totalRuns; }
    public void setTotalRuns(int runs)           { this.totalRuns = runs; }

    public int  getNearMissesTotal()             { return nearMissesTotal; }
    public void addNearMiss()                    { this.nearMissesTotal++; }
    public void setNearMissesTotal(int count)    { this.nearMissesTotal = count; }

    public boolean isTutorialCompleted()         { return tutorialCompleted; }
    public void setTutorialCompleted(boolean c)  { this.tutorialCompleted = c; }

    // ── Economy & Career Getters / Setters ────────────────────────────────────
    public int  getCoins()                       { return coins; }
    public void setCoins(int coins)              { this.coins = Math.max(0, coins); }
    public void addCoins(int amount)             { this.coins = Math.max(0, this.coins + amount); }
    public boolean spendCoins(int amount) {
        if (coins >= amount) { coins -= amount; return true; }
        return false;
    }

    public int  getCareerRankPoints()            { return careerRankPoints; }
    public void addCareerRankPoints(int pts)     { this.careerRankPoints += pts; }
    public void setCareerRankPoints(int pts)     { this.careerRankPoints = pts; }

    public String getCareerRankTitle() {
        if (careerRankPoints > 10000) return "APEX PHANTOM";
        if (careerRankPoints > 5000)  return "STREET OUTLAW";
        if (careerRankPoints > 2000)  return "PRO PURSUER";
        if (careerRankPoints > 500)   return "STREET RACER";
        return "ROOKIE DRIVER";
    }

    // ── Selection Getters / Setters ───────────────────────────────────────────
    public String getSelectedCarId()             { return selectedCarId; }
    public void   setSelectedCarId(String id)    { this.selectedCarId = id != null ? id : ""; }

    public String getSelectedThemeId()           { return selectedThemeId; }
    public void   setSelectedThemeId(String id)  { this.selectedThemeId = id != null ? id : ""; }

    // ── Vehicle Upgrades ──────────────────────────────────────────────────────
    public int getUpgradeTier(String carId, String upgradeType) {
        String key = carId + "_" + upgradeType;
        return upgradeTiers.get(key, 0);
    }

    public void setUpgradeTier(String carId, String upgradeType, int tier) {
        String key = carId + "_" + upgradeType;
        upgradeTiers.put(key, Math.max(0, Math.min(GameConfig.MAX_UPGRADE_TIER, tier)));
    }

    public ObjectMap<String, Integer> getAllUpgrades() {
        return upgradeTiers;
    }
}
