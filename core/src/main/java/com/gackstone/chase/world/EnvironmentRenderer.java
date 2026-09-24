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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.gackstone.chase.assets.ModelRegistry;
import com.gackstone.chase.core.GameConfig;

/**
 * High-performance 3D environment renderer managing dynamic theme lighting,
 * atmospheric fog, recycled endless road segments, guard rails, terrain ground plane,
 * and roadside decorative props (skyscrapers, streetlights, desert mesas, cacti, cargo containers).
 */
public class EnvironmentRenderer implements Disposable {

    private final ModelRegistry modelRegistry;
    private final Environment environment;
    private final DirectionalLight sunLight;

    private EnvironmentTheme currentTheme;
    private Model groundModel;
    private ModelInstance groundInstance;

    private final Array<ModelInstance> roadInstances = new Array<>(false, 16);
    private final Array<ModelInstance> guardRailLeftInstances = new Array<>(false, 16);
    private final Array<ModelInstance> guardRailRightInstances = new Array<>(false, 16);

    // Roadside Props Management
    private final Array<RoadsidePropEntity> activeProps = new Array<>(false, 64);
    private final Pool<RoadsidePropEntity> propPool = new Pool<RoadsidePropEntity>(32, 128) {
        @Override
        protected RoadsidePropEntity newObject() {
            return new RoadsidePropEntity();
        }
    };

    private float furthestRoadZ = 0.0f;
    private float nextPropSpawnZ = 0.0f;
    private static final float PROP_SPAWN_INTERVAL = 25.0f;

    private boolean isDisposed = false;

    public EnvironmentRenderer(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        this.environment = new Environment();

        sunLight = new DirectionalLight();
        environment.add(sunLight);

        // Default to Neon Metropolis theme
        setTheme(EnvironmentRegistry.getById(EnvironmentRegistry.THEME_NEON_CITY));
    }

