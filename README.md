# Chase - 3D Mobile Chase & Pursuit Game

**Chase** is a production-grade native 3D pursuit game built for Android with Java 17 and libGDX 3D. The project is designed with a modular architecture that cleanly separates Android platform lifecycle concerns, core 3D gameplay simulation, spatial physics/collision detection, reactive UI, layered audio, and persistent data storage.

---

## Technical Highlights

* **Architecture**: Clean 3-tier separation (`android/` platform shell, `core/` game logic & 3D runtime, `data/` persistence bridge).
* **3D Game Runtime**: Pure code-driven OpenGL ES 3.0/2.0 rendering pipeline with `ModelBatch`, `PerspectiveCamera`, dynamic directional lighting, environmental fog, and procedural track generation.
* **Chase AI Engine**: Multi-state AI pursuit controller with dynamic speed catch-up modulation, lateral tracking, and evasive maneuver recovery.
* **Camera System**: Dedicated third-person chase camera with position lerping, look-ahead prediction, and rotation damping.
* **Input Abstraction**: Pluggable input interface supporting swipe gestures, smooth touch drag steering, dual-zone touch, and desktop keyboard fallbacks.
* **Zero-Allocation Loops**: Object pooling (`ObjectPoolManager`) and reusable math structures to avoid garbage collection stutter on mobile devices.
* **Responsive UI**: Scene2D UI system scaling automatically across 16:9, 18:9, 19.5:9, and tablet aspect ratios.
* **Lifecycle Resilience**: Full integration with Android `onPause`, `onResume`, and `onWindowFocusChanged` with sticky immersive full-screen landscape mode.

---

## Directory Layout

```text
Chase/
├── android/            # Android application module (Manifest, Launcher, Prefs bridge)
├── core/               # Platform-agnostic game logic, 3D runtime, AI, UI, physics
├── assets/             # Game assets (models, textures, audio, skins)
├── docs/               # System documentation (Architecture, Development, Performance)
├── build.gradle        # Root Gradle build configuration
├── settings.gradle     # Module definitions (:core, :android)
└── gradle.properties   # SDK, Gradle, and version settings
```

---

## How to Open in Android Studio

1. Launch **Android Studio**.
2. Select **Open** and choose this project root directory (`Chase`).
3. Android Studio will automatically recognize the Gradle structure and sync dependencies.

---

## Building and Testing

### Run Unit Tests
```bash
./gradlew test
```

### Build Android Debug APK
```bash
./gradlew assembleDebug
```

The output APK will be generated at:
`android/build/outputs/apk/debug/android-debug.apk`
