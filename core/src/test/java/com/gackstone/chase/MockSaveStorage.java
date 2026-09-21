package com.gackstone.chase;

import com.gackstone.chase.save.ISaveStorage;
import java.util.HashMap;
import java.util.Map;

public class MockSaveStorage implements ISaveStorage {

    private final Map<String, Object> storage = new HashMap<>();

    @Override
    public void putInt(String key, int value) {
        storage.put(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object val = storage.get(key);
        return val instanceof Integer ? (Integer) val : defaultValue;
    }

    @Override
    public void putLong(String key, long value) {
        storage.put(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object val = storage.get(key);
        return val instanceof Long ? (Long) val : defaultValue;
    }

    @Override
    public void putFloat(String key, float value) {
        storage.put(key, value);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        Object val = storage.get(key);
        return val instanceof Float ? (Float) val : defaultValue;
    }

    @Override
    public void putString(String key, String value) {
        storage.put(key, value);
    }

    @Override
    public String getString(String key, String defaultValue) {
        Object val = storage.get(key);
        return val instanceof String ? (String) val : defaultValue;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        storage.put(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = storage.get(key);
        return val instanceof Boolean ? (Boolean) val : defaultValue;
    }

    @Override
    public void flush() {
        // In-memory no-op
    }
}
