package com.gackstone.chase.enemy;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * 3D Physical Chaser entity pursuing the Player.
 * Loaded from ModelRegistry; no procedural boxes.
 */
public class EnemyEntity implements ICollidable {

    /** Half-extents used for AABB collision (based on a typical patrol cruiser). */
    public static final float BODY_WIDTH  = 1.6f;
    public static final float BODY_HEIGHT = 0.9f;
    public static final float BODY_LENGTH = 3.2f;

    private final ModelRegistry modelRegistry;
    private final Vector3 position = new Vector3(0, 0.45f, -20.0f);
    private final Vector3 velocity = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();

    /** Visual tilt while turning (mirrors player banking logic). */
    private float bankAngle = 0.0f;

    private ChaseAIState aiState = ChaseAIState.CHASING;
    private float chaseSpeed = 24.0f;
    private boolean active = true;

    private CarDefinition carDefinition;
    private ModelInstance modelInstance;

    private float sirenTimer = 0.0f;
    private com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute sirenRedAttr;
    private com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute sirenBlueAttr;

    public EnemyEntity(ModelRegistry modelRegistry, CarDefinition carDefinition) {
        this.modelRegistry = modelRegistry;
        this.carDefinition = carDefinition;

        // Load 3D model from registry (no ModelBuilder.createBox)
        if (modelRegistry != null && carDefinition != null) {
            this.modelInstance = modelRegistry.createInstance(carDefinition.getModelKey());
            initSirenAttributes();
        }

        updateBoundingBox();
    }

    private void initSirenAttributes() {
        if (modelInstance == null) return;
        com.badlogic.gdx.graphics.g3d.Material redMat = modelInstance.getMaterial("siren_red");
        if (redMat != null) {
            sirenRedAttr = (com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute) redMat.get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Emissive);
        }
        com.badlogic.gdx.graphics.g3d.Material blueMat = modelInstance.getMaterial("siren_blue");
        if (blueMat != null) {
            sirenBlueAttr = (com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute) blueMat.get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Emissive);
        }
    }

    public void update(float delta) {
        if (!active) return;

        // Strobe siren lightbars (alternating red / blue)
        sirenTimer += delta * 14.0f;
        boolean redActive = ((int) sirenTimer) % 2 == 0;
        if (sirenRedAttr != null) {
            sirenRedAttr.color.set(redActive ? com.badlogic.gdx.graphics.Color.RED : com.badlogic.gdx.graphics.Color.BLACK);
        }
        if (sirenBlueAttr != null) {
            sirenBlueAttr.color.set(redActive ? com.badlogic.gdx.graphics.Color.BLACK : com.badlogic.gdx.graphics.Color.CYAN);
        }

        if (modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.setToTranslation(position);
            if (bankAngle != 0) {
                modelInstance.transform.rotate(Vector3.Z, bankAngle);
            }
        }
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        boundingBox.set(
            new Vector3(position.x - BODY_WIDTH * 0.5f,  position.y - BODY_HEIGHT * 0.5f, position.z - BODY_LENGTH * 0.5f),
            new Vector3(position.x + BODY_WIDTH * 0.5f,  position.y + BODY_HEIGHT * 0.5f, position.z + BODY_LENGTH * 0.5f)
        );
    }

    public void render(ModelBatch batch, Environment environment) {
        if (active && modelInstance != null) {
            batch.render(modelInstance, environment);
        }
    }

    @Override public BoundingBox getBoundingBox() { return boundingBox; }
    @Override public CollisionLayer getCollisionLayer() { return CollisionLayer.ENEMY; }
    @Override public boolean isCollisionActive() { return active && aiState != ChaseAIState.DISABLED; }
    @Override public void onCollision(ICollidable other) { /* handled by PlayerEntity */ }

    public void reset(Vector3 spawnPosition) {
        position.set(spawnPosition);
        velocity.set(0, 0, 0);
        aiState = ChaseAIState.CHASING;
        active = true;
        bankAngle = 0;
        chaseSpeed = carDefinition != null ? carDefinition.getMaxSpeed() * 0.45f : 24.0f;
        update(0);
    }

    /** Dispose is intentionally lightweight – model data lives in ModelRegistry. */
    public void dispose() {
        modelInstance = null;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Vector3 getPosition()            { return position; }
    public Vector3 getVelocity()            { return velocity; }
    public ChaseAIState getAiState()        { return aiState; }
    public void setAiState(ChaseAIState s)  { this.aiState = s; }
    public float getChaseSpeed()            { return chaseSpeed; }
    public void setChaseSpeed(float speed)  { this.chaseSpeed = speed; }
    public float getBankAngle()             { return bankAngle; }
    public void setBankAngle(float angle)   { this.bankAngle = angle; }
    public boolean isActive()               { return active; }
    public void setActive(boolean active)   { this.active = active; }
    public CarDefinition getCarDefinition() { return carDefinition; }
}
