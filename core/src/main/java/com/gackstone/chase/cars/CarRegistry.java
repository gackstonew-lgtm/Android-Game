package com.gackstone.chase.cars;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.gackstone.chase.assets.ModelRegistry;

/**
 * Registry of all available cars in Chase.
 * Provides preconfigured vehicles for players, enemies, and traffic.
 */
public class CarRegistry {

    private static final ObjectMap<String, CarDefinition> CARS_MAP = new ObjectMap<>();
    private static final Array<CarDefinition> PLAYER_CARS = new Array<>();
    private static final Array<CarDefinition> ENEMY_CARS = new Array<>();

    static {
        // Player 1: Phantom GT
        CarDefinition phantomGT = new CarDefinition(
                ModelRegistry.KEY_PLAYER_PHANTOM_GT,
                "Phantom GT",
                CarDefinition.VehicleType.PLAYER,
                "High-revving twin-turbo exotic. Exceptional top speed and razor-sharp handling.",
                ModelRegistry.KEY_PLAYER_PHANTOM_GT,
                52.0f,  // Max speed
                18.5f,  // Acceleration
                22.0f,  // Handling
                100.0f, // Health
                1350.0f,// Mass
                true,   // Unlocked
                0,
                new Color(0.0f, 0.85f, 1.0f, 1.0f),
                new Color(0.12f, 0.14f, 0.20f, 1.0f),
                new Color(0.0f, 1.0f, 1.0f, 1.0f)
        );
        register(phantomGT);

        // Player 2: Apex Muscle
        CarDefinition apexMuscle = new CarDefinition(
                ModelRegistry.KEY_PLAYER_APEX_MUSCLE,
                "Apex Interceptor V8",
                CarDefinition.VehicleType.PLAYER,
                "American supercharged V8 beast. Unmatched straight-line acceleration and rugged armor.",
                ModelRegistry.KEY_PLAYER_APEX_MUSCLE,
                48.0f,
                23.0f,
                17.5f,
                140.0f,
                1780.0f,
                false,
                1500,
                new Color(1.0f, 0.45f, 0.0f, 1.0f),
                new Color(0.15f, 0.15f, 0.18f, 1.0f),
                new Color(1.0f, 0.7f, 0.0f, 1.0f)
        );
        register(apexMuscle);

        // Player 3: Vanguard Armored Titan
        CarDefinition vanguardTitan = new CarDefinition(
                ModelRegistry.KEY_PLAYER_VANGUARD,
                "Vanguard Titan",
                CarDefinition.VehicleType.PLAYER,
                "Reinforced tactical assault cruiser. Smashes through obstacles with heavy armor at the expense of agility.",
                ModelRegistry.KEY_PLAYER_VANGUARD,
                44.0f,
                15.0f,
                14.0f,
                220.0f,
                2400.0f,
                false,
                3500,
                new Color(0.85f, 0.2f, 0.25f, 1.0f),
                new Color(0.18f, 0.2f, 0.24f, 1.0f),
                new Color(1.0f, 0.1f, 0.1f, 1.0f)
        );
        register(vanguardTitan);

        // Player 4: Shadowblade EX Hypercar
        CarDefinition shadowbladeEX = new CarDefinition(
                ModelRegistry.KEY_PLAYER_SHADOWBLADE,
                "Shadowblade EX",
                CarDefinition.VehicleType.PLAYER,
                "Carbon-fiber experimental hypercar. Extreme top speed and track grip engineered for outrunning heavy police roadblocks.",
                ModelRegistry.KEY_PLAYER_SHADOWBLADE,
                58.0f,  // Max speed
                24.5f,  // Acceleration
                24.0f,  // Handling
                120.0f, // Health
                1220.0f,// Mass
                false,  // Unlocked
                5000,   // Unlock price
                new Color(0.6f, 0.15f, 0.9f, 1.0f),
                new Color(0.12f, 0.12f, 0.16f, 1.0f),
                new Color(1.0f, 0.0f, 0.8f, 1.0f)
        );
        register(shadowbladeEX);

        // Enemy 1: State Patrol Pursuit Cruiser
        CarDefinition patrolCruiser = new CarDefinition(
                ModelRegistry.KEY_ENEMY_PATROL,
                "Highway Patrol Cruiser",
                CarDefinition.VehicleType.ENEMY,
                "Standard state pursuit unit. Agile and relentless tracking in pack formation.",
                ModelRegistry.KEY_ENEMY_PATROL,
                54.0f,
                19.0f,
                19.0f,
                110.0f,
                1600.0f,
                true,
                0,
                Color.WHITE,
                Color.BLACK,
                Color.RED
        );
        register(patrolCruiser);

        // Enemy 2: Tactical Heavy SWAT Interceptor
        CarDefinition tacticalSUV = new CarDefinition(
                ModelRegistry.KEY_ENEMY_TACTICAL_SUV,
                "Tactical SWAT Interceptor",
                CarDefinition.VehicleType.ENEMY,
                "Heavy armored enforcement vehicle designed to perform PIT maneuvers and establish roadblocks.",
                ModelRegistry.KEY_ENEMY_TACTICAL_SUV,
                47.0f,
                17.0f,
                15.0f,
                250.0f,
                2600.0f,
                true,
                0,
                new Color(0.12f, 0.14f, 0.18f, 1.0f),
                new Color(0.25f, 0.28f, 0.35f, 1.0f),
                Color.BLUE
        );
        register(tacticalSUV);
    }

    public static void register(CarDefinition def) {
        CARS_MAP.put(def.getId(), def);
        if (def.getType() == CarDefinition.VehicleType.PLAYER) {
            PLAYER_CARS.add(def);
        } else if (def.getType() == CarDefinition.VehicleType.ENEMY) {
            ENEMY_CARS.add(def);
        }
    }

    public static CarDefinition getById(String id) {
        CarDefinition def = CARS_MAP.get(id);
        return def != null ? def : PLAYER_CARS.first();
    }

    public static Array<CarDefinition> getPlayerCars() {
        return PLAYER_CARS;
    }

    public static Array<CarDefinition> getEnemyCars() {
        return ENEMY_CARS;
    }
}
