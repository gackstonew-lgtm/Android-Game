package com.gackstone.chase.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Unified camera controller for Chase.
 *
 * <p>Supports three modes via {@link CameraMode}:
 * <ul>
 *   <li><b>CHASE</b>  – Third-person trailing camera with position lerp and look-ahead.
 *   <li><b>HOOD</b>   – Low camera affixed just above the bonnet; no X-damping.
 *   <li><b>COCKPIT</b>– Eye-level camera inside the cabin, cockpit geometry rendered separately.
 * </ul>
 *
 * <p><b>Zero-allocation hot path</b>: {@code desiredPosition} and {@code currentLookTarget}
 * are pre-allocated and reused every frame.
 */
public class ChaseCamera implements com.gackstone.chase.core.GameEvents.GameEventListener {

    private final PerspectiveCamera camera;
    private final PlayerEntity targetPlayer;

    // ── Pre-allocated scratch vectors (no per-frame heap) ────────────────────
    private final Vector3 desiredPosition   = new Vector3();
    private final Vector3 currentLookTarget = new Vector3();
    private final Vector3 tmpVec            = new Vector3();

    // ── Screen Shake & Trauma ────────────────────────────────────────────────
    private float shakeTrauma = 0.0f;

    // ── Tunable parameters ────────────────────────────────────────────────────
    private float cameraDistance  = GameConfig.CAMERA_DISTANCE;
    private float cameraHeight    = GameConfig.CAMERA_HEIGHT;
    private float lookAhead       = GameConfig.CAMERA_LOOK_AHEAD;
    private float followSpeed     = GameConfig.CAMERA_FOLLOW_SPEED;
    private float rotationSpeed   = GameConfig.CAMERA_ROTATION_SPEED;

    // Hood-cam offsets (metres from player position)
    private static final float HOOD_HEIGHT   =  0.55f;
    private static final float HOOD_FORWARD  =  1.20f;

    // Cockpit offsets
    private static final float COCKPIT_HEIGHT  =  0.55f;
    private static final float COCKPIT_FORWARD =  0.30f;

    private CameraMode mode = CameraMode.CHASE;

    public ChaseCamera(PlayerEntity targetPlayer, float viewportWidth, float viewportHeight) {
        this.targetPlayer = targetPlayer;
        camera = new PerspectiveCamera(GameConfig.CAMERA_FOV, viewportWidth, viewportHeight);
        camera.near = GameConfig.CAMERA_NEAR;
        camera.far  = GameConfig.CAMERA_FAR;
        com.gackstone.chase.core.GameEvents.addListener(this);
        reset();
    }

    public void addShake(float trauma) {
        shakeTrauma = MathUtils.clamp(shakeTrauma + trauma, 0.0f, 1.0f);
    }

    @Override
    public void onPlayerDamaged(float currentHealth, float damageAmount) {
        addShake(0.45f);
    }

    @Override
    public void onObstacleHit(float damage) {
        addShake(0.35f);
    }

