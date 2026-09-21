package com.gackstone.chase.save;

/**
 * Pluggable persistent storage abstraction.
 * Allows transparent swapping between Android SharedPreferences, desktop JSON,
 * memory mock (for unit testing), or future cloud persistence.
 */
public interface ISaveStorage {

    void putInt(String key, int value);
    int getInt(String key, int defaultValue);

    void putLong(String key, long value);
    long getLong(String key, long defaultValue);

    void putFloat(String key, float value);
    float getFloat(String key, float defaultValue);

    void putString(String key, String value);
    String getString(String key, String defaultValue);

    void putBoolean(String key, boolean value);
    boolean getBoolean(String key, boolean defaultValue);

    void flush();
}
