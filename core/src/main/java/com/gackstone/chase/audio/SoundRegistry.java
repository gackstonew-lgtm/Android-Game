package com.gackstone.chase.audio;

/**
 * Registry of sound IDs and audio event keys across all game categories:
 * Music, UI, Player, Enemy, Environment, Effects.
 */
public final class SoundRegistry {

    private SoundRegistry() {}

    // Audio Bus Categories
    public enum AudioCategory {
        MASTER,
        MUSIC,
        UI,
        PLAYER,
        ENEMY,
        EFFECTS
    }

    // Sound identifiers
    public static final String SND_UI_CLICK = "snd_ui_click";
    public static final String SND_ENGINE_LOOP = "snd_engine_loop";
    public static final String SND_SIREN_LOOP = "snd_siren_loop";
    public static final String SND_CRASH = "snd_crash";
    public static final String SND_DODGE = "snd_dodge";
    public static final String SND_GAME_OVER = "snd_game_over";
    public static final String MUS_MAIN_THEME = "mus_main_theme";
}
