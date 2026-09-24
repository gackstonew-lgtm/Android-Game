package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * Dynamic civilian traffic vehicle cruising along lanes.
 * Implements ICollidable so player and police must weave through or risk collisions.
 */
public class TrafficVehicleEntity implements ICollidable, Poolable {

    private ModelInstance modelInstance;
    private final Vector3 position = new Vector3();
    private final Vector3 velocity = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();

    private float speed = 16.0f;
    private float width = 1.6f;
    private float height = 1.0f;
    private float length = 3.2f;
    private boolean active = false;

    public TrafficVehicleEntity() {
    }

    public void init(ModelInstance modelInstance, float x, float y, float z, float speed, boolean isTruck) {
        this.modelInstance = modelInstance;
        this.position.set(x, y, z);
        this.speed = speed;
        this.velocity.set(0, 0, speed);
        this.active = true;

        if (isTruck) {
            this.width = 2.0f;
            this.height = 1.8f;
            this.length = 4.2f;
        } else {
            this.width = 1.6f;
            this.height = 0.9f;
            this.length = 3.0f;
        }

        updateBoundingBox();
        updateTransform();
    }

    public void update(float delta) {
        if (!active) return;

        position.z += speed * delta;
        updateTransform();
        updateBoundingBox();
    }

    private void updateTransform() {
        if (modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.setToTranslation(position);
        }
    }

    private void updateBoundingBox() {
        boundingBox.set(
                new Vector3(position.x - width * 0.5f, position.y - height * 0.5f, position.z - length * 0.5f),
                new Vector3(position.x + width * 0.5f, position.y + height * 0.5f, position.z + length * 0.5f)
        );
    }

    public void render(ModelBatch batch, Environment environment) {
        if (active && modelInstance != null) {
            batch.render(modelInstance, environment);
        }
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
            // Spin off on collision
            this.active = false;
        }
    }

    @Override
    public void reset() {
        this.modelInstance = null;
        this.position.set(0, 0, 0);
        this.velocity.set(0, 0, 0);
        this.speed = 0;
        this.active = false;
    }

    public Vector3 getPosition() { return position; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
