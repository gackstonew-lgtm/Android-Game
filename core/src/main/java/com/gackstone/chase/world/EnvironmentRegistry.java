package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.gackstone.chase.assets.ModelRegistry;

/**
 * Registry holding preconfigured Environment Themes.
 */
public class EnvironmentRegistry {

    public static final String THEME_NEON_CITY = "env_neon_city";
    public static final String THEME_DESERT = "env_desert_canyon";
    public static final String THEME_INDUSTRIAL = "env_industrial_docks";

    private static final ObjectMap<String, EnvironmentTheme> THEMES = new ObjectMap<>();
    private static final Array<EnvironmentTheme> THEME_LIST = new Array<>();

    static {
        // 1. Neon City Metropolis (Night Cyberpunk)
        EnvironmentTheme neonCity = new EnvironmentTheme(
                THEME_NEON_CITY,
                "Neon Metropolis",
                "High-density cyberpunk cityscape with glowing skyscrapers and wet neon reflections.",
                new Color(0.2f, 0.25f, 0.38f, 1.0f),
                new Color(0.45f, 0.75f, 1.0f, 1.0f),
                new Vector3(-0.35f, -0.85f, 0.4f),
                new Color(0.06f, 0.08f, 0.12f, 1.0f),
                25.0f,
                180.0f,
                new Color(0.05f, 0.07f, 0.11f, 1.0f),
                ModelRegistry.KEY_PROP_ROAD_NEON,
                ModelRegistry.KEY_PROP_RAIL_NEON,
                new Color(0.06f, 0.07f, 0.1f, 1.0f),
                new String[]{
                        ModelRegistry.KEY_PROP_SKYSCRAPER_A,
                        ModelRegistry.KEY_PROP_SKYSCRAPER_B,
                        ModelRegistry.KEY_PROP_STREETLIGHT
                }
        );
        register(neonCity);

        // 2. Dustfall Canyon (Arid Desert Sunset)
        EnvironmentTheme desertCanyon = new EnvironmentTheme(
                THEME_DESERT,
                "Dustfall Canyon",
                "Blazing sunset highway flanked by towering sandstone mesas and saguaro cacti.",
                new Color(0.48f, 0.32f, 0.24f, 1.0f),
                new Color(1.0f, 0.65f, 0.35f, 1.0f),
                new Vector3(-0.6f, -0.6f, 0.52f),
                new Color(0.32f, 0.18f, 0.12f, 1.0f),
                30.0f,
                220.0f,
                new Color(0.38f, 0.20f, 0.14f, 1.0f),
                ModelRegistry.KEY_PROP_ROAD_DESERT,
                ModelRegistry.KEY_PROP_RAIL_DESERT,
                new Color(0.55f, 0.32f, 0.18f, 1.0f),
                new String[]{
                        ModelRegistry.KEY_PROP_DESERT_MESA,
                        ModelRegistry.KEY_PROP_DESERT_CACTUS
                }
        );
        register(desertCanyon);

        // 3. Industrial Docks (Overcast Heavy Freight Port)
        EnvironmentTheme industrial = new EnvironmentTheme(
                THEME_INDUSTRIAL,
                "Industrial Port",
                "Overcast shipping district filled with stacked containers and heavy infrastructure.",
                new Color(0.28f, 0.32f, 0.35f, 1.0f),
                new Color(0.75f, 0.85f, 0.82f, 1.0f),
                new Vector3(-0.4f, -0.9f, 0.3f),
                new Color(0.14f, 0.18f, 0.20f, 1.0f),
                20.0f,
                170.0f,
                new Color(0.12f, 0.15f, 0.18f, 1.0f),
                ModelRegistry.KEY_PROP_ROAD_INDUSTRIAL,
                ModelRegistry.KEY_PROP_RAIL_INDUSTRIAL,
                new Color(0.15f, 0.18f, 0.20f, 1.0f),
                new String[]{
                        ModelRegistry.KEY_PROP_CONTAINER_ORANGE,
                        ModelRegistry.KEY_PROP_CONTAINER_BLUE,
                        ModelRegistry.KEY_PROP_STREETLIGHT
                }
        );
        register(industrial);
    }

    public static void register(EnvironmentTheme theme) {
        THEMES.put(theme.getId(), theme);
        THEME_LIST.add(theme);
    }

    public static EnvironmentTheme getById(String id) {
        EnvironmentTheme theme = THEMES.get(id);
        return theme != null ? theme : THEME_LIST.first();
    }

    public static Array<EnvironmentTheme> getThemes() {
        return THEME_LIST;
    }
}
