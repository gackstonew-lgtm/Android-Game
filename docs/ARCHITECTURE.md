# System Architecture - Chase

This document details the architectural layers and interactions within the **Chase** mobile application.

---

## 1. Architectural Layers

### Android Layer (`android/`)
* **`AndroidLauncher`**: Extends `AndroidApplication`, initializes the OpenGL ES 3.0 runtime, configures window flags (`KEEP_SCREEN_ON`, immersive sticky full-screen), and forwards Android lifecycle hooks.
* **`AndroidPreferencesBridge`**: Implements `ISaveStorage` by mapping key-value persistence to native Android `SharedPreferences`.

### Core Game Layer (`core/`)
* **Master Coordinator (`ChaseGame`)**: Inherits from `Game`, registers the state machine, initializes asset managers, dispatches input events, and routes frames according to the active game state.
* **Game Session Orchestrator (`GameManager`)**: Handles the live simulation tick loop, player controller updates, enemy pursuit AI execution, procedural track generation, obstacle recycling, and score accumulation.
* **State Machine (`GameStateMachine`)**: Enforces explicit state transitions (`BOOT`, `LOADING`, `MAIN_MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`, `SETTINGS`).
* **Physics & Collision (`CollisionManager`)**: Performs broad and narrow phase AABB bounding-box intersection tests across registered physical layers without per-frame allocations.
* **AI Pursuit System (`ChaseController`)**: Implements states (`IDLE`, `SEARCHING`, `CHASING`, `LOST_TARGET`, `RECOVERING`, `DISABLED`) with dynamic pursuit catch-up speed modulation.
* **Chase Camera (`ChaseCamera`)**: Interpolates 3D coordinates behind the player with look-ahead prediction and rotation damping.
* **Audio Manager (`AudioManager`)**: Multi-bus volume hierarchy (`Master`, `Music`, `UI`, `Player`, `Enemy`, `Effects`) with mute safety and graceful audio playback.
* **UI & HUD (`UIManager`, `GameHUDScreen`, `MainMenuScreen`, `PauseMenuScreen`, `GameOverScreen`, `SettingsScreen`)**: Responsive Scene2D interfaces with procedural skins.

### Data Layer (`core/save/`)
* **`ISaveStorage`**: Decoupled persistent storage interface.
* **`GamePreferencesData`**: Data transfer object holding player settings, audio volumes, graphics presets, and high score records.
* **`SaveManager`**: Handles serialization, deserialization, and high-score recording logic.

---

## 2. Gameplay Loop & State Diagram

```
                 ┌────────────────────────────────┐
                 │             BOOT               │
                 └───────────────┬────────────────┘
                                 │
                                 ▼
                         ┌───────────────┐
           ┌────────────►│   MAIN_MENU   │◄────────────┐
           │             └───────┬───────┘             │
           │                     │                     │
           │ (Save & Back)       │ (PLAY)              │ (Main Menu)
           │                     ▼                     │
    ┌──────────────┐     ┌───────────────┐     ┌───────────────┐
    │   SETTINGS   │     │    PLAYING    │────►│   GAME_OVER   │
    └──────────────┘     └───────┬───────┘     └───────────────┘
                                 │ ▲                   ▲
                         (Pause) │ │ (Resume)          │ (Caught)
                                 ▼ │                   │
                         ┌───────────────┐             │
                         │    PAUSED     │─────────────┘
                         └───────────────┘
```

---

## 3. Collision Matrix

| Source \ Target | PLAYER | ENEMY | OBSTACLE | COLLECTIBLE | ROAD_BOUNDARY |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **PLAYER** | - | Fatal (100 HP) | Damage (25 HP) | Pickup | Bound Clamp |
| **ENEMY** | Fatal | - | Avoid / Glance | - | Bound Clamp |
| **OBSTACLE** | Contact | - | - | - | - |
