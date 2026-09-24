# Chase v2.0 – Development Guide

## 1. Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | Java 17 (Eclipse Adoptium or OpenJDK) |
| Android SDK | compileSdk 35, minSdk 24 |
| Android Studio | Ladybug / Iguana or later |
| libGDX | 1.12.1 (declared in `gradle.properties`) |
| Gradle | 8.9 (wrapper; `gradle/wrapper/gradle-wrapper.properties`) |

---

## 2. Common Gradle Tasks

| Task | Command | Description |
|------|---------|-------------|
| Run unit tests | `./gradlew test` | JUnit 5 tests in `:core` |
| Debug APK | `./gradlew assembleDebug` | Debug build with symbols |
| Release APK | `./gradlew assembleRelease` | Minified + ProGuard |
| Clean | `./gradlew clean` | Delete all build artefacts |
| Compile only | `./gradlew :core:compileJava` | Fast compile check |

Output APK: `android/build/outputs/apk/debug/android-debug.apk`

---

## 3. Project Structure at a Glance

```
Chase/
├── android/          Android shell (Launcher, Prefs bridge)
├── core/             Game logic, 3D runtime, AI, UI, physics
├── assets/           Runtime assets (models, audio, textures, data)
│   ├── data/         cars.json, environments.json
│   ├── models/       cars/, props/, environments/, drivers/
│   ├── audio/        sounds/, music/
│   ├── textures/
│   ├── particles/
│   └── ui/
├── docs/             ARCHITECTURE.md, DEVELOPMENT.md, ASSETS.md
├── build.gradle      Root build config
├── gradle.properties SDK versions, libGDX version, app ID
└── settings.gradle   Module definitions (:core, :android)
```

---

## 4. Architecture Quick Reference

| System | Key Class | Owns |
|--------|-----------|------|
| Asset pipeline | `ModelRegistry` | `ProceduralModelFactory`, `AssetManager` |
| Player vehicle | `PlayerEntity` | `ModelInstance`, `PlayerState` |
| Enemy AI | `EnemyManager` + `ChaseController` | `EnemyEntity[]` |
| World generation | `WorldManager` | `EnvironmentRenderer`, obstacles, traffic |
| Camera | `ChaseCamera` | `PerspectiveCamera`, `CameraMode` |
| UI | `ChaseGame` | All `ScreenAdapter` instances |
| Persistence | `SaveManager` | `GamePreferencesData`, `ISaveStorage` |
| Events | `GameEvents` | `CopyOnWriteArrayList<GameEventListener>` |

See **ARCHITECTURE.md** for the full class map and data-flow diagrams.

---

## 5. Adding a New Car (Step-by-Step)

1. **Model**: Export from Blender → `assets/models/cars/my_car.g3db`
2. **Factory method** *(optional)*: Add `createMyCar(...)` to `ProceduralModelFactory`
3. **Registry constant** in `ModelRegistry`:
   ```java
   public static final String KEY_PLAYER_MY_CAR = "player_my_car";
   ```
4. **Baking call** inside `ModelRegistry.loadAllModels()`:
   ```java
   cachedModels.put(KEY_PLAYER_MY_CAR, proceduralFactory.createMyCar(...));
   ```
5. **CarDefinition** in `CarRegistry` static block:
   ```java
   register(new CarDefinition(
       KEY_PLAYER_MY_CAR, "My Car", VehicleType.PLAYER, "Description",
       KEY_PLAYER_MY_CAR,
       52f, 18f, 20f, 110f, 1400f,   // speed/accel/handling/health/mass
       false, 2000,                   // locked, price
       primaryColor, secondaryColor, glowColor
   ));
   ```
6. The car **immediately** appears in the Garage screen.

---

## 6. Adding a New Environment Theme

1. **Gather props**: reuse existing `ModelRegistry` keys or add new ones.
2. **Theme constant** in `EnvironmentRegistry`:
   ```java
   public static final String THEME_MY_THEME = "env_my_theme";
   ```
