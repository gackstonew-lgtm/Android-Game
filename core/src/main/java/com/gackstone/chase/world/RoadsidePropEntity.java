package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Reusable roadside environment prop (skyscraper, streetlight, mesa, container, etc.).
 */
public class RoadsidePropEntity implements Poolable {

    private ModelInstance modelInstance;
    private final Vector3 position = new Vector3();
    private float rotationY = 0.0f;
    private float scale = 1.0f;
    private boolean active = false;

    public RoadsidePropEntity() {
    }

    public void init(ModelInstance modelInstance, float x, float y, float z, float rotationY, float scale) {
        this.modelInstance = modelInstance;
        this.position.set(x, y, z);
        this.rotationY = rotationY;
        this.scale = scale;
        this.active = true;

        updateTransform();
    }

    public void updateTransform() {
        if (modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.setToTranslation(position);
            if (rotationY != 0) {
                modelInstance.transform.rotate(Vector3.Y, rotationY);
            }
            if (scale != 1.0f) {
                modelInstance.transform.scl(scale);
            }
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        if (active && modelInstance != null) {
            batch.render(modelInstance, environment);
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

    @Override
    public void reset() {
        this.modelInstance = null;
        this.position.set(0, 0, 0);
        this.rotationY = 0;
        this.scale = 1.0f;
        this.active = false;
    }
}
