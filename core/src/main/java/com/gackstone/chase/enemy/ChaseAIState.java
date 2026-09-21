package com.gackstone.chase.enemy;

/**
 * AI state machine states for pursuer/chaser entities.
 */
public enum ChaseAIState {
    /** Chaser waiting or in standby */
    IDLE,
    
    /** Chaser scanning/searching for player target */
    SEARCHING,
    
    /** Active high-speed pursuit tracking player */
    CHASING,
    
    /** Player evaded sightline or outpaced chaser */
    LOST_TARGET,
    
    /** Temporary stabilization/recovery after obstacle glance or drift */
    RECOVERING,
    
    /** Chaser disabled / destroyed */
    DISABLED
}
