package com.gackstone.chase.core;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight decoupled game event bus for dispatching gameplay occurrences
 * such as player damage, collisions, score changes, and enemy capture.
 */
public class GameEvents {

    public interface GameEventListener {
        default void onPlayerDamaged(float currentHealth, float damageAmount) {}
        default void onPlayerCaught(float finalDistance, long finalScore) {}
        default void onObstacleHit(float damage) {}
        default void onScoreUpdated(long currentScore, float currentDistance) {}
        default void onGameRestarted() {}
    }

    private static final CopyOnWriteArrayList<GameEventListener> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public static void firePlayerDamaged(float currentHealth, float damageAmount) {
        for (GameEventListener listener : listeners) {
            listener.onPlayerDamaged(currentHealth, damageAmount);
        }
    }

    public static void firePlayerCaught(float finalDistance, long finalScore) {
        for (GameEventListener listener : listeners) {
            listener.onPlayerCaught(finalDistance, finalScore);
        }
    }

    public static void fireObstacleHit(float damage) {
        for (GameEventListener listener : listeners) {
            listener.onObstacleHit(damage);
        }
    }

    public static void fireScoreUpdated(long currentScore, float currentDistance) {
        for (GameEventListener listener : listeners) {
            listener.onScoreUpdated(currentScore, currentDistance);
        }
    }

    public static void fireGameRestarted() {
        for (GameEventListener listener : listeners) {
            listener.onGameRestarted();
        }
    }

    public static void clearListeners() {
        listeners.clear();
    }
}
