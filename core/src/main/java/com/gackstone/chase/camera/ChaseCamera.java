package com.gackstone.chase.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Dedicated 3D third-person chase camera.
 * Features smooth position dampening, predictive look-ahead, lateral tilt dampening,
 * and FOV management across multiple mobile aspect ratios.
 */
public class ChaseCamera {

    private final PerspectiveCamera perspectiveCamera;
    private final PlayerEntity targetPlayer;

    // Configurable parameters
    private float cameraDistance = GameConfig.CAMERA_DISTANCE;
    private float cameraHeight = GameConfig.CAMERA_HEIGHT;
    private float lookAhead = GameConfig.CAMERA_LOOK_AHEAD;
    private float followSpeed = GameConfig.CAMERA_FOLLOW_SPEED;
    private float rotationSpeed = GameConfig.CAMERA_ROTATION_SPEED;

    // Smooth tracking vectors
    private final Vector3 currentLookTarget = new Vector3();
    private final Vector3 desiredPosition = new Vector3();

    public ChaseCamera(PlayerEntity targetPlayer, float viewportWidth, float viewportHeight) {
        this.targetPlayer = targetPlayer;
        
        perspectiveCamera = new PerspectiveCamera(
            GameConfig.CAMERA_FOV,
            viewportWidth,
            viewportHeight
        );
        perspectiveCamera.near = GameConfig.CAMERA_NEAR;
        perspectiveCamera.far = GameConfig.CAMERA_FAR;

        reset();
    }

    public void update(float delta) {
        if (targetPlayer == null) return;

        Vector3 playerPos = targetPlayer.getPosition();

        // 1. Calculate desired camera position behind player
        desiredPosition.set(
            playerPos.x * 0.7f, // Dampen lateral camera movement to prevent motion sickness
            playerPos.y + cameraHeight,
            playerPos.z - cameraDistance
        );

        // 2. Smoothly interpolate current camera position toward desired position
        float lerpFactor = Math.min(1.0f, delta * followSpeed);
        perspectiveCamera.position.lerp(desiredPosition, lerpFactor);

        // 3. Calculate look-at target with forward look-ahead
        Vector3 desiredLookTarget = new Vector3(
            playerPos.x * 0.5f,
            playerPos.y + 0.8f,
            playerPos.z + lookAhead
        );
        currentLookTarget.lerp(desiredLookTarget, Math.min(1.0f, delta * rotationSpeed));

        // 4. Orient camera toward target
        perspectiveCamera.lookAt(currentLookTarget);
        perspectiveCamera.up.set(Vector3.Y);
        perspectiveCamera.update();
    }

    public void resize(int width, int height) {
        perspectiveCamera.viewportWidth = width;
        perspectiveCamera.viewportHeight = height;
        perspectiveCamera.update();
    }

    public void reset() {
        if (targetPlayer != null) {
            Vector3 playerPos = targetPlayer.getPosition();
            perspectiveCamera.position.set(
                playerPos.x,
                playerPos.y + cameraHeight,
                playerPos.z - cameraDistance
            );
            currentLookTarget.set(playerPos.x, playerPos.y + 0.8f, playerPos.z + lookAhead);
            perspectiveCamera.lookAt(currentLookTarget);
            perspectiveCamera.up.set(Vector3.Y);
            perspectiveCamera.update();
        }
    }

    public PerspectiveCamera getPerspectiveCamera() {
        return perspectiveCamera;
    }

    public float getCameraDistance() {
        return cameraDistance;
    }

    public void setCameraDistance(float cameraDistance) {
        this.cameraDistance = cameraDistance;
    }

    public float getCameraHeight() {
        return cameraHeight;
    }

    public void setCameraHeight(float cameraHeight) {
        this.cameraHeight = cameraHeight;
    }

    public float getFollowSpeed() {
        return followSpeed;
    }

    public void setFollowSpeed(float followSpeed) {
        this.followSpeed = followSpeed;
    }

    public float getLookAhead() {
        return lookAhead;
    }

    public void setLookAhead(float lookAhead) {
        this.lookAhead = lookAhead;
    }
}