    @Override
    public void onPlayerCaught(float finalDistance, long finalScore) {
        addShake(0.85f);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /** Cycles to the next camera mode: CHASE → HOOD → COCKPIT → CHASE. */
    public void cycleMode() {
        switch (mode) {
            case CHASE:   setMode(CameraMode.HOOD);    break;
            case HOOD:    setMode(CameraMode.COCKPIT); break;
            case COCKPIT: setMode(CameraMode.CHASE);   break;
        }
    }

    public void setMode(CameraMode newMode) {
        this.mode = newMode;
        reset();
    }

    public CameraMode getMode() { return mode; }

    public void update(float delta) {
        if (targetPlayer == null) return;

        Vector3 playerPos  = targetPlayer.getPosition();
        float   bankAngle  = targetPlayer.getState().getCurrentBankAngle();
        float   steerInput = targetPlayer.getState().getCurrentSteerAngle();

        switch (mode) {
            case CHASE:   updateChase(delta, playerPos, steerInput);   break;
            case HOOD:    updateHood(delta, playerPos, bankAngle);      break;
            case COCKPIT: updateCockpit(delta, playerPos, bankAngle);   break;
        }

        // Apply screen shake trauma
        if (shakeTrauma > 0.0f) {
            float shakeMagnitude = shakeTrauma * shakeTrauma * 0.45f;
            float shakeX = (MathUtils.random() * 2.0f - 1.0f) * shakeMagnitude;
            float shakeY = (MathUtils.random() * 2.0f - 1.0f) * shakeMagnitude;
            camera.position.add(shakeX, shakeY, 0);
            shakeTrauma = Math.max(0.0f, shakeTrauma - delta * 1.8f);
        }

        camera.up.set(Vector3.Y);
        camera.update();
    }

    public void resize(int width, int height) {
        camera.viewportWidth  = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void reset() {
        if (targetPlayer == null) return;
        Vector3 pos = targetPlayer.getPosition();
        camera.position.set(pos.x, pos.y + cameraHeight, pos.z - cameraDistance);
        currentLookTarget.set(pos.x, pos.y + 0.8f, pos.z + lookAhead);
        camera.lookAt(currentLookTarget);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mode implementations
    // ──────────────────────────────────────────────────────────────────────────

    private void updateChase(float delta, Vector3 playerPos, float steerInput) {
        // Slight lateral offset so camera tracks player lean
        float lateralDamp = 0.7f;
        desiredPosition.set(
            playerPos.x * lateralDamp,
            playerPos.y + cameraHeight,
            playerPos.z - cameraDistance
        );
        camera.position.lerp(desiredPosition, Math.min(1.0f, delta * followSpeed));

        tmpVec.set(playerPos.x * 0.5f, playerPos.y + 0.8f, playerPos.z + lookAhead);
        currentLookTarget.lerp(tmpVec, Math.min(1.0f, delta * rotationSpeed));
        camera.lookAt(currentLookTarget);
    }

    private void updateHood(float delta, Vector3 playerPos, float bankAngle) {
        // Camera glued to bonnet; very responsive (high lerp factor)
        desiredPosition.set(playerPos.x, playerPos.y + HOOD_HEIGHT, playerPos.z + HOOD_FORWARD);
        camera.position.lerp(desiredPosition, Math.min(1.0f, delta * 22.0f));

        tmpVec.set(playerPos.x, playerPos.y + HOOD_HEIGHT * 0.6f, playerPos.z + lookAhead * 1.5f);
        currentLookTarget.lerp(tmpVec, Math.min(1.0f, delta * 20.0f));
        camera.lookAt(currentLookTarget);
    }

    private void updateCockpit(float delta, Vector3 playerPos, float bankAngle) {
        // Eye-level inside cabin
        desiredPosition.set(
            playerPos.x,
            playerPos.y + COCKPIT_HEIGHT,
            playerPos.z + COCKPIT_FORWARD
        );
        camera.position.lerp(desiredPosition, Math.min(1.0f, delta * 25.0f));

        // Look toward vanishing point; slight vertical bob with bank
        float lookY = playerPos.y + COCKPIT_HEIGHT * 0.5f + MathUtils.sin(bankAngle * MathUtils.degreesToRadians) * 0.12f;
        tmpVec.set(playerPos.x, lookY, playerPos.z + lookAhead * 2.0f);
        currentLookTarget.lerp(tmpVec, Math.min(1.0f, delta * 22.0f));
        camera.lookAt(currentLookTarget);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────────

    public PerspectiveCamera getPerspectiveCamera() { return camera; }
    public float getCameraDistance()                { return cameraDistance; }
    public void  setCameraDistance(float d)         { this.cameraDistance = d; }
    public float getCameraHeight()                  { return cameraHeight; }
    public void  setCameraHeight(float h)           { this.cameraHeight = h; }
    public float getFollowSpeed()                   { return followSpeed; }
    public void  setFollowSpeed(float s)            { this.followSpeed = s; }
    public float getLookAhead()                     { return lookAhead; }
    public void  setLookAhead(float la)             { this.lookAhead = la; }
}
