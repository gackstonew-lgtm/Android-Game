package com.gackstone.chase.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.player.PlayerEntity;

/**
 * Intelligent pursuit controller governing enemy AI behaviour.
 *
 * <p><b>Physics model (arcade, zero-allocation):</b>
 * <ul>
 *   <li>Forward motion is computed each tick; no accumulated velocity object.
 *   <li>Lateral tracking: proportional steering toward the player's X position,
 *       lerped for smooth appearance.
 *   <li>Speed modulation: farther behind → stronger catch-up multiplier;
 *       near player → matches player speed plus pursuit pressure.
 *   <li>Bank angle mirrors lateral steer rate for visual polish.
 * </ul>
 */
public class ChaseController {

    private final EnemyEntity enemy;
    private final PlayerEntity targetPlayer;

    private float recoveryTimer = 0.0f;
    private float sideAttackTimer = 0.0f;
    private static final float SIDE_ATTACK_COOLDOWN = 6.0f;

    public ChaseController(EnemyEntity enemy, PlayerEntity targetPlayer) {
        this.enemy = enemy;
        this.targetPlayer = targetPlayer;
    }

    public void update(float delta) {
        if (!enemy.isActive() || enemy.getAiState() == ChaseAIState.DISABLED) return;

        Vector3 enemyPos  = enemy.getPosition();
        Vector3 playerPos = targetPlayer.getPosition();
        float   playerSpeed  = targetPlayer.getState().getForwardSpeed();

        float distanceZ  = playerPos.z - enemyPos.z;
        float lateralDiff = playerPos.x - enemyPos.x;

        // Cooldown for side-ram attack
        if (sideAttackTimer > 0) sideAttackTimer -= delta;

        switch (enemy.getAiState()) {
            case IDLE:
                if (distanceZ > -5.0f && distanceZ < 60.0f) enemy.setAiState(ChaseAIState.CHASING);
                break;

            case SEARCHING:
                if (Math.abs(distanceZ) < 70.0f) enemy.setAiState(ChaseAIState.CHASING);
                break;

            case CHASING:
                float targetSpeed;
                if (distanceZ > GameConfig.ENEMY_CATCHUP_BOOST_DISTANCE) {
                    targetSpeed = playerSpeed * 1.30f;          // aggressive catch-up
                } else if (distanceZ > 8.0f) {
                    targetSpeed = playerSpeed * GameConfig.ENEMY_BASE_SPEED_MULTIPLIER;
                } else {
                    targetSpeed = playerSpeed * 1.02f;          // sustain close pursuit
                }
                enemy.setChaseSpeed(MathUtils.lerp(enemy.getChaseSpeed(), targetSpeed,
                        Math.min(1.0f, delta * 3.5f)));

                // Lateral tracking – sigmoid-smoothed
                float steerX = MathUtils.clamp(lateralDiff * 2.2f, -1.0f, 1.0f);
                enemyPos.x += steerX * GameConfig.ENEMY_LATERAL_TRACKING_SPEED * delta;
                enemyPos.x  = MathUtils.clamp(enemyPos.x, -GameConfig.BOUNDARY_LIMIT_X, GameConfig.BOUNDARY_LIMIT_X);

                // Propagate visual bank angle
                enemy.setBankAngle(MathUtils.lerp(enemy.getBankAngle(), -steerX * 12.0f,
                        Math.min(1.0f, delta * 10.0f)));

                // Forward integration
                enemyPos.z += enemy.getChaseSpeed() * delta;

                // Side-ram attack when very close and aligned
                if (Math.abs(lateralDiff) < 1.5f && distanceZ < 4.0f && sideAttackTimer <= 0) {
                    float ramDir = MathUtils.randomSign();
                    enemyPos.x += ramDir * 0.6f;
                    sideAttackTimer = SIDE_ATTACK_COOLDOWN;
                }

                // Dodge evade detection → recovery state
                if (Math.abs(lateralDiff) > 4.5f && distanceZ < 6.0f) {
                    enemy.setAiState(ChaseAIState.RECOVERING);
                    recoveryTimer = GameConfig.ENEMY_RECOVERY_TIME;
                }
                if (distanceZ > 90.0f) enemy.setAiState(ChaseAIState.LOST_TARGET);
                break;

            case RECOVERING:
                enemyPos.z += (playerSpeed * 0.82f) * delta;
                recoveryTimer -= delta;
                if (recoveryTimer <= 0.0f) enemy.setAiState(ChaseAIState.CHASING);
                break;

            case LOST_TARGET:
                enemyPos.z += (playerSpeed * 1.35f) * delta;
                if (distanceZ < 50.0f) enemy.setAiState(ChaseAIState.CHASING);
                break;

            case DISABLED:
            default:
                break;
        }

        enemy.update(delta);
    }

    public EnemyEntity getEnemy() { return enemy; }

    public float getDistanceToPlayer() {
        return targetPlayer.getPosition().z - enemy.getPosition().z;
    }
}
