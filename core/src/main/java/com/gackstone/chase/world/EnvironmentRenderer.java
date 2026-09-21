package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gackstone.chase.core.GameConfig;

/**
 * Manages 3D lighting, ground terrain, recycled endless road segments,
 * guard rails, and environmental atmospheric fog.
 */
public class EnvironmentRenderer {

    private final Environment environment;
    private final DirectionalLight sunLight;
    
    private Model roadSegmentModel;
    private Model guardRailModel;
    private Model groundModel;

    private ModelInstance groundInstance;
    private final Array<ModelInstance> roadInstances = new Array<>(false, 16);
    private final Array<ModelInstance> guardRailLeftInstances = new Array<>(false, 16);
    private final Array<ModelInstance> guardRailRightInstances = new Array<>(false, 16);

    private float furthestRoadZ = 0.0f;
    private boolean isDisposed = false;

    public EnvironmentRenderer() {
        environment = new Environment();
        // Ambient skylight
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.45f, 0.55f, 1.0f));
        // Directional warm sunlight
        sunLight = new DirectionalLight().set(0.9f, 0.88f, 0.8f, -0.4f, -0.8f, 0.5f);
        environment.add(sunLight);

        createModels();
        initializeSegments();
    }

    private void createModels() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // 1. Road asphalt segment
        Material roadMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.18f, 0.2f, 0.24f, 1.0f)),
            ColorAttribute.createSpecular(new Color(0.1f, 0.1f, 0.1f, 1.0f))
        );
        roadSegmentModel = modelBuilder.createBox(
            GameConfig.ROAD_WIDTH, 0.2f, GameConfig.ROAD_SEGMENT_LENGTH,
            roadMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        // 2. Neon / Metallic Guard Rail
        Material railMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.85f, 0.25f, 0.15f, 1.0f)),
            ColorAttribute.createSpecular(Color.WHITE)
        );
        guardRailModel = modelBuilder.createBox(
            0.4f, 0.8f, GameConfig.ROAD_SEGMENT_LENGTH,
            railMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        // 3. Surrounding vast terrain
        Material groundMat = new Material(
            ColorAttribute.createDiffuse(new Color(0.08f, 0.1f, 0.14f, 1.0f))
        );
        groundModel = modelBuilder.createBox(
            400.0f, 0.1f, 600.0f,
            groundMat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        groundInstance = new ModelInstance(groundModel);
        groundInstance.transform.setToTranslation(0, -0.15f, 150.0f);
    }

    private void initializeSegments() {
        roadInstances.clear();
        guardRailLeftInstances.clear();
        guardRailRightInstances.clear();

        furthestRoadZ = -GameConfig.ROAD_SEGMENT_LENGTH;
        for (int i = 0; i < GameConfig.ROAD_SEGMENTS_COUNT; i++) {
            float segmentZ = furthestRoadZ + (i * GameConfig.ROAD_SEGMENT_LENGTH);
            addSegmentAt(segmentZ);
        }
        furthestRoadZ += (GameConfig.ROAD_SEGMENTS_COUNT * GameConfig.ROAD_SEGMENT_LENGTH);
    }

    private void addSegmentAt(float z) {
        // Road instance
        ModelInstance roadInst = new ModelInstance(roadSegmentModel);
        roadInst.transform.setToTranslation(0, 0, z);
        roadInstances.add(roadInst);

        // Left Guard Rail
        float railX = GameConfig.ROAD_WIDTH * 0.5f;
        ModelInstance leftRail = new ModelInstance(guardRailModel);
        leftRail.transform.setToTranslation(-railX, 0.35f, z);
        guardRailLeftInstances.add(leftRail);

        // Right Guard Rail
        ModelInstance rightRail = new ModelInstance(guardRailModel);
        rightRail.transform.setToTranslation(railX, 0.35f, z);
        guardRailRightInstances.add(rightRail);
    }

    public void update(float playerZ) {
        // Update terrain center to follow player
        groundInstance.transform.setToTranslation(0, -0.15f, playerZ + 150.0f);

        // Recycle road segments that fall behind
        if (roadInstances.size > 0) {
            Vector3 pos = new Vector3();
            roadInstances.get(0).transform.getTranslation(pos);

            if (pos.z < playerZ - GameConfig.ROAD_SEGMENT_LENGTH * 1.5f) {
                // Recycle first segment to furthest Z
                ModelInstance recycledRoad = roadInstances.removeIndex(0);
                ModelInstance recycledLeftRail = guardRailLeftInstances.removeIndex(0);
                ModelInstance recycledRightRail = guardRailRightInstances.removeIndex(0);

                recycledRoad.transform.setToTranslation(0, 0, furthestRoadZ);
                float railX = GameConfig.ROAD_WIDTH * 0.5f;
                recycledLeftRail.transform.setToTranslation(-railX, 0.35f, furthestRoadZ);
                recycledRightRail.transform.setToTranslation(railX, 0.35f, furthestRoadZ);

                roadInstances.add(recycledRoad);
                guardRailLeftInstances.add(recycledLeftRail);
                guardRailRightInstances.add(recycledRightRail);

                furthestRoadZ += GameConfig.ROAD_SEGMENT_LENGTH;
            }
        }
    }

    public void render(ModelBatch modelBatch) {
        if (groundInstance != null) {
            modelBatch.render(groundInstance, environment);
        }

        for (int i = 0; i < roadInstances.size; i++) {
            modelBatch.render(roadInstances.get(i), environment);
            modelBatch.render(guardRailLeftInstances.get(i), environment);
            modelBatch.render(guardRailRightInstances.get(i), environment);
        }
    }

    public void reset() {
        initializeSegments();
    }

    public void dispose() {
        if (!isDisposed) {
            if (roadSegmentModel != null) roadSegmentModel.dispose();
            if (guardRailModel != null) guardRailModel.dispose();
            if (groundModel != null) groundModel.dispose();
            isDisposed = true;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }
}
