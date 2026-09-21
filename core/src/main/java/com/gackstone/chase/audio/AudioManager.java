package com.gackstone.chase.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Robust audio manager providing multi-bus volume mixing, mute states,
 * and safe playback wrappers.
 */
public class AudioManager {

    private static AudioManager instance;

    private float masterVolume = 1.0f;
    private float musicVolume = 0.8f;
    private float sfxVolume = 1.0f;
    private float uiVolume = 0.9f;
    private boolean isMuted = false;

    private final ObjectMap<String, Sound> loadedSounds = new ObjectMap<>();
    private final ObjectMap<String, Music> loadedMusic = new ObjectMap<>();
    private Music currentMusicTrack = null;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public AudioManager() {
    }

    public void playSound(String soundKey, SoundRegistry.AudioCategory category) {
        if (isMuted) return;

        Sound sound = loadedSounds.get(soundKey);
        if (sound != null) {
            float volume = getEffectiveVolume(category);
            try {
                sound.play(volume);
            } catch (Exception e) {
                System.err.println("[AudioManager] Error playing sound " + soundKey + ": " + e.getMessage());
            }
        }
    }

    public void playMusic(String musicKey, boolean loop) {
        if (isMuted) return;

        Music music = loadedMusic.get(musicKey);
        if (music != null) {
            if (currentMusicTrack != null && currentMusicTrack.isPlaying()) {
                currentMusicTrack.stop();
            }
            currentMusicTrack = music;
            currentMusicTrack.setLooping(loop);
            currentMusicTrack.setVolume(masterVolume * musicVolume);
            try {
                currentMusicTrack.play();
            } catch (Exception e) {
                System.err.println("[AudioManager] Error playing music " + musicKey + ": " + e.getMessage());
            }
        }
    }

    public void pauseMusic() {
        if (currentMusicTrack != null && currentMusicTrack.isPlaying()) {
            currentMusicTrack.pause();
        }
    }

    public void resumeMusic() {
        if (!isMuted && currentMusicTrack != null && !currentMusicTrack.isPlaying()) {
            currentMusicTrack.play();
        }
    }

    public void stopMusic() {
        if (currentMusicTrack != null) {
            currentMusicTrack.stop();
            currentMusicTrack = null;
        }
    }

    public float getEffectiveVolume(SoundRegistry.AudioCategory category) {
        if (isMuted) return 0.0f;

        switch (category) {
            case MUSIC:
                return masterVolume * musicVolume;
            case UI:
                return masterVolume * uiVolume;
            case PLAYER:
            case ENEMY:
            case EFFECTS:
                return masterVolume * sfxVolume;
            case MASTER:
            default:
                return masterVolume;
        }
    }

    public void registerSound(String key, Sound sound) {
        if (key != null && sound != null) {
            loadedSounds.put(key, sound);
        }
    }

    public void registerMusic(String key, Music music) {
        if (key != null && music != null) {
            loadedMusic.put(key, music);
        }
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateMusicVolume();
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateMusicVolume();
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void setUiVolume(float volume) {
        this.uiVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (isMuted) {
            if (currentMusicTrack != null && currentMusicTrack.isPlaying()) {
                currentMusicTrack.pause();
            }
        } else {
            updateMusicVolume();
            if (currentMusicTrack != null) {
                currentMusicTrack.play();
            }
        }
    }

    private void updateMusicVolume() {
        if (currentMusicTrack != null) {
            currentMusicTrack.setVolume(isMuted ? 0.0f : masterVolume * musicVolume);
        }
    }

    public float getMasterVolume() { return masterVolume; }
    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume() { return sfxVolume; }
    public float getUiVolume() { return uiVolume; }
    public boolean isMuted() { return isMuted; }

    public void dispose() {
        stopMusic();
        for (Sound sound : loadedSounds.values()) {
            sound.dispose();
        }
        for (Music music : loadedMusic.values()) {
            music.dispose();
        }
        loadedSounds.clear();
        loadedMusic.clear();
    }
}
