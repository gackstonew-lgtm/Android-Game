package com.gackstone.chase.core;

/**
 * Global game balance, physics, camera, display, and asset configuration.
 *
 * <p>All tunable constants live here so nothing is scattered as magic numbers
 * across the codebase. Systems reference these constants directly; changing a
 * single value here re-tunes the entire game.
 */
public final class GameConfig {

    private GameConfig() {}

    // ── Application ───────────────────────────────────────────────────────────
    public static final String GAME_NAME    = "Chase";
    public static final String GAME_VERSION = "2.0.0";

    // ── World / Road Geometry ─────────────────────────────────────────────────
    public static final float ROAD_WIDTH          = 14.0f;
    public static final float ROAD_SEGMENT_LENGTH = 100.0f;
    public static final int   ROAD_SEGMENTS_COUNT = 6;
    public static final float BOUNDARY_LIMIT_X    = ROAD_WIDTH / 2.0f - 1.2f;

    // ── Player Movement ───────────────────────────────────────────────────────
    public static final float PLAYER_INITIAL_FORWARD_SPEED  = 24.0f;
    public static final float PLAYER_MAX_FORWARD_SPEED      = 48.0f;
    public static final float PLAYER_FORWARD_ACCELERATION   = 0.8f;
    public static final float PLAYER_LATERAL_SPEED          = 18.0f;
    public static final float PLAYER_BANK_ANGLE_MAX         = 22.0f;
    public static final float PLAYER_BANK_SMOOTHING         = 12.0f;
    public static final float PLAYER_INITIAL_HEALTH         = 100.0f;

    // ── Enemy Pursuit ─────────────────────────────────────────────────────────
    public static final float ENEMY_INITIAL_DISTANCE_BEHIND = 22.0f;
    public static final float ENEMY_MIN_CAPTURE_DISTANCE    = 1.8f;
    public static final float ENEMY_BASE_SPEED_MULTIPLIER   = 1.06f;
    public static final float ENEMY_LATERAL_TRACKING_SPEED  = 9.0f;
    public static final float ENEMY_CATCHUP_BOOST_DISTANCE  = 35.0f;
    public static final float ENEMY_RECOVERY_TIME           = 1.2f;

    // ── Chase Camera ──────────────────────────────────────────────────────────
    public static final float CAMERA_DISTANCE      = 11.0f;
    public static final float CAMERA_HEIGHT        = 5.2f;
    public static final float CAMERA_LOOK_AHEAD    = 14.0f;
    public static final float CAMERA_FOLLOW_SPEED  = 7.5f;
    public static final float CAMERA_ROTATION_SPEED = 8.0f;
    public static final float CAMERA_FOV           = 67.0f;
    public static final float CAMERA_NEAR          = 0.5f;
    public static final float CAMERA_FAR           = 320.0f;

    // ── Scoring / Difficulty ──────────────────────────────────────────────────
    public static final float SCORE_PER_METER           = 1.0f;
    public static final float CLOSE_CALL_BONUS_SCORE    = 250.0f;
    public static final float SPEED_DIFFICULTY_SCALING  = 0.005f;

    // ── Virtual Viewport ─────────────────────────────────────────────────────
    public static final float VIRTUAL_WIDTH  = 1280.0f;
    public static final float VIRTUAL_HEIGHT = 720.0f;

    // ── Graphics Quality Presets ──────────────────────────────────────────────
    public enum GraphicsQuality { LOW, MEDIUM, HIGH }

    // ── Prop / Traffic Draw Distance ─────────────────────────────────────────
    /** Props beyond this distance ahead of the player are not spawned. */
    public static final float PROP_DRAW_DISTANCE    = 220.0f;
    /** Traffic vehicles beyond this distance behind player are recycled. */
    public static final float TRAFFIC_CULL_DISTANCE = 30.0f;

    // ── Garage / Economy ─────────────────────────────────────────────────────
    /** Starting coins awarded to new players so they can buy one upgrade immediately. */
    public static final int PLAYER_STARTING_COINS = 500;

    // ── Audio ─────────────────────────────────────────────────────────────────
    public static final float ENGINE_PITCH_MIN  = 0.8f;
    public static final float ENGINE_PITCH_MAX  = 2.0f;
    public static final float SIREN_VOLUME      = 0.65f;
}
