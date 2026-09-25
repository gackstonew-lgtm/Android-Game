package com.gackstone.chase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.cars.CarDefinition;

/**
 * High-performance 3D studio preview renderer for the Garage screen.
 *
 * <p>Features:
 * <ul>
 *   <li>Dedicated 3D studio lighting environment (key light, fill light, rim light, floor spotlight).
 *   <li>Stylized showroom pedestal with circular glow rings and contact shadow base.
 *   <li>Smooth auto-rotation with interactive touch/mouse drag yaw control and damping inertia.
 *   <li>Zero GC allocation during per-frame rendering.
 *   <li>Full support for swapping {@link CarDefinition} models dynamically.
 * </ul>
 */
public class CarPreviewRenderer implements Disposable {

    private final ModelRegistry modelRegistry;
    private final ModelBatch    modelBatch;
    private final PerspectiveCamera camera;
    private final Environment   environment;

    // Showroom pedestal & ground models
    private final Model         groundModel;
    private final ModelInstance groundInstance;

    // Active car instance
    private CarDefinition currentCarDef;
    private ModelInstance carInstance;

    // Rotation & interaction state
    private float autoRotateSpeed = 16.0f; // degrees per second
    private float yawAngle        = -35.0f; // initial nice 3/4 front quarter angle
    private float targetYawAngle  = -35.0f;
    private float dragVelocity    = 0.0f;
    private boolean isDragging    = false;
    private float lastTouchX      = 0.0f;

    // Positioning offset for showroom centering
    private final Vector3 carCenter = new Vector3(0.15f, 0.0f, -0.1f);
    private final Matrix4 transformTemp = new Matrix4();

    public CarPreviewRenderer(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        this.modelBatch    = new ModelBatch();

        // 1. Studio Camera
        this.camera = new PerspectiveCamera(42.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.position.set(2.4f, 1.45f, 3.4f);
        this.camera.lookAt(carCenter.x, carCenter.y + 0.35f, carCenter.z);
        this.camera.near = 0.2f;
        this.camera.far  = 100.0f;
        this.camera.update();

        // 2. Studio Lighting Environment
        this.environment = new Environment();
        // Ambient studio slate
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.38f, 0.42f, 0.52f, 1.0f));

        // Primary key light (front-left high angle)
        DirectionalLight keyLight = new DirectionalLight();
        keyLight.set(0.95f, 0.95f, 1.0f, -0.6f, -0.8f, -0.6f);
        this.environment.add(keyLight);

        // Fill light (right-front soft blue)
        DirectionalLight fillLight = new DirectionalLight();
        fillLight.set(0.35f, 0.45f, 0.65f, 0.7f, -0.5f, -0.4f);
        this.environment.add(fillLight);

        // Rim/Back light (rear accent neon)
        DirectionalLight rimLight = new DirectionalLight();
        rimLight.set(0.3f, 0.65f, 0.9f, 0.4f, -0.3f, 0.8f);
        this.environment.add(rimLight);

        // Ground under-car accent light
        PointLight groundSpot = new PointLight();
        groundSpot.set(new Color(0.0f, 0.8f, 1.0f, 1.0f), 0, 0.2f, 0, 8.0f);
        this.environment.add(groundSpot);

