package com.gackstone.chase.core;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * State machine managing transitions between {@link GameState}s and broadcasting
 * transition events to registered listeners.
 */
public class GameStateMachine {

    public interface StateChangeListener {
        void onStateChanged(GameState previousState, GameState newState);
    }

    private GameState currentState = GameState.BOOT;
    private GameState previousState = GameState.BOOT;
    private final CopyOnWriteArrayList<StateChangeListener> listeners = new CopyOnWriteArrayList<>();

    public GameStateMachine() {
    }

    public synchronized boolean transitionTo(GameState newState) {
        if (newState == null || newState == currentState) {
            return false;
        }

        // Validate state transitions
        if (!isValidTransition(currentState, newState)) {
            System.err.println("[GameStateMachine] Warning: Invalid transition requested from " 
                + currentState + " to " + newState);
            return false;
        }

        this.previousState = this.currentState;
        this.currentState = newState;

        for (StateChangeListener listener : listeners) {
            try {
                listener.onStateChanged(previousState, currentState);
            } catch (Exception e) {
                System.err.println("[GameStateMachine] Error notifying listener: " + e.getMessage());
            }
        }
        return true;
    }

    private boolean isValidTransition(GameState from, GameState to) {
        switch (from) {
            case BOOT:
                return to == GameState.LOADING || to == GameState.MAIN_MENU;
            case LOADING:
                return to == GameState.MAIN_MENU || to == GameState.PLAYING;
            case MAIN_MENU:
                return to == GameState.PLAYING || to == GameState.GARAGE || to == GameState.ENV_SELECT || to == GameState.SETTINGS || to == GameState.LOADING;
            case GARAGE:
                return to == GameState.MAIN_MENU || to == GameState.ENV_SELECT || to == GameState.PLAYING || to == GameState.SETTINGS;
            case ENV_SELECT:
                return to == GameState.GARAGE || to == GameState.MAIN_MENU || to == GameState.PLAYING;
            case PLAYING:
                return to == GameState.PAUSED || to == GameState.GAME_OVER || to == GameState.MAIN_MENU || to == GameState.COUNTDOWN || to == GameState.SETTINGS;
            case PAUSED:
                return to == GameState.PLAYING || to == GameState.MAIN_MENU || to == GameState.SETTINGS || to == GameState.GAME_OVER;
            case GAME_OVER:
                return to == GameState.PLAYING || to == GameState.MAIN_MENU || to == GameState.GARAGE || to == GameState.ENV_SELECT || to == GameState.LOADING;
            case SETTINGS:
                return to == GameState.MAIN_MENU || to == GameState.PAUSED || to == GameState.GARAGE;
            case COUNTDOWN:
                return to == GameState.PLAYING || to == GameState.PAUSED;
            case RESPAWNING:
            case MISSION_COMPLETE:
                return to == GameState.PLAYING || to == GameState.MAIN_MENU;
            default:
                return true;
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public GameState getPreviousState() {
        return previousState;
    }

    public void addListener(StateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }
}
