package com.gackstone.chase.android;

import android.content.Context;
import android.content.SharedPreferences;
import com.gackstone.chase.save.ISaveStorage;

/**
 * Android implementation of {@link ISaveStorage} utilizing Android {@link SharedPreferences}.
 */
public class AndroidPreferencesBridge implements ISaveStorage {

    private static final String PREF_FILE_NAME = "chase_game_preferences";
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public AndroidPreferencesBridge(Context context) {
        this.preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = preferences.edit();
        }
        return editor;
    }

    @Override
    public void putInt(String key, int value) {
        getEditor().putInt(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    @Override
    public void putLong(String key, long value) {
        getEditor().putLong(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    @Override
    public void putFloat(String key, float value) {
        getEditor().putFloat(key, value);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    @Override
    public void putString(String key, String value) {
        getEditor().putString(key, value);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public void flush() {
        if (editor != null) {
            editor.apply();
            editor = null;
        }
    }
}
