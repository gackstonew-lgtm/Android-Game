package com.gackstone.chase.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gackstone.chase.assets.ModelRegistry;

/**
 * Encapsulates full atmospheric, lighting, material, and prop rules for a game environment.
 */
public class EnvironmentTheme {

    private final String id;
    private final String name;
    private final String description;

    // Lighting
    private final Color ambientLight;
    private final Color directionalLightColor;
    private final Vector3 lightDirection;

    // Fog & Atmosphere
    private final Color fogColor;
    private final float fogNear;
    private final float fogFar;
    private final Color skyClearColor;

    // Model Keys
    private final String roadModelKey;
    private final String railModelKey;
    private final Color terrainColor;

    // Prop Keys
    private final Array<String> propKeys;

    public EnvironmentTheme(String id, String name, String description,
                            Color ambientLight, Color directionalLightColor, Vector3 lightDirection,
                            Color fogColor, float fogNear, float fogFar, Color skyClearColor,
                            String roadModelKey, String railModelKey, Color terrainColor,
                            String[] props) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ambientLight = ambientLight;
        this.directionalLightColor = directionalLightColor;
        this.lightDirection = lightDirection.nor();
        this.fogColor = fogColor;
        this.fogNear = fogNear;
        this.fogFar = fogFar;
        this.skyClearColor = skyClearColor;
        this.roadModelKey = roadModelKey;
        this.railModelKey = railModelKey;
        this.terrainColor = terrainColor;
        this.propKeys = new Array<>(props);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public Color getAmbientLight() { return ambientLight; }
    public Color getDirectionalLightColor() { return directionalLightColor; }
    public Vector3 getLightDirection() { return lightDirection; }

    public Color getFogColor() { return fogColor; }
    public float getFogNear() { return fogNear; }
    public float getFogFar() { return fogFar; }
    public Color getSkyClearColor() { return skyClearColor; }

    public String getRoadModelKey() { return roadModelKey; }
    public String getRailModelKey() { return railModelKey; }
    public Color getTerrainColor() { return terrainColor; }
    public Array<String> getPropKeys() { return propKeys; }
}
