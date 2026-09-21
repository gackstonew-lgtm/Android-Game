package com.gackstone.chase;

import com.gackstone.chase.enemy.ChaseAIState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChaseAITest {

    @Test
    public void testChaseAIStateValues() {
        assertNotNull(ChaseAIState.valueOf("IDLE"));
        assertNotNull(ChaseAIState.valueOf("SEARCHING"));
        assertNotNull(ChaseAIState.valueOf("CHASING"));
        assertNotNull(ChaseAIState.valueOf("LOST_TARGET"));
        assertNotNull(ChaseAIState.valueOf("RECOVERING"));
        assertNotNull(ChaseAIState.valueOf("DISABLED"));
    }
}
