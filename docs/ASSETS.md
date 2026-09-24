# Chase – Assets Reference (ASSETS.md)

All runtime assets live in `assets/` which is mapped as the Android assets source-set
in `android/build.gradle` via `assets.srcDirs = ['../assets']`.

---

## Directory Structure

```
assets/
├── data/
│   ├── cars.json              Car definitions (id, stats, model path, colours)
│   └── environments.json      Environment theme definitions
│
├── models/
│   ├── cars/
│   │   ├── phantom_gt.g3db        Player – Phantom GT supercar
│   │   ├── apex_muscle.g3db       Player – Apex Interceptor V8
│   │   ├── vanguard_armored.g3db  Player – Vanguard Titan
│   │   ├── patrol_cruiser.g3db    Enemy  – Highway Patrol Cruiser
│   │   └── tactical_suv.g3db      Enemy  – Tactical SWAT SUV
│   │
│   ├── props/
│   │   ├── traffic_cone.g3db
│   │   ├── concrete_barrier.g3db
│   │   ├── street_light.g3db
│   │   ├── container_orange.g3db
│   │   ├── container_blue.g3db
│   │   ├── traffic_sedan.g3db
│   │   └── traffic_truck.g3db
│   │
│   ├── environments/
│   │   ├── skyscraper_a.g3db      Neon City – tall tower A
│   │   ├── skyscraper_b.g3db      Neon City – tall tower B
│   │   ├── desert_mesa.g3db       Desert Canyon – sandstone mesa
│   │   └── desert_cactus.g3db     Desert Canyon – saguaro cactus
│   │
│   └── drivers/
│       ├── cockpit_dashboard.g3db  First-person cockpit dashboard
│       └── steering_wheel.g3db    Animated steering wheel
│
├── textures/
│   ├── cars/                      Albedo + normal maps per car
│   ├── road/                      Asphalt, lane markings, curbs
│   └── props/                     Prop texture atlases
│
├── audio/
│   ├── sounds/
│   │   ├── ui_click.ogg
│   │   ├── engine_loop.ogg        Looped RPM engine sound
│   │   ├── siren_loop.ogg         Police siren loop
│   │   ├── crash.ogg
│   │   ├── dodge.ogg
│   │   └── game_over.ogg
│   └── music/
│       └── main_chase_theme.ogg
│
├── particles/
│   ├── exhaust.pfx                Particle effect descriptor
│   ├── sparks.pfx
│   └── smoke.pfx
│
└── ui/
    ├── ui_skin.json               Scene2D skin descriptor (optional)
    └── icons/                     App icons at various densities
```

> **Note:** `models/` files ending in `.g3db` are libGDX binary model format.
> To use glTF (`.gltf` / `.glb`) add the `gdx-gltf` library dependency and
> register a `GLTFLoader` with the `AssetManager` (see DEVELOPMENT.md).

---

## Model Registration Flow

```
ModelRegistry.loadAllModels()
  └─ ProceduralModelFactory.create*()   ← generates multi-part ModelBuilder mesh
       └─ builtModels[]                 ← owned by factory, disposed with it
  └─ cachedModels.put(KEY, model)       ← O(1) instance lookup at runtime
```

At runtime call `modelRegistry.createInstance(KEY)` to obtain a new `ModelInstance`
sharing the cached `Model` data. **Never call `ModelInstance.dispose()`** — only
the backing `Model` in `ModelRegistry` needs disposal.

---

## Adding a New Car

1. **Create the 3D model** (Blender → export `.g3db` via fbx-conv or gdx-gltf).
2. **Drop it** into `assets/models/cars/my_new_car.g3db`.
3. **Add a `ProceduralModelFactory` method** (or register the file path in
   `ModelRegistry` and queue it via `AssetManager`).
4. **Add a constant** in `ModelRegistry`: `KEY_PLAYER_MY_CAR = "player_my_car"`.
5. **Register in `CarRegistry`** static block:
   ```java
   CarDefinition myCar = new CarDefinition(
       "player_my_car", "My Car Name", CarDefinition.VehicleType.PLAYER,
       "Short description.",
       ModelRegistry.KEY_PLAYER_MY_CAR,
       55.0f,  // maxSpeed
       20.0f,  // acceleration
       19.0f,  // handling
       120.0f, // maxHealth
       1500.0f,// mass
       false,  // unlockedByDefault
       2000,   // unlockPrice
       new Color(0.5f, 0.1f, 0.9f, 1.0f),  // primary
       Color.BLACK,                           // secondary
       Color.PURPLE                           // glow
   );
   register(myCar);
   ```
6. The car immediately appears in the **Garage** screen with correct stat bars.

---

## Adding a New Environment Theme

1. **Decide props** – either reuse existing `ModelRegistry` keys or add new models.
2. **Add a constant** in `EnvironmentRegistry`: `THEME_MY_THEME = "env_my_theme"`.
3. **Register in `EnvironmentRegistry`** static block:
   ```java
   EnvironmentTheme myTheme = new EnvironmentTheme(
       THEME_MY_THEME,
       "Rainy Night Highway",
       "Wet motorway under torrential rain.",
       new Color(0.2f, 0.22f, 0.28f, 1.0f),   // ambientLight
       new Color(0.8f, 0.85f, 0.9f, 1.0f),    // directionalColor
       new Vector3(-0.3f, -0.85f, 0.45f),      // lightDir
       new Color(0.08f, 0.10f, 0.14f, 1.0f),  // fogColor
       18.0f,                                   // fogNear
       150.0f,                                  // fogFar
       new Color(0.06f, 0.08f, 0.12f, 1.0f),  // skyClearColor
       ModelRegistry.KEY_PROP_ROAD_NEON,        // road model key
       ModelRegistry.KEY_PROP_RAIL_NEON,        // rail model key
       new Color(0.07f, 0.08f, 0.12f, 1.0f),  // terrainColor
       new String[]{
           ModelRegistry.KEY_PROP_STREETLIGHT,
           ModelRegistry.KEY_PROP_SKYSCRAPER_A
       }
   );
   register(myTheme);
   ```
4. The environment appears immediately in the **Track Select** screen.

---

## Audio Wiring

Sound files must be registered at startup via `AudioManager.registerSound(key, sound)`.
Use `SoundRegistry` constants as keys. The multi-bus mixer applies:

| Bus | Volume formula |
|-----|----------------|
| MUSIC | `masterVolume × musicVolume` |
| PLAYER / ENEMY / EFFECTS | `masterVolume × sfxVolume` |
| UI | `masterVolume × uiVolume` |
