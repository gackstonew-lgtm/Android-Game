package com.gackstone.chase.save;

import com.gackstone.chase.core.GameConfig;

/**
 * Manages loading, saving, and syncing player preferences and records.
 */
public class SaveManager {

    private static final String KEY_MASTER_VOL = "pref_master_vol";
    private static final String KEY_MUSIC_VOL = "pref_music_vol";
    private static final String KEY_SFX_VOL = "pref_sfx_vol";
    private static final String KEY_MUTED = "pref_muted";
    private static final String KEY_GRAPHICS = "pref_graphics";
    private static final String KEY_DEBUG_ENABLED = "pref_debug_enabled";
    private static final String KEY_HIGH_SCORE = "stat_high_score";
    private static final String KEY_HIGH_DISTANCE = "stat_high_distance";
    private static final String KEY_TOTAL_RUNS = "stat_total_runs";

    private final ISaveStorage storage;
    private final GamePreferencesData data;

    public SaveManager(ISaveStorage storage) {
        this.storage = storage;
        this.data = new GamePreferencesData();
        load();
    }

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

        data.setDebugOverlayEnabled(storage.getBoolean(KEY_DEBUG_ENABLED, false));
        data.setHighScore(storage.getLong(KEY_HIGH_SCORE, 0L));
        data.setHighestDistance(storage.getFloat(KEY_HIGH_DISTANCE, 0.0f));
        data.setTotalRuns(storage.getInt(KEY_TOTAL_RUNS, 0));
    }

    public void save() {
        if (storage == null) return;

        storage.putFloat(KEY_MASTER_VOL, data.getMasterVolume());
        storage.putFloat(KEY_MUSIC_VOL, data.getMusicVolume());
        storage.putFloat(KEY_SFX_VOL, data.getSfxVolume());
        storage.putBoolean(KEY_MUTED, data.isMuted());
        storage.putString(KEY_GRAPHICS, data.getGraphicsQuality().name());
        storage.putBoolean(KEY_DEBUG_ENABLED, data.isDebugOverlayEnabled());
        storage.putLong(KEY_HIGH_SCORE, data.getHighScore());
        storage.putFloat(KEY_HIGH_DISTANCE, data.getHighestDistance());
        storage.putInt(KEY_TOTAL_RUNS, data.getTotalRuns());
        storage.flush();
    }

    /**
     * Records the outcome of a game run, updating high scores if beaten.
     * Returns true if a new high score was set.
     */
    public boolean recordRunResult(long score, float distance) {
        boolean isNewHighScore = false;
        data.setTotalRuns(data.getTotalRuns() + 1);

        if (score > data.getHighScore()) {
            data.setHighScore(score);
            isNewHighScore = true;
        }
        if (distance > data.getHighestDistance()) {
            data.setHighestDistance(distance);
        }

        save();
        return isNewHighScore;
    }

    public GamePreferencesData getData() {
        return data;
    }
}