3. **Register** in the static block:
   ```java
   register(new EnvironmentTheme(
       THEME_MY_THEME, "Display Name", "Description",
       ambientColor, directionalColor, lightDir,
       fogColor, fogNear, fogFar, skyClearColor,
       ModelRegistry.KEY_PROP_ROAD_NEON,   // road model key
       ModelRegistry.KEY_PROP_RAIL_NEON,   // rail model key
       terrainColor,
       new String[]{ ModelRegistry.KEY_PROP_STREETLIGHT }
   ));
   ```
4. The theme **immediately** appears in the Track Select screen.

---

## 7. Camera Modes

| Mode | HUD Label | Description |
|------|-----------|-------------|
| `CHASE` | `[CHASE]` | Third-person trailing, lerped position & look-ahead |
| `HOOD` | `[HOOD]` | Bonnet-mounted, highly responsive |
| `COCKPIT` | `[COCKPIT]` | Interior eye-level; cockpit mesh rendered |

Cycle with the **CAM** button in the HUD. Persists for the current session only.

---

## 8. Debug / Diagnostics Overlay

Enable via **Settings → Debug Overlay** toggle. Displays:

- FPS
- Java Heap / Native Heap (MB)
- Player world coordinates, speed, active car name
- Enemy count + closest distance
- Active collision objects
- Current environment theme
- Camera mode

---

## 9. Coin Economy

- Players earn **1 coin per 10 metres** driven (rewarded in `SaveManager.recordRunResult`).
- Starting balance: `GameConfig.PLAYER_STARTING_COINS` (500).
- Cars with `unlockPrice > 0` require sufficient coins to select in the Garage.
- `GamePreferencesData.spendCoins(amount)` is atomic and refuses if balance is insufficient.

---

## 10. Asset Import Pipeline

### 3D Models (`.g3db`)
1. Design in **Blender 3.x** or any DCC tool.
2. Export as FBX → convert with **fbx-conv** (`fbx-conv -f model.fbx`) → `.g3db`.
3. Drop into `assets/models/<category>/`.
4. Register in `ModelRegistry`.

### Audio (`.ogg` recommended)
1. Record or source audio at 44.1 kHz stereo (music) / mono (SFX).
2. Export to Ogg Vorbis (VBR Q4–Q6 for music, Q2–Q4 for SFX).
3. Drop into `assets/audio/sounds/` or `assets/audio/music/`.
4. Add a key constant in `SoundRegistry`.
5. Call `AudioManager.registerSound(key, Gdx.audio.newSound(...))` at startup.

### Textures
- Use **power-of-two** dimensions (512×512, 1024×1024, 2048×2048).
- Compress to **ETC2** (Android) via texture packer or shell script.
- Drop `.png` files into `assets/textures/<category>/`.

---

## 11. ProGuard / R8 Considerations

The release build enables minification. libGDX reflection-based classes are already
covered by the bundled `proguard-android-optimize.txt`. For new data classes loaded
via JSON, add to `proguard-rules.pro`:

```proguard
-keep class com.gackstone.chase.cars.** { *; }
-keep class com.gackstone.chase.world.** { *; }
```

---

## 12. Performance Guidelines

| Practice | Rationale |
|----------|-----------|
| Use `ObjectPoolManager.obtainVector3()` for transient math | Avoid GC pressure in 60 Hz hot path |
| `ModelRegistry.createInstance(key)` not `new ModelInstance(new Model(...))` | Re-use baked model data |
| Extend `Pool<T>` for props and traffic | Zero allocation on recycle |
| Keep `EnvironmentRenderer` prop count ≤ 80 simultaneous | Android GPU fillrate limit |
| Profile with **Android GPU Inspector** targeting 60 FPS on a mid-range device | Baseline: Snapdragon 720G / Mali-G76 |
