package org.sample.zelda_horse_behavior.PlayerSystem;

import net.minecraft.entity.player.PlayerEntity;
import org.sample.zelda_horse_behavior.LogicConfig;

import static org.sample.zelda_horse_behavior.LogicConfig.*;

// State machine for player
public class PlayerStateMachine {

    // logic of sneaking state machine
    public static void updateSneakingState(PlayerEntity player, PlayerState state) {
        //Enter sneaking state
        if (!state.isSneaking && player.isSneaking()) {
            state.isSneaking = true;
        }

        //Exit sneaking state
        if (state.isSneaking && !player.isSneaking()) {
            state.isSneaking = false;
        }
    }

    // execute player state effects
    public static void playerStateExecute(PlayerState state) {
        if (state.isSneaking) {
            state.detectionRange = SNEAK_RANGE;
            state.ANGLE = SNEAK_ANGLE;

        }
        if (!state.isSneaking) {
            state.detectionRange = DEFAULT_DETECTION_RANGE;
            state.ANGLE = DEFAULT_ANGLE;
        }
    }
}
