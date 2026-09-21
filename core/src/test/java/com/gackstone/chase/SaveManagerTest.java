package com.gackstone.chase;

import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.save.SaveManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {

    private MockSaveStorage mockStorage;
    private SaveManager saveManager;

    @BeforeEach
    public void setUp() {
        mockStorage = new MockSaveStorage();
        saveManager = new SaveManager(mockStorage);
    }

    @Test
    public void testDefaultPreferences() {
        assertEquals(1.0f, saveManager.getData().getMasterVolume());
        assertEquals(0.8f, saveManager.getData().getMusicVolume());
        assertEquals(1.0f, saveManager.getData().getSfxVolume());
        assertFalse(saveManager.getData().isMuted());
        assertEquals(GameConfig.GraphicsQuality.HIGH, saveManager.getData().getGraphicsQuality());
        assertEquals(0, saveManager.getData().getHighScore());
    }

    @Test
    public void testUpdateAndPersistPreferences() {
        saveManager.getData().setMasterVolume(0.5f);
        saveManager.getData().setGraphicsQuality(GameConfig.GraphicsQuality.MEDIUM);
        saveManager.save();

        // Reload fresh manager using same storage backend
        SaveManager reloaded = new SaveManager(mockStorage);
        assertEquals(0.5f, reloaded.getData().getMasterVolume(), 0.001f);
        assertEquals(GameConfig.GraphicsQuality.MEDIUM, reloaded.getData().getGraphicsQuality());
    }

    @Test
    public void testRecordRunResultHighScore() {
        boolean firstRecord = saveManager.recordRunResult(1200L, 350.0f);
        assertTrue(firstRecord);
        assertEquals(1200L, saveManager.getData().getHighScore());
        assertEquals(350.0f, saveManager.getData().getHighestDistance(), 0.01f);
        assertEquals(1, saveManager.getData().getTotalRuns());

        // Lower score run should not be a new record
        boolean lowerRecord = saveManager.recordRunResult(800L, 200.0f);
        assertFalse(lowerRecord);
        assertEquals(1200L, saveManager.getData().getHighScore());
        assertEquals(2, saveManager.getData().getTotalRuns());

        // Higher score run sets new record
        boolean higherRecord = saveManager.recordRunResult(2500L, 600.0f);
        assertTrue(higherRecord);
        assertEquals(2500L, saveManager.getData().getHighScore());
        assertEquals(600.0f, saveManager.getData().getHighestDistance(), 0.01f);
        assertEquals(3, saveManager.getData().getTotalRuns());
    }
}
