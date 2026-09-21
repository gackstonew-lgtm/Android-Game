package com.gackstone.chase;

import com.gackstone.chase.core.GameState;
import com.gackstone.chase.core.GameStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateMachineTest {

    private GameStateMachine stateMachine;

    @BeforeEach
    public void setUp() {
        stateMachine = new GameStateMachine();
    }

    @Test
    public void testInitialStateIsBoot() {
        assertEquals(GameState.BOOT, stateMachine.getCurrentState());
    }

    @Test
    public void testValidStateTransitions() {
        assertTrue(stateMachine.transitionTo(GameState.MAIN_MENU));
        assertEquals(GameState.MAIN_MENU, stateMachine.getCurrentState());
        assertEquals(GameState.BOOT, stateMachine.getPreviousState());

        assertTrue(stateMachine.transitionTo(GameState.PLAYING));
        assertEquals(GameState.PLAYING, stateMachine.getCurrentState());

        assertTrue(stateMachine.transitionTo(GameState.PAUSED));
        assertEquals(GameState.PAUSED, stateMachine.getCurrentState());

        assertTrue(stateMachine.transitionTo(GameState.PLAYING));
        assertEquals(GameState.PLAYING, stateMachine.getCurrentState());

        assertTrue(stateMachine.transitionTo(GameState.GAME_OVER));
        assertEquals(GameState.GAME_OVER, stateMachine.getCurrentState());

        assertTrue(stateMachine.transitionTo(GameState.MAIN_MENU));
        assertEquals(GameState.MAIN_MENU, stateMachine.getCurrentState());
    }

    @Test
    public void testInvalidTransitionsRejected() {
        // BOOT cannot go directly to PAUSED or GAME_OVER
        assertFalse(stateMachine.transitionTo(GameState.PAUSED));
        assertEquals(GameState.BOOT, stateMachine.getCurrentState());

        assertFalse(stateMachine.transitionTo(GameState.GAME_OVER));
        assertEquals(GameState.BOOT, stateMachine.getCurrentState());
    }

    @Test
    public void testListenerNotifications() {
        AtomicInteger transitionsCount = new AtomicInteger(0);
        stateMachine.addListener((prev, next) -> transitionsCount.incrementAndGet());

        stateMachine.transitionTo(GameState.MAIN_MENU);
        stateMachine.transitionTo(GameState.PLAYING);
        stateMachine.transitionTo(GameState.PAUSED);

        assertEquals(3, transitionsCount.get());
    }
}
