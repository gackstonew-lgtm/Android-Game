package com.gackstone.chase.player;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.cars.CarDefinition;
import com.gackstone.chase.cars.CarRegistry;
import com.gackstone.chase.core.GameEvents;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.ICollidable;

/**
 * 3D Physical Player vehicle representation in the Chase world.
 * Coordinates 3D transforms, car model swapping, cockpit props, and collisions.
 */
public class PlayerEntity implements ICollidable, Disposable {

    public static final float BODY_WIDTH = 1.5f;
    public static final float BODY_HEIGHT = 0.85f;
    public static final float BODY_LENGTH = 3.2f;

    private final ModelRegistry modelRegistry;
    private final Vector3 position = new Vector3(0, 0.4f, 0);
    private final Vector3 velocity = new Vector3();
    private final BoundingBox boundingBox = new BoundingBox();
    private final PlayerState state = new PlayerState();

    private CarDefinition currentCar;
    private ModelInstance modelInstance;

    // Cockpit 3D models for first-person camera mode
    private ModelInstance dashboardInstance;
    private ModelInstance steeringWheelInstance;

    private boolean isDisposed = false;

    public PlayerEntity(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;

        // Load dashboard & steering wheel
        if (modelRegistry != null) {
            dashboardInstance = modelRegistry.createInstance(ModelRegistry.KEY_COCKPIT_DASH);
            steeringWheelInstance = modelRegistry.createInstance(ModelRegistry.KEY_COCKPIT_STEERING);
        }

        // Apply default player car (Phantom GT)
        setCarDefinition(CarRegistry.getPlayerCars().first());
        updateBoundingBox();
    }

    /**
     * Swaps the active car definition and 3D model instance at runtime.
     */
    public void setCarDefinition(CarDefinition carDef) {
        this.currentCar = carDef != null ? carDef : CarRegistry.getPlayerCars().first();
        this.state.applyCarDefinition(this.currentCar);

        if (modelRegistry != null) {
            this.modelInstance = modelRegistry.createInstance(this.currentCar.getModelKey());
        }
        updateTransform();
    }

    public void update(float delta) {
        state.update(delta);
        updateTransform();
        updateBoundingBox();
    }

    private void updateTransform() {
        if (modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.setToTranslation(position);
            modelInstance.transform.rotate(Vector3.Z, state.getCurrentBankAngle());
        }

        // Update cockpit transforms relative to player position
        if (dashboardInstance != null) {
            dashboardInstance.transform.idt();
            dashboardInstance.transform.setToTranslation(position.x, position.y + 0.35f, position.z + 0.2f);
            dashboardInstance.transform.rotate(Vector3.Z, state.getCurrentBankAngle());
        }

        if (steeringWheelInstance != null) {
            steeringWheelInstance.transform.idt();
            steeringWheelInstance.transform.setToTranslation(position.x - 0.3f, position.y + 0.38f, position.z + 0.45f);
            // Rotate steering wheel in sync with steer angle
            steeringWheelInstance.transform.rotate(Vector3.Z, state.getCurrentSteerAngle() * 180.0f);
        }
    }

    private void updateBoundingBox() {
        boundingBox.set(
                new Vector3(position.x - BODY_WIDTH * 0.5f, position.y - BODY_HEIGHT * 0.5f, position.z - BODY_LENGTH * 0.5f),
                new Vector3(position.x + BODY_WIDTH * 0.5f, position.y + BODY_HEIGHT * 0.5f, position.z + BODY_LENGTH * 0.5f)
        );
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (modelInstance != null) {
            // Visual damage flash if invulnerable
            if (state.isInvulnerable()) {
                if (MathUtils.randomBoolean(0.3f)) {
                    return; // Skip frame for blink effect
                }
            }
            modelBatch.render(modelInstance, environment);
        }
    }

    public void renderCockpit(ModelBatch modelBatch, Environment environment) {
        if (dashboardInstance != null) {
            modelBatch.render(dashboardInstance, environment);
        }
        if (steeringWheelInstance != null) {
            modelBatch.render(steeringWheelInstance, environment);
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
            float baseDmg = 25.0f;
            // Armor damage reduction based on car mass/health
            float armorFactor = 100.0f / (currentCar != null ? currentCar.getMaxHealth() : 100.0f);
            float dmg = baseDmg * armorFactor;

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

    @Override
    public void dispose() {
        isDisposed = true;
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

    public CarDefinition getCurrentCar() {
        return currentCar;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
