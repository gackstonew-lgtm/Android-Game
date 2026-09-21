package com.gackstone.chase.world;

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
 * 3D Physical Obstacle entity (e.g. barriers, road cones, concrete blocks).
 */
public class ObstacleEntity implements ICollidable {

    public static final float OBSTACLE_WIDTH = 1.6f;
    public static final float OBSTACLE_HEIGHT = 1.2f;
    public static final float OBSTACLE_LENGTH = 1.2f;

    private final Vector3 position = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();
    private boolean active = true;

    private Model model;
    private ModelInstance modelInstance;
    private boolean isDisposed = false;

    public ObstacleEntity(Vector3 spawnPos) {
        this.position.set(spawnPos);
        createProceduralModel();
        updateBoundingBox();
    }

    private void createProceduralModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material obsMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.95f, 0.65f, 0.1f, 1.0f)), // Warning amber
            ColorAttribute.createSpecular(Color.DARK_GRAY)
        );

        model = modelBuilder.createBox(
            OBSTACLE_WIDTH, OBSTACLE_HEIGHT, OBSTACLE_LENGTH,
            obsMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(position);
    }

    public void updateBoundingBox() {
        boundingBox.set(
            new Vector3(position.x - OBSTACLE_WIDTH * 0.5f, position.y - OBSTACLE_HEIGHT * 0.5f, position.z - OBSTACLE_LENGTH * 0.5f),
            new Vector3(position.x + OBSTACLE_WIDTH * 0.5f, position.y + OBSTACLE_HEIGHT * 0.5f, position.z + OBSTACLE_LENGTH * 0.5f)
        );
    }

    public void render(ModelBatch modelBatch) {
        if (active && modelInstance != null) {
            modelBatch.render(modelInstance);
        }
    }

    public void setPosition(Vector3 newPos) {
        this.position.set(newPos);
        if (modelInstance != null) {
            modelInstance.transform.setToTranslation(position);
        }
        updateBoundingBox();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.OBSTACLE;
    }

    @Override
    public boolean isCollisionActive() {
        return active;
    }

    @Override
    public void onCollision(ICollidable other) {
        // When player hits obstacle, deactivate or shatter obstacle
        if (other.getCollisionLayer() == CollisionLayer.PLAYER) {
            this.active = false;
        }
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
