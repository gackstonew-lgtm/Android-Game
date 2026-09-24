package com.gackstone.chase.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.gackstone.chase.core.GameConfig;

/**
 * Central registry for all 3D models in Chase.
 *
 * Combines libGDX AssetManager asynchronous pipeline with ProceduralModelFactory
 * fallbacks and baking to ensure zero stutter during runtime gameplay.
 */
public class ModelRegistry implements Disposable {

    private final AssetManager assetManager;
    private final ProceduralModelFactory proceduralFactory;
    private final ObjectMap<String, Model> cachedModels = new ObjectMap<>();

    // Model Keys
    public static final String KEY_PLAYER_PHANTOM_GT = "player_phantom_gt";
    public static final String KEY_PLAYER_APEX_MUSCLE = "player_apex_muscle";
    public static final String KEY_PLAYER_VANGUARD = "player_vanguard_armored";

    public static final String KEY_ENEMY_PATROL = "enemy_patrol_cruiser";
    public static final String KEY_ENEMY_TACTICAL_SUV = "enemy_tactical_suv";

    public static final String KEY_TRAFFIC_SEDAN = "traffic_sedan";
    public static final String KEY_TRAFFIC_TRUCK = "traffic_truck";

    public static final String KEY_PROP_ROAD_NEON = "road_neon";
    public static final String KEY_PROP_ROAD_DESERT = "road_desert";
    public static final String KEY_PROP_ROAD_INDUSTRIAL = "road_industrial";

    public static final String KEY_PROP_RAIL_NEON = "rail_neon";
    public static final String KEY_PROP_RAIL_DESERT = "rail_desert";
    public static final String KEY_PROP_RAIL_INDUSTRIAL = "rail_industrial";

    public static final String KEY_PROP_STREETLIGHT = "prop_streetlight";
    public static final String KEY_PROP_SKYSCRAPER_A = "prop_skyscraper_a";
    public static final String KEY_PROP_SKYSCRAPER_B = "prop_skyscraper_b";
    public static final String KEY_PROP_DESERT_MESA = "prop_desert_mesa";
    public static final String KEY_PROP_DESERT_CACTUS = "prop_desert_cactus";
    public static final String KEY_PROP_CONTAINER_ORANGE = "prop_container_orange";
    public static final String KEY_PROP_CONTAINER_BLUE = "prop_container_blue";

    public static final String KEY_OBS_CONE = "obs_traffic_cone";
    public static final String KEY_OBS_BARRIER = "obs_concrete_barrier";

    public static final String KEY_COCKPIT_DASH = "cockpit_dash";
    public static final String KEY_COCKPIT_STEERING = "cockpit_steering";

    private boolean isLoaded = false;

    public ModelRegistry(AssetManager assetManager) {
        this.assetManager = assetManager != null ? assetManager : new AssetManager();
        this.proceduralFactory = new ProceduralModelFactory();
    }

