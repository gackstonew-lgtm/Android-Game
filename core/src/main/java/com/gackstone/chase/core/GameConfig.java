package com.gackstone.chase.core;

/**
 * Global game balance, physics, camera, and display parameters.
 * Centralizing these values allows tuning without searching across systems.
 */
public final class GameConfig {

    private GameConfig() {}

    // Application & Version
    public static final String GAME_NAME = "Chase";
    public static final String GAME_VERSION = "1.0.0";

    // World & Road Geometry
    public static final float ROAD_WIDTH = 14.0f;
    public static final float ROAD_SEGMENT_LENGTH = 100.0f;
    public static final int ROAD_SEGMENTS_COUNT = 5;
    public static final float BOUNDARY_LIMIT_X = ROAD_WIDTH / 2.0f - 1.2f;

    // Player Movement Parameters
    public static final float PLAYER_INITIAL_FORWARD_SPEED = 24.0f;
    public static final float PLAYER_MAX_FORWARD_SPEED = 48.0f;
    public static final float PLAYER_FORWARD_ACCELERATION = 0.8f;
    public static final float PLAYER_LATERAL_SPEED = 18.0f;
    public static final float PLAYER_BANK_ANGLE_MAX = 22.0f;
    public static final float PLAYER_BANK_SMOOTHING = 12.0f;
    public static final float PLAYER_INITIAL_HEALTH = 100.0f;

    // Enemy Pursuit Parameters
    public static final float ENEMY_INITIAL_DISTANCE_BEHIND = 22.0f;
    public static final float ENEMY_MIN_CAPTURE_DISTANCE = 1.8f;
    public static final float ENEMY_BASE_SPEED_MULTIPLIER = 1.05f;
    public static final float ENEMY_LATERAL_TRACKING_SPEED = 8.5f;
    public static final float ENEMY_CATCHUP_BOOST_DISTANCE = 35.0f;
    public static final float ENEMY_RECOVERY_TIME = 1.2f;

    // Chase Camera Parameters
    public static final float CAMERA_DISTANCE = 11.0f;
    public static final float CAMERA_HEIGHT = 5.2f;
    public static final float CAMERA_LOOK_AHEAD = 14.0f;
    public static final float CAMERA_FOLLOW_SPEED = 7.5f;
    public static final float CAMERA_ROTATION_SPEED = 8.0f;
    public static final float CAMERA_FOV = 67.0f;
    public static final float CAMERA_NEAR = 0.5f;
    public static final float CAMERA_FAR = 300.0f;

    // Scoring & Difficulty
    public static final float SCORE_PER_METER = 1.0f;
    public static final float CLOSE_CALL_BONUS_SCORE = 250.0f;
    public static final float SPEED_DIFFICULTY_SCALING = 0.005f;

    // Mobile Target Display Resolutions (Viewport sizing)
    public static final float VIRTUAL_WIDTH = 1280.0f;
    public static final float VIRTUAL_HEIGHT = 720.0f;

    // Graphics Quality Presets
    public enum GraphicsQuality {
        LOW,
        MEDIUM,
        HIGH
    }
}
