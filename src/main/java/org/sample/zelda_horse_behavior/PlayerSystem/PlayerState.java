package org.sample.zelda_horse_behavior.PlayerSystem;

import java.util.UUID;

import static org.sample.zelda_horse_behavior.LogicConfig.DEFAULT_DETECTION_RANGE;

// State of the player
public class PlayerState {
    public boolean isSneaking = false;
    public double detectionRange = DEFAULT_DETECTION_RANGE;
    public UUID forceMountedHorse;
    public long forceMountTick;
}