    /**
     * Initializes and bakes all models for high performance.
     */
    public void loadAllModels() {
        if (isLoaded) return;

        // 1. Player Cars
        cachedModels.put(KEY_PLAYER_PHANTOM_GT, proceduralFactory.createPhantomGTSportsCar(
                new Color(0.0f, 0.85f, 1.0f, 1.0f),
                new Color(0.12f, 0.14f, 0.20f, 1.0f),
                new Color(0.0f, 1.0f, 1.0f, 1.0f)
        ));

        cachedModels.put(KEY_PLAYER_APEX_MUSCLE, proceduralFactory.createApexMuscleCar(
                new Color(1.0f, 0.45f, 0.0f, 1.0f),
                new Color(0.15f, 0.15f, 0.18f, 1.0f),
                new Color(1.0f, 0.7f, 0.0f, 1.0f)
        ));

        cachedModels.put(KEY_PLAYER_VANGUARD, proceduralFactory.createVanguardArmoredCruiser(
                new Color(0.85f, 0.2f, 0.25f, 1.0f),
                new Color(0.18f, 0.2f, 0.24f, 1.0f),
                new Color(1.0f, 0.1f, 0.1f, 1.0f)
        ));

        // 2. Enemy Chaser Cars
        cachedModels.put(KEY_ENEMY_PATROL, proceduralFactory.createPatrolCruiserEnemy());
        cachedModels.put(KEY_ENEMY_TACTICAL_SUV, proceduralFactory.createTacticalSUVEnemy());

        // 3. Traffic vehicles
        cachedModels.put(KEY_TRAFFIC_SEDAN, proceduralFactory.createTrafficSedan(new Color(0.7f, 0.75f, 0.8f, 1.0f)));
        cachedModels.put(KEY_TRAFFIC_TRUCK, proceduralFactory.createTrafficTruck(new Color(0.9f, 0.3f, 0.2f, 1.0f)));

        // 4. Roads and Rails for Themes
        cachedModels.put(KEY_PROP_ROAD_NEON, proceduralFactory.createDetailedRoadSegment(
                GameConfig.ROAD_WIDTH, GameConfig.ROAD_SEGMENT_LENGTH,
                new Color(0.14f, 0.16f, 0.22f, 1.0f),
                new Color(0.0f, 0.9f, 1.0f, 1.0f)
        ));
        cachedModels.put(KEY_PROP_ROAD_DESERT, proceduralFactory.createDetailedRoadSegment(
                GameConfig.ROAD_WIDTH, GameConfig.ROAD_SEGMENT_LENGTH,
                new Color(0.32f, 0.28f, 0.24f, 1.0f),
                new Color(1.0f, 0.8f, 0.2f, 1.0f)
        ));
        cachedModels.put(KEY_PROP_ROAD_INDUSTRIAL, proceduralFactory.createDetailedRoadSegment(
                GameConfig.ROAD_WIDTH, GameConfig.ROAD_SEGMENT_LENGTH,
                new Color(0.2f, 0.24f, 0.24f, 1.0f),
                new Color(0.9f, 0.9f, 0.9f, 1.0f)
        ));

        cachedModels.put(KEY_PROP_RAIL_NEON, proceduralFactory.createDetailedGuardRail(
                GameConfig.ROAD_SEGMENT_LENGTH, new Color(1.0f, 0.1f, 0.35f, 1.0f)
        ));
        cachedModels.put(KEY_PROP_RAIL_DESERT, proceduralFactory.createDetailedGuardRail(
                GameConfig.ROAD_SEGMENT_LENGTH, new Color(0.85f, 0.45f, 0.2f, 1.0f)
        ));
        cachedModels.put(KEY_PROP_RAIL_INDUSTRIAL, proceduralFactory.createDetailedGuardRail(
                GameConfig.ROAD_SEGMENT_LENGTH, new Color(0.95f, 0.5f, 0.1f, 1.0f)
        ));

        // 5. Environment props
        cachedModels.put(KEY_PROP_STREETLIGHT, proceduralFactory.createStreetLight(new Color(0.0f, 0.95f, 1.0f, 1.0f)));
        cachedModels.put(KEY_PROP_SKYSCRAPER_A, proceduralFactory.createCitySkyscraper(18.0f, 65.0f, 18.0f, new Color(0.12f, 0.15f, 0.20f, 1.0f), new Color(0.0f, 0.8f, 1.0f, 1.0f)));
        cachedModels.put(KEY_PROP_SKYSCRAPER_B, proceduralFactory.createCitySkyscraper(22.0f, 90.0f, 22.0f, new Color(0.08f, 0.10f, 0.15f, 1.0f), new Color(1.0f, 0.2f, 0.6f, 1.0f)));
        cachedModels.put(KEY_PROP_DESERT_MESA, proceduralFactory.createDesertMesa(28.0f, 35.0f, 28.0f, new Color(0.72f, 0.38f, 0.22f, 1.0f)));
        cachedModels.put(KEY_PROP_DESERT_CACTUS, proceduralFactory.createDesertCactus());
        cachedModels.put(KEY_PROP_CONTAINER_ORANGE, proceduralFactory.createShippingContainer(new Color(0.9f, 0.35f, 0.1f, 1.0f)));
        cachedModels.put(KEY_PROP_CONTAINER_BLUE, proceduralFactory.createShippingContainer(new Color(0.15f, 0.45f, 0.85f, 1.0f)));

        // 6. Obstacles
        cachedModels.put(KEY_OBS_CONE, proceduralFactory.createTrafficCone());
        cachedModels.put(KEY_OBS_BARRIER, proceduralFactory.createConcreteBarrier());

        // 7. Cockpit
        cachedModels.put(KEY_COCKPIT_DASH, proceduralFactory.createCockpitDashboard());
        cachedModels.put(KEY_COCKPIT_STEERING, proceduralFactory.createSteeringWheel());

        isLoaded = true;
    }

    /**
     * Obtains a new ModelInstance from a registered model key.
     */
    public ModelInstance createInstance(String modelKey) {
        Model model = getModel(modelKey);
        if (model != null) {
            return new ModelInstance(model);
        }
        return null;
    }

    /**
     * Gets a raw Model reference by key.
     */
    public Model getModel(String modelKey) {
        if (!isLoaded) {
            loadAllModels();
        }
        return cachedModels.get(modelKey);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void dispose() {
        proceduralFactory.dispose();
        cachedModels.clear();
        if (assetManager != null) {
            assetManager.dispose();
        }
        isLoaded = false;
    }
}
