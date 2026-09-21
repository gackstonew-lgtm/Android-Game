# Development Guide - Chase

This guide outlines workflows for building, debugging, testing, and expanding the **Chase** codebase.

---

## 1. Prerequisites

* **JDK**: Java Development Kit 17 (Eclipse Adoptium or OpenJDK).
* **Android SDK**: Compile SDK 35, Build Tools 34.0.0+, Min SDK 24.
* **IDE**: Android Studio Ladybug / Iguana or later.

---

## 2. Common Gradle Tasks

| Task | Command | Description |
| :--- | :--- | :--- |
| **Run Tests** | `./gradlew test` | Executes all JUnit 5 unit tests in the `:core` module |
| **Build Debug APK** | `./gradlew assembleDebug` | Generates the debug APK with debug symbols enabled |
| **Build Release APK** | `./gradlew assembleRelease` | Compiles optimized release build with ProGuard/R8 |
| **Clean Build** | `./gradlew clean` | Deletes build artifacts across all subprojects |

---

## 3. Developer Diagnostics Mode

The game includes a real-time diagnostics overlay:
* Enable via the in-game **SETTINGS** menu (toggle **DEBUG OVERLAY**).
* Displays live **FPS**, **Java/Native Heap usage**, **Player world coordinates**, **Speed**, **Pursuit distance**, and **Active collidables count**.

---

## 4. Asset Import Pipeline

1. **3D Models**: Place `.g3db` or `.obj` models in `assets/models/`. Load via LibGDX `AssetManager`.
2. **Audio**: Place `.ogg` or `.wav` sounds in `assets/audio/`. Register identifiers in `SoundRegistry.java`.
3. **Textures**: Place `.png` textures in `assets/textures/`.
