package com.gackstone.chase.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * 3D Physical Chaser entity pursuing the Player.
 */
public class EnemyEntity implements ICollidable {

    public static final float BODY_WIDTH = 1.6f;
    public static final float BODY_HEIGHT = 0.9f;
    public static final float BODY_LENGTH = 3.0f;

    private final Vector3 position = new Vector3(0, 0.45f, -20.0f);
    private final Vector3 velocity = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();
    
    private ChaseAIState aiState = ChaseAIState.CHASING;
    private float chaseSpeed = 24.0f;
    private boolean active = true;

    private Model model;
    private ModelInstance modelInstance;
    private boolean isDisposed = false;

    public EnemyEntity() {
        createProceduralModel();
        updateBoundingBox();
    }

    private void createProceduralModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        // Stylized aggressive crimson pursuer body
        Material enemyMat = new Material(
            ColorAttribute.createDiffuse(new Color(1.0f, 0.15f, 0.2f, 1.0f)),
            ColorAttribute.createSpecular(Color.RED)
        );

        model = modelBuilder.createBox(
            BODY_WIDTH, BODY_HEIGHT, BODY_LENGTH,
            enemyMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(position);
    }

    public void update(float delta) {
        if (!active) return;

        modelInstance.transform.idt();
        modelInstance.transform.setToTranslation(position);
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        boundingBox.set(
            new Vector3(position.x - BODY_WIDTH * 0.5f, position.y - BODY_HEIGHT * 0.5f, position.z - BODY_LENGTH * 0.5f),
            new Vector3(position.x + BODY_WIDTH * 0.5f, position.y + BODY_HEIGHT * 0.5f, position.z + BODY_LENGTH * 0.5f)
        );
    }

    public void render(ModelBatch modelBatch) {
        if (active && modelInstance != null) {
            modelBatch.render(modelInstance);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.ENEMY;
    }

    @Override
    public boolean isCollisionActive() {
        return active && aiState != ChaseAIState.DISABLED;
    }

    @Override
    public void onCollision(ICollidable other) {
        // Handled in player and world collision resolutions
    }

    public void reset(Vector3 spawnPosition) {
        position.set(spawnPosition);
        velocity.set(0, 0, 0);
        aiState = ChaseAIState.CHASING;
        active = true;
        update(0);
    }

    public void dispose() {
        if (!isDisposed && model != null) {
            model.dispose();
            isDisposed = true;
        }
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public ChaseAIState getAiState() {
        return aiState;
    }

    public void setAiState(ChaseAIState aiState) {
        this.aiState = aiState;
    }

    public float getChaseSpeed() {
        return chaseSpeed;
    }

    public void setChaseSpeed(float chaseSpeed) {
        this.chaseSpeed = chaseSpeed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