        // 3. Build Showroom Floor Pedestal
        this.groundModel = createShowroomFloor();
        this.groundInstance = new ModelInstance(groundModel);
        this.groundInstance.transform.setToTranslation(carCenter.x, -0.02f, carCenter.z);
    }

    /**
     * Builds a circular showroom turntable and soft ground shadow disc.
     */
    private Model createShowroomFloor() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // Shadow disc beneath car
        Material shadowMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.03f, 0.04f, 0.07f, 0.95f)),
                ColorAttribute.createSpecular(new Color(0.1f, 0.12f, 0.18f, 1.0f))
        );
        MeshPartBuilder shadowBuilder = mb.part("shadowDisc", GL20.GL_TRIANGLES, attr, shadowMat);
        CylinderShapeBuilder.build(shadowBuilder, 4.4f, 0.04f, 4.4f, 32);

        // Outer neon glow ring
        Material ringMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.06f, 0.10f, 0.18f, 0.9f)),
                ColorAttribute.createEmissive(new Color(0.0f, 0.75f, 1.0f, 0.8f))
        );
        MeshPartBuilder ringBuilder = mb.part("glowRing", GL20.GL_TRIANGLES, attr, ringMat);
        CylinderShapeBuilder.build(ringBuilder, 4.6f, 0.015f, 4.6f, 32);

        // Grid accents around the turntable
        Material gridMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.12f, 0.18f, 0.28f, 0.7f)),
                ColorAttribute.createSpecular(Color.CYAN)
        );
        MeshPartBuilder gridBuilder = mb.part("gridLines", GL20.GL_TRIANGLES, attr, gridMat);
        BoxShapeBuilder.build(gridBuilder, 0, 0.01f, 0, 4.2f, 0.01f, 0.04f);
        BoxShapeBuilder.build(gridBuilder, 0, 0.01f, 0, 0.04f, 0.01f, 4.2f);

        return mb.end();
    }

    /**
     * Sets the active car to render.
     */
    public void setCar(CarDefinition carDef) {
        if (carDef == null) return;
        this.currentCarDef = carDef;

        if (modelRegistry != null) {
            this.carInstance = modelRegistry.createInstance(carDef.getModelKey());
        }
        updateCarTransform();
    }

    public CarDefinition getCurrentCar() {
        return currentCarDef;
    }

    /**
     * Updates rotation and camera logic.
     */
    public void update(float delta) {
        if (!isDragging) {
            // Apply damping to drag velocity
            dragVelocity = MathUtils.lerp(dragVelocity, 0.0f, delta * 4.0f);
            targetYawAngle += (autoRotateSpeed + dragVelocity) * delta;
        }

        yawAngle = MathUtils.lerp(yawAngle, targetYawAngle, Math.min(1.0f, delta * 12.0f));
        updateCarTransform();
    }

    private void updateCarTransform() {
        if (carInstance != null) {
            transformTemp.idt();
            transformTemp.setToTranslation(carCenter);
            transformTemp.rotate(Vector3.Y, yawAngle);
            carInstance.transform.set(transformTemp);
        }
    }

    /**
     * Renders the 3D car and studio showroom background.
     */
    public void render() {
        // Enable depth test for 3D geometry
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

        camera.update();

        modelBatch.begin(camera);

        // 1. Showroom floor pedestal
        if (groundInstance != null) {
            modelBatch.render(groundInstance, environment);
        }

        // 2. Active 3D Car
        if (carInstance != null) {
            modelBatch.render(carInstance, environment);
        }

        modelBatch.end();

        // Restore OpenGL state for Scene2D UI
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    /**
     * Resizes the perspective camera viewport.
     */
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        // Position camera to frame car slightly right of center to accommodate left UI rail
        camera.position.set(2.2f, 1.40f, 3.3f);
        camera.lookAt(carCenter.x, carCenter.y + 0.35f, carCenter.z);
        camera.update();
    }

    /**
     * Touch / Drag interaction handling for user 360 degree rotation.
     */
    public void handleTouchDown(float screenX) {
        isDragging = true;
        lastTouchX = screenX;
        dragVelocity = 0.0f;
    }

    public void handleTouchDragged(float screenX) {
        if (isDragging) {
            float deltaX = screenX - lastTouchX;
            float angleDelta = deltaX * 0.45f;
            targetYawAngle += angleDelta;
            yawAngle += angleDelta;
            dragVelocity = angleDelta * 25.0f;
            lastTouchX = screenX;
            updateCarTransform();
        }
    }

    public void handleTouchUp() {
        isDragging = false;
    }

    public void triggerSpin360() {
        targetYawAngle += 360.0f;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (groundModel != null) {
            groundModel.dispose();
        }
    }
}
