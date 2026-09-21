package com.gackstone.chase.player;

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
import com.gackstone.chase.core.GameEvents;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * 3D Physical Player representation in the Chase world.
 * Manages 3D transform, procedural 3D model instance, and collision bounding volume.
 */
public class PlayerEntity implements ICollidable {

    public static final float BODY_WIDTH = 1.4f;
    public static final float BODY_HEIGHT = 0.8f;
    public static final float BODY_LENGTH = 2.8f;

    private final Vector3 position = new Vector3(0, 0.4f, 0);
    private final Vector3 velocity = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();
    private final PlayerState state = new PlayerState();

    private Model model;
    private ModelInstance modelInstance;
    private boolean isDisposed = false;

    public PlayerEntity() {
        createProceduralModel();
        updateBoundingBox();
    }

    private void createProceduralModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        // Create stylized sleek neon/cyan pursuit vehicle
        Material playerMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.1f, 0.8f, 1.0f, 1.0f)),
            ColorAttribute.createSpecular(Color.WHITE)
        );
        
        model = modelBuilder.createBox(
            BODY_WIDTH, BODY_HEIGHT, BODY_LENGTH,
            playerMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(position);
    }

    public void update(float delta) {
        state.update(delta);
        
        // Update model transform matrix
        modelInstance.transform.idt();
        modelInstance.transform.setToTranslation(position);
        modelInstance.transform.rotate(Vector3.Z, state.getCurrentBankAngle());

        updateBoundingBox();
    }

    private void updateBoundingBox() {
        boundingBox.set(
            new Vector3(position.x - BODY_WIDTH * 0.5f, position.y - BODY_HEIGHT * 0.5f, position.z - BODY_LENGTH * 0.5f),
            new Vector3(position.x + BODY_WIDTH * 0.5f, position.y + BODY_HEIGHT * 0.5f, position.z + BODY_LENGTH * 0.5f)
        );
    }

    public void render(ModelBatch modelBatch) {
        if (modelInstance != null) {
            modelBatch.render(modelInstance);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.PLAYER;
    }

    @Override
    public boolean isCollisionActive() {
        return state.isAlive();
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other.getCollisionLayer() == CollisionLayer.OBSTACLE) {
            float dmg = 25.0f;
            state.takeDamage(dmg);
            GameEvents.fireObstacleHit(dmg);
            GameEvents.firePlayerDamaged(state.getHealth(), dmg);
        } else if (other.getCollisionLayer() == CollisionLayer.ENEMY) {
            state.takeDamage(100.0f);
            GameEvents.firePlayerCaught(state.getDistanceTraveled(), (long) state.getDistanceTraveled());
        }
    }

    public void reset() {
        position.set(0, 0.4f, 0);
        velocity.set(0, 0, 0);
        state.reset();
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

    public PlayerState getState() {
        return state;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
