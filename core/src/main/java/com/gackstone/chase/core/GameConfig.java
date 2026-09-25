package com.gackstone.chase.core;

/**
 * Global game balance, physics, camera, display, upgrade tuning, and asset configuration.
 */
public final class GameConfig {

    private GameConfig() {}

    // ── Application ───────────────────────────────────────────────────────────
    public static final String GAME_NAME    = "Chase";
    public static final String GAME_VERSION = "2.1.0";

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
    public static final float CAMERA_FOV_BOOST     = 78.0f;
    public static final float CAMERA_NEAR          = 0.5f;
    public static final float CAMERA_FAR           = 320.0f;

    // ── Scoring / Difficulty / Near-Miss ─────────────────────────────────────
    public static final float SCORE_PER_METER           = 1.0f;
    public static final float CLOSE_CALL_BONUS_SCORE    = 250.0f;
    public static final float NEAR_MISS_LATERAL_DIST    = 2.2f;
    public static final float NEAR_MISS_FORWARD_DIST    = 3.8f;
    public static final float SPEED_DIFFICULTY_SCALING  = 0.005f;

    // ── Upgrades Tuning (0 - 5 tiers) ─────────────────────────────────────────
    public static final int   MAX_UPGRADE_TIER          = 5;
    public static final int   UPGRADE_COST_BASE         = 300;
    public static final int   UPGRADE_COST_PER_TIER     = 250;
    public static final float UPGRADE_SPEED_BOOST_PER_TIER   = 0.04f; // +4% max speed per tier
    public static final float UPGRADE_ACCEL_BOOST_PER_TIER   = 0.06f; // +6% accel per tier
    public static final float UPGRADE_HANDLING_BOOST_PER_TIER= 0.05f; // +5% grip per tier
    public static final float UPGRADE_ARMOUR_BOOST_PER_TIER  = 0.08f; // +8% max hp per tier
    public static final float UPGRADE_NITRO_BOOST_PER_TIER   = 0.10f; // +10% nitro capacity/regen per tier

    // ── Virtual Viewport ─────────────────────────────────────────────────────
    public static final float VIRTUAL_WIDTH  = 1280.0f;
    public static final float VIRTUAL_HEIGHT = 720.0f;

    // ── Graphics Quality Presets ──────────────────────────────────────────────
    public enum GraphicsQuality { LOW, MEDIUM, HIGH }

    // ── Control Scheme ────────────────────────────────────────────────────────
    public enum ControlScheme { TOUCH_DRAG, TOUCH_BUTTONS, TILT }

    // ── Prop / Traffic Draw Distance ─────────────────────────────────────────
    public static final float PROP_DRAW_DISTANCE    = 220.0f;
    public static final float TRAFFIC_CULL_DISTANCE = 30.0f;

    // ── Garage / Economy ─────────────────────────────────────────────────────
    public static final int PLAYER_STARTING_COINS = 500;

    // ── Audio ─────────────────────────────────────────────────────────────────
    public static final float ENGINE_PITCH_MIN  = 0.8f;
    public static final float ENGINE_PITCH_MAX  = 2.0f;
    public static final float SIREN_VOLUME      = 0.65f;
}
