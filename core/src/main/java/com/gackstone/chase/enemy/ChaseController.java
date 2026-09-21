package com.gackstone.chase.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Intelligent pursuit controller governing enemy behavior, target tracking,
 * dynamic speed modulation, recovery from evasive maneuvers, and capture triggers.
 */
public class ChaseController {

    private final EnemyEntity enemy;
    private final PlayerEntity targetPlayer;
    private float recoveryTimer = 0.0f;

    public ChaseController(EnemyEntity enemy, PlayerEntity targetPlayer) {
        this.enemy = enemy;
        this.targetPlayer = targetPlayer;
    }

    public void update(float delta) {
        if (!enemy.isActive() || enemy.getAiState() == ChaseAIState.DISABLED) {
            return;
        }

        Vector3 enemyPos = enemy.getPosition();
        Vector3 playerPos = targetPlayer.getPosition();
        float playerForwardSpeed = targetPlayer.getState().getForwardSpeed();

        // Calculate distance delta (z-axis along road and Euclidean)
        float distanceZ = playerPos.z - enemyPos.z;
        float lateralDiff = playerPos.x - enemyPos.x;

        switch (enemy.getAiState()) {
            case IDLE:
                // Standby until player passes or triggers proximity
                if (distanceZ > -5.0f && distanceZ < 60.0f) {
                    enemy.setAiState(ChaseAIState.CHASING);
                }
                break;

            case SEARCHING:
                // Re-orient toward last known player lane
                if (Math.abs(distanceZ) < 70.0f) {
                    enemy.setAiState(ChaseAIState.CHASING);
                }
                break;

            case CHASING:
                // 1. Dynamic speed modulation:
                // When far behind, chaser gets a catch-up boost.
                // When close, chaser matches player speed + slight pursuit pressure.
                float targetSpeed;
                if (distanceZ > GameConfig.ENEMY_CATCHUP_BOOST_DISTANCE) {
                    targetSpeed = playerForwardSpeed * 1.25f;
                } else if (distanceZ > 5.0f) {
                    targetSpeed = playerForwardSpeed * GameConfig.ENEMY_BASE_SPEED_MULTIPLIER;
                } else {
                    targetSpeed = playerForwardSpeed * 1.02f;
                }

                enemy.setChaseSpeed(MathUtils.lerp(enemy.getChaseSpeed(), targetSpeed, Math.min(1.0f, delta * 3.0f)));

                // 2. Lateral tracking (smoothing follows player's lane/X position)
                float steerX = MathUtils.clamp(lateralDiff * 2.0f, -1.0f, 1.0f);
                enemyPos.x += steerX * GameConfig.ENEMY_LATERAL_TRACKING_SPEED * delta;
                enemyPos.x = MathUtils.clamp(enemyPos.x, -GameConfig.BOUNDARY_LIMIT_X, GameConfig.BOUNDARY_LIMIT_X);

                // 3. Forward integration
                enemyPos.z += enemy.getChaseSpeed() * delta;

                // 4. Evasion check: if player performs sudden extreme lateral dodge
                if (Math.abs(lateralDiff) > 4.5f && distanceZ < 6.0f) {
                    enemy.setAiState(ChaseAIState.RECOVERING);
                    recoveryTimer = GameConfig.ENEMY_RECOVERY_TIME;
                }

                // If player is drastically far ahead, state becomes LOST_TARGET
                if (distanceZ > 90.0f) {
                    enemy.setAiState(ChaseAIState.LOST_TARGET);
                }
                break;

            case RECOVERING:
                // Temporarily slow down after being dodged
                enemyPos.z += (playerForwardSpeed * 0.85f) * delta;
                recoveryTimer -= delta;
                if (recoveryTimer <= 0.0f) {
                    enemy.setAiState(ChaseAIState.CHASING);
                }
                break;

            case LOST_TARGET:
                // Catch up steadily to reacquire target
                enemyPos.z += (playerForwardSpeed * 1.3f) * delta;
                if (distanceZ < 50.0f) {
                    enemy.setAiState(ChaseAIState.CHASING);
                }
                break;

            case DISABLED:
            default:
                break;
        }

        enemy.update(delta);
    }

    public EnemyEntity getEnemy() {
        return enemy;
    }

    public float getDistanceToPlayer() {
        return targetPlayer.getPosition().z - enemy.getPosition().z;
    }
}
