package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * 3D Physical Obstacle entity (traffic cones, concrete barriers).
 * Utilizes preloaded/cached 3D models from ModelRegistry.
 */
public class ObstacleEntity implements ICollidable {

    public enum ObstacleType {
        TRAFFIC_CONE(0.6f, 0.8f, 0.6f, 15.0f),
        CONCRETE_BARRIER(1.5f, 1.0f, 1.8f, 35.0f);

        public final float width;
        public final float height;
        public final float length;
        public final float damage;

        ObstacleType(float width, float height, float length, float damage) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.damage = damage;
        }
    }

    private final Vector3 position = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();
    private ObstacleType type;
    private ModelInstance modelInstance;
    private boolean active = true;

    public ObstacleEntity(ModelInstance modelInstance, ObstacleType type, Vector3 spawnPos) {
        this.modelInstance = modelInstance;
        this.type = type != null ? type : ObstacleType.CONCRETE_BARRIER;
        this.position.set(spawnPos);

        if (this.modelInstance != null) {
            this.modelInstance.transform.setToTranslation(position);
        }
        updateBoundingBox();
    }

    public void updateBoundingBox() {
        float hw = type.width * 0.5f;
        float hh = type.height * 0.5f;
        float hl = type.length * 0.5f;
        boundingBox.set(
                new Vector3(position.x - hw, position.y - hh, position.z - hl),
                new Vector3(position.x + hw, position.y + hh, position.z + hl)
        );
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (active && modelInstance != null) {
            modelBatch.render(modelInstance, environment);
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
        if (other.getCollisionLayer() == CollisionLayer.PLAYER || other.getCollisionLayer() == CollisionLayer.ENEMY) {
            this.active = false;
        }
    }

    public void dispose() {
        // ModelInstance doesn't need explicit disposal, raw model is held by ModelRegistry
        modelInstance = null;
    }

    public Vector3 getPosition() {
        return position;
    }

    public ObstacleType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
