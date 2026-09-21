package com.gackstone.chase;

import com.gackstone.chase.core.GameConfig;
import com.gackstone.chase.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerMovementTest {

    private PlayerState playerState;

    @BeforeEach
    public void setUp() {
        playerState = new PlayerState();
    }

    @Test
    public void testInitialPlayerState() {
        assertEquals(GameConfig.PLAYER_INITIAL_HEALTH, playerState.getHealth());
        assertEquals(GameConfig.PLAYER_INITIAL_FORWARD_SPEED, playerState.getForwardSpeed());
        assertEquals(0.0f, playerState.getDistanceTraveled());
        assertTrue(playerState.isAlive());
        assertFalse(playerState.isInvulnerable());
    }

    @Test
    public void testDistanceAccumulation() {
        playerState.addDistance(10.5f);
        playerState.addDistance(14.5f);
        assertEquals(25.0f, playerState.getDistanceTraveled(), 0.001f);
    }

    @Test
    public void testDamageAndInvulnerability() {
        playerState.takeDamage(30.0f);
        assertEquals(70.0f, playerState.getHealth(), 0.001f);
        assertTrue(playerState.isInvulnerable());

        // While invulnerable, immediate damage is ignored
        playerState.takeDamage(20.0f);
        assertEquals(70.0f, playerState.getHealth(), 0.001f);

        // Update past invulnerability window (0.8s)
        playerState.update(0.9f);
        assertFalse(playerState.isInvulnerable());

        // Fatal damage
        playerState.takeDamage(100.0f);
        assertEquals(0.0f, playerState.getHealth());
        assertFalse(playerState.isAlive());
    }
}