    /**
     * Applies a new environment theme and rebuilds active track segments and props.
     */
    public void setTheme(EnvironmentTheme theme) {
        this.currentTheme = theme;

        // 1. Update Lighting & Fog
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, theme.getAmbientLight()));
        environment.set(new ColorAttribute(ColorAttribute.Fog, theme.getFogColor()));
        sunLight.set(theme.getDirectionalLightColor(), theme.getLightDirection());

        // 2. Recreate ground plane with theme terrain color
        if (groundModel != null) {
            groundModel.dispose();
        }
        ModelBuilder modelBuilder = new ModelBuilder();
        Material groundMat = new Material(
                ColorAttribute.createDiffuse(theme.getTerrainColor()),
                ColorAttribute.createSpecular(new Color(0.02f, 0.02f, 0.02f, 1.0f))
        );
        groundModel = modelBuilder.createBox(
                500.0f, 0.2f, 800.0f,
                groundMat,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        groundInstance = new ModelInstance(groundModel);
        groundInstance.transform.setToTranslation(0, -0.2f, 150.0f);

        // 3. Reset and rebuild segments
        initializeSegments();
    }

    private void initializeSegments() {
        // Clear road segments
        roadInstances.clear();
        guardRailLeftInstances.clear();
        guardRailRightInstances.clear();

        // Clear active props back to pool
        for (int i = 0; i < activeProps.size; i++) {
            propPool.free(activeProps.get(i));
        }
        activeProps.clear();

        furthestRoadZ = -GameConfig.ROAD_SEGMENT_LENGTH;
        for (int i = 0; i < GameConfig.ROAD_SEGMENTS_COUNT; i++) {
            float segmentZ = furthestRoadZ + (i * GameConfig.ROAD_SEGMENT_LENGTH);
            addSegmentAt(segmentZ);
        }
        furthestRoadZ += (GameConfig.ROAD_SEGMENTS_COUNT * GameConfig.ROAD_SEGMENT_LENGTH);

        // Spawn initial roadside props ahead
        nextPropSpawnZ = -20.0f;
        while (nextPropSpawnZ < furthestRoadZ) {
            spawnRoadsidePropsAt(nextPropSpawnZ);
            nextPropSpawnZ += PROP_SPAWN_INTERVAL;
        }
    }

    private void addSegmentAt(float z) {
        // 1. Road instance from registry
        Model roadModel = modelRegistry.getModel(currentTheme.getRoadModelKey());
        if (roadModel != null) {
            ModelInstance roadInst = new ModelInstance(roadModel);
            roadInst.transform.setToTranslation(0, 0, z);
            roadInstances.add(roadInst);
        }

        // 2. Left and Right Guard Rails
        Model railModel = modelRegistry.getModel(currentTheme.getRailModelKey());
        if (railModel != null) {
            float railX = GameConfig.ROAD_WIDTH * 0.5f;

            ModelInstance leftRail = new ModelInstance(railModel);
            leftRail.transform.setToTranslation(-railX, 0.35f, z);
            guardRailLeftInstances.add(leftRail);

            ModelInstance rightRail = new ModelInstance(railModel);
            rightRail.transform.setToTranslation(railX, 0.35f, z);
            guardRailRightInstances.add(rightRail);
        }
    }

    private void spawnRoadsidePropsAt(float z) {
        Array<String> propKeys = currentTheme.getPropKeys();
        if (propKeys.size == 0) return;

        // Left roadside prop
        String leftKey = propKeys.get(MathUtils.random(0, propKeys.size - 1));
        Model leftModel = modelRegistry.getModel(leftKey);
        if (leftModel != null) {
            RoadsidePropEntity prop = propPool.obtain();
            float leftX = -GameConfig.ROAD_WIDTH * 0.5f - MathUtils.random(4.0f, 14.0f);
            float rotY = MathUtils.random(0, 3) * 90.0f;
            float scale = MathUtils.random(0.9f, 1.2f);
            prop.init(new ModelInstance(leftModel), leftX, 0, z, rotY, scale);
            activeProps.add(prop);
        }

        // Right roadside prop
        String rightKey = propKeys.get(MathUtils.random(0, propKeys.size - 1));
        Model rightModel = modelRegistry.getModel(rightKey);
        if (rightModel != null) {
            RoadsidePropEntity prop = propPool.obtain();
            float rightX = GameConfig.ROAD_WIDTH * 0.5f + MathUtils.random(4.0f, 14.0f);
            float rotY = MathUtils.random(0, 3) * 90.0f;
            float scale = MathUtils.random(0.9f, 1.2f);
            prop.init(new ModelInstance(rightModel), rightX, 0, z, rotY, scale);
            activeProps.add(prop);
        }
    }

    public void update(float playerZ) {
        // Ground plane follows player forward position
        if (groundInstance != null) {
            groundInstance.transform.setToTranslation(0, -0.2f, playerZ + 150.0f);
        }

        // 1. Recycle road segments
        if (roadInstances.size > 0) {
            Vector3 pos = new Vector3();
            roadInstances.get(0).transform.getTranslation(pos);

            if (pos.z < playerZ - GameConfig.ROAD_SEGMENT_LENGTH * 1.5f) {
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

        // 2. Recycle roadside props behind player
        for (int i = activeProps.size - 1; i >= 0; i--) {
            RoadsidePropEntity prop = activeProps.get(i);
            if (prop.getPosition().z < playerZ - 30.0f) {
                activeProps.removeIndex(i);
                propPool.free(prop);
            }
        }

        // 3. Spawn roadside props ahead
        while (nextPropSpawnZ < playerZ + 220.0f) {
            spawnRoadsidePropsAt(nextPropSpawnZ);
            nextPropSpawnZ += PROP_SPAWN_INTERVAL + MathUtils.random(-3.0f, 6.0f);
        }
    }

    public void render(ModelBatch modelBatch) {
        // 1. Render ground
        if (groundInstance != null) {
            modelBatch.render(groundInstance, environment);
        }

        // 2. Render road and guard rails
        for (int i = 0; i < roadInstances.size; i++) {
            modelBatch.render(roadInstances.get(i), environment);
            modelBatch.render(guardRailLeftInstances.get(i), environment);
            modelBatch.render(guardRailRightInstances.get(i), environment);
        }

        // 3. Render roadside props
        for (int i = 0; i < activeProps.size; i++) {
            activeProps.get(i).render(modelBatch, environment);
        }
    }

    public void reset() {
        initializeSegments();
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            if (groundModel != null) {
                groundModel.dispose();
            }
            for (int i = 0; i < activeProps.size; i++) {
                propPool.free(activeProps.get(i));
            }
            activeProps.clear();
            isDisposed = true;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public EnvironmentTheme getCurrentTheme() {
        return currentTheme;
    }
}
