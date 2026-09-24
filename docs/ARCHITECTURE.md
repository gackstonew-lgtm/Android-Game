# Chase – Architecture Reference

## Overview

Chase is a real-time 3D Android car-chase game built on **Java 17 + libGDX 1.12.1**. The codebase follows a strict three-tier separation:

```
android/   ← Android platform shell (Launcher, Preferences bridge)
core/      ← Platform-agnostic game logic, 3D runtime, AI, UI, physics
assets/    ← Runtime assets (models, textures, audio, particles, data)
```

---

## Package Structure

```
com.gackstone.chase
├── ChaseGame.java               Root Game, state machine coordinator
│
├── assets/
│   ├── ModelRegistry.java       Central 3D model cache (AssetManager + factory)
│   └── ProceduralModelFactory.java  High-fidelity multi-part mesh builder
│
├── audio/
│   ├── AudioManager.java        Multi-bus audio mixer (singleton)
│   └── SoundRegistry.java       Sound key constants & category enum
│
├── camera/
│   ├── ChaseCamera.java         Chase / Hood / Cockpit camera modes
│   └── CameraMode.java          Mode enum (CHASE, HOOD, COCKPIT)
│
├── cars/
│   ├── CarDefinition.java       Data record: stats, model key, price
│   └── CarRegistry.java         Static registry of all vehicles
│
├── core/
│   ├── GameConfig.java          All tunable constants (no magic numbers)
│   ├── GameEvents.java          Decoupled event bus
│   ├── GameManager.java         Active gameplay system coordinator
│   ├── GameState.java           State enum (includes GARAGE, ENV_SELECT)
│   └── GameStateMachine.java    Deterministic state machine
│
├── enemy/
│   ├── ChaseAIState.java        AI behaviour states
│   ├── ChaseController.java     Per-enemy pursuit physics + side-ram AI
│   ├── EnemyEntity.java         3D enemy vehicle (ModelRegistry-backed)
│   └── EnemyManager.java        Multi-enemy lifecycle & difficulty escalation
│
├── input/
│   ├── IInputController.java    Pluggable input abstraction
│   └── TouchGestureController.java  Swipe + drag steering
│
├── physics/
│   ├── CollisionLayer.java      PLAYER / ENEMY / OBSTACLE layer enum
│   ├── CollisionManager.java    Broad-phase AABB collision dispatcher
│   └── ICollidable.java         Collidable entity contract
│
├── player/
│   ├── PlayerController.java    Arcade physics integration (speed/bank/steer)
│   ├── PlayerEntity.java        3D player vehicle (ModelRegistry + cockpit)
│   └── PlayerState.java         Dynamic state: health, speed, nitro, steer
│
├── pool/
│   └── ObjectPoolManager.java   Vector3 pool + libGDX Pools wrapper
│
├── save/
│   ├── GamePreferencesData.java Player prefs, stats, coins, car/theme IDs
│   ├── ISaveStorage.java        Storage abstraction interface
│   └── SaveManager.java         Load/save/recordRun (+ coin rewards)
│
├── ui/
│   ├── DebugOverlay.java        FPS/memory/entity telemetry (dev only)
│   ├── EnvSelectScreen.java     Environment theme selector
│   ├── GameHUDScreen.java       In-game HUD (speed/health/nitro/CAM)
│   ├── GameOverScreen.java      Session-end summary
│   ├── GarageScreen.java        Car picker with stat bars & unlock logic
│   ├── MainMenuScreen.java      Main menu → Garage flow
│   ├── PauseMenuScreen.java     Pause overlay
│   ├── SettingsScreen.java      Audio/graphics settings
│   └── UIManager.java           Scene2D skin & typography factory
│
└── world/
    ├── EnvironmentRegistry.java  Static map of all environment themes
    ├── EnvironmentRenderer.java  Themed track, props, lighting, fog
    ├── EnvironmentTheme.java     Lighting + fog + model key config record
    ├── ObstacleEntity.java       Typed obstacle (cone / jersey barrier)
    ├── RoadsidePropEntity.java   Pooled roadside decoration
    ├── TrafficVehicleEntity.java Dynamic civilian traffic (ICollidable)
    └── WorldManager.java         World lifecycle orchestrator
```

---

## Data Flow

```
ChaseGame.create()
  └─ GameManager (owns ModelRegistry, injects into all subsystems)
       ├─ PlayerEntity       ← uses ModelRegistry
       ├─ EnemyManager       ← uses ModelRegistry, spawns 1+2 enemies
       ├─ WorldManager
       │    └─ EnvironmentRenderer  ← uses ModelRegistry for roads/rails/props
       ├─ CollisionManager   ← AABB broad-phase
       └─ ChaseCamera        ← CHASE / HOOD / COCKPIT modes
```

---

## Rendering Pipeline (PLAYING state)

```
ChaseGame.render()
  1. Gdx.gl.glClear(COLOR | DEPTH)
  2. GameManager.update(delta)       ← physics, AI, scoring
  3. GameManager.render()
       ├─ ModelBatch.begin(camera)
       ├─ WorldManager.render()      ← terrain, roads, rails, props, obstacles, traffic
       ├─ PlayerEntity.render()      ← car body
       ├─ PlayerEntity.renderCockpit() [COCKPIT mode only]
       ├─ EnemyManager.render()      ← police vehicles
       └─ ModelBatch.end()
  4. GameHUDScreen.render()          ← Scene2D HUD overlay
  5. DebugOverlay.render() [if enabled]
```

---

## Car Physics Model (Arcade)

| Parameter       | Mechanism |
|----------------|-----------|
| **Forward speed** | Linearly accelerates toward `car.maxSpeed` at `car.acceleration × 0.1` m/s² |
| **Lateral motion** | Input steer (0..1) × `car.handling`; inertia lerp at 14 Hz |
| **Bank angle** | Visual roll = `-steer × 22°`, smoothed at `PLAYER_BANK_SMOOTHING` Hz |
| **Enemy catch-up** | Speed multiplier scales from 1.3× (far) down to 1.02× (close) |
| **Collision** | AABB; damage scales inversely with car's max health (armour) |

No third-party physics engine used. All motion integrated with explicit Euler in hot-path methods annotated as zero-allocation.

---

## State Machine

```
BOOT → LOADING → MAIN_MENU → GARAGE → ENV_SELECT → PLAYING
                                                        ↓
                                         PAUSED ←→ PLAYING
                                                        ↓
                                                  GAME_OVER → MAIN_MENU
SETTINGS accessible from MAIN_MENU at any time.
```

---

## Threading

All gameplay state is confined to the libGDX GL thread. `GameEvents` uses `CopyOnWriteArrayList` for safe cross-thread listener registration from Android Activity callbacks only. No background threads own game data.
