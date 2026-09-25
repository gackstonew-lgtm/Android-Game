package com.gackstone.chase.save;

import com.badlogic.gdx.utils.ObjectMap;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameConfig;

/**
 * Manages loading, saving, and syncing player preferences, upgrades, career ranks,
 * and high score telemetry via {@link ISaveStorage}.
 */
public class SaveManager {

    // ── Storage keys ──────────────────────────────────────────────────────────
    private static final String KEY_MASTER_VOL      = "pref_master_vol";
    private static final String KEY_MUSIC_VOL       = "pref_music_vol";
    private static final String KEY_SFX_VOL         = "pref_sfx_vol";
    private static final String KEY_MUTED           = "pref_muted";
    private static final String KEY_GRAPHICS        = "pref_graphics";
    private static final String KEY_DEBUG_ENABLED   = "pref_debug_enabled";
    private static final String KEY_CTRL_SCHEME     = "pref_ctrl_scheme";
    private static final String KEY_STEER_SENS      = "pref_steer_sens";
    private static final String KEY_HIGH_SCORE      = "stat_high_score";
    private static final String KEY_HIGH_DISTANCE   = "stat_high_distance";
    private static final String KEY_TOTAL_RUNS      = "stat_total_runs";
    private static final String KEY_NEAR_MISSES     = "stat_near_misses";
    private static final String KEY_TUTORIAL_DONE   = "stat_tutorial_done";
    private static final String KEY_CAREER_PTS      = "stat_career_pts";
    private static final String KEY_COINS           = "economy_coins";
    private static final String KEY_SELECTED_CAR    = "garage_selected_car";
    private static final String KEY_SELECTED_THEME  = "garage_selected_theme";

    private static final String[] UPGRADE_TYPES = { "engine", "handling", "armour", "nitro" };

    private final ISaveStorage       storage;
    private final GamePreferencesData data;

    public SaveManager(ISaveStorage storage) {
        this.storage = storage;
        this.data    = new GamePreferencesData();
        load();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistence
    // ──────────────────────────────────────────────────────────────────────────

    public void load() {
        if (storage == null) return;

        data.setMasterVolume(storage.getFloat(KEY_MASTER_VOL, 1.0f));
        data.setMusicVolume(storage.getFloat(KEY_MUSIC_VOL, 0.8f));
        data.setSfxVolume(storage.getFloat(KEY_SFX_VOL, 1.0f));
        data.setMuted(storage.getBoolean(KEY_MUTED, false));

        String graphicsName = storage.getString(KEY_GRAPHICS, GameConfig.GraphicsQuality.HIGH.name());
        try {
            data.setGraphicsQuality(GameConfig.GraphicsQuality.valueOf(graphicsName));
        } catch (Exception e) {
            data.setGraphicsQuality(GameConfig.GraphicsQuality.HIGH);
        }

        String ctrlName = storage.getString(KEY_CTRL_SCHEME, GameConfig.ControlScheme.TOUCH_DRAG.name());
        try {
            data.setControlScheme(GameConfig.ControlScheme.valueOf(ctrlName));
        } catch (Exception e) {
            data.setControlScheme(GameConfig.ControlScheme.TOUCH_DRAG);
        }

        data.setSteeringSensitivity(storage.getFloat(KEY_STEER_SENS, 1.0f));
        data.setDebugOverlayEnabled(storage.getBoolean(KEY_DEBUG_ENABLED, false));
        data.setHighScore(storage.getLong(KEY_HIGH_SCORE, 0L));
        data.setHighestDistance(storage.getFloat(KEY_HIGH_DISTANCE, 0.0f));
        data.setTotalRuns(storage.getInt(KEY_TOTAL_RUNS, 0));
        data.setNearMissesTotal(storage.getInt(KEY_NEAR_MISSES, 0));
        data.setTutorialCompleted(storage.getBoolean(KEY_TUTORIAL_DONE, false));
        data.setCareerRankPoints(storage.getInt(KEY_CAREER_PTS, 0));
        data.setCoins(storage.getInt(KEY_COINS, GameConfig.PLAYER_STARTING_COINS));
        data.setSelectedCarId(storage.getString(KEY_SELECTED_CAR, ""));
        data.setSelectedThemeId(storage.getString(KEY_SELECTED_THEME, ""));

        // Load Upgrade Tiers for all player cars
        for (CarDefinition car : CarRegistry.getPlayerCars()) {
            for (String type : UPGRADE_TYPES) {
                String key = "upgrade_" + car.getId() + "_" + type;
                int tier = storage.getInt(key, 0);
                data.setUpgradeTier(car.getId(), type, tier);
            }
        }
    }

    public void save() {
        if (storage == null) return;

        storage.putFloat(KEY_MASTER_VOL, data.getMasterVolume());
        storage.putFloat(KEY_MUSIC_VOL,  data.getMusicVolume());
        storage.putFloat(KEY_SFX_VOL,    data.getSfxVolume());
        storage.putBoolean(KEY_MUTED,    data.isMuted());
        storage.putString(KEY_GRAPHICS,  data.getGraphicsQuality().name());
        storage.putString(KEY_CTRL_SCHEME, data.getControlScheme().name());
        storage.putFloat(KEY_STEER_SENS, data.getSteeringSensitivity());
        storage.putBoolean(KEY_DEBUG_ENABLED, data.isDebugOverlayEnabled());
        storage.putLong(KEY_HIGH_SCORE,      data.getHighScore());
        storage.putFloat(KEY_HIGH_DISTANCE,  data.getHighestDistance());
        storage.putInt(KEY_TOTAL_RUNS,       data.getTotalRuns());
        storage.putInt(KEY_NEAR_MISSES,      data.getNearMissesTotal());
        storage.putBoolean(KEY_TUTORIAL_DONE, data.isTutorialCompleted());
        storage.putInt(KEY_CAREER_PTS,       data.getCareerRankPoints());
        storage.putInt(KEY_COINS,            data.getCoins());
        storage.putString(KEY_SELECTED_CAR,  data.getSelectedCarId());
        storage.putString(KEY_SELECTED_THEME, data.getSelectedThemeId());

        // Save Upgrades
        for (ObjectMap.Entry<String, Integer> entry : data.getAllUpgrades()) {
            storage.putInt("upgrade_" + entry.key, entry.value);
        }

        storage.flush();
    }

    /**
     * Records the outcome of a completed game run and computes career points.
     */
    public boolean recordRunResult(long score, float distance) {
        boolean isNewRecord = false;
        data.setTotalRuns(data.getTotalRuns() + 1);

        if (score > data.getHighScore()) {
            data.setHighScore(score);
            isNewRecord = true;
        }
        if (distance > data.getHighestDistance()) {
            data.setHighestDistance(distance);
        }

        // Reward coins based on distance and near-misses
        int earnedCoins = (int)(distance / 10.0f) + (data.getNearMissesTotal() * 5);
        data.addCoins(earnedCoins);
        data.addCareerRankPoints((int)(score / 100) + (int)(distance / 5));

        save();
        return isNewRecord;
    }

    public GamePreferencesData getData() { return data; }
}
