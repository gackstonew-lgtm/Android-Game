package com.gackstone.chase.core;

/**
 * Explicit lifecycle states of the Chase game.
 * Avoids scattered boolean flags and ensures clean, deterministic transitions.
 */
public enum GameState {
    /** Initial engine and asset boot phase */
    BOOT,
    
    /** Asset preloading / level transition phase */
    LOADING,
    
    /** Primary menu where player can start, tweak settings, or exit */
    MAIN_MENU,
    
    /** Active 3D gameplay state */
    PLAYING,
    
    /** Gameplay loop frozen, UI interactable */
    PAUSED,
    
    /** Chase ended (caught by enemy or collided with fatal obstacle) */
    GAME_OVER,
    
    /** Settings and configuration screen */
    SETTINGS,
    
    /** Future expansion hook: pre-game countdown (3, 2, 1, GO!) */
    COUNTDOWN,
    
    /** Future expansion hook: respawning after crash */
    RESPAWNING,
    
    /** Future expansion hook: mission / stage complete */
    MISSION_COMPLETE
}
