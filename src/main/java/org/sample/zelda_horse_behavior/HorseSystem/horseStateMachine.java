package org.sample.zelda_horse_behavior.HorseSystem;

import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.sample.zelda_horse_behavior.HorseSystem.HorseState.MobState;
import org.sample.zelda_horse_behavior.PlayerSystem.PlayerState;

import static org.sample.zelda_horse_behavior.EntityUtils.FOVcheck;
import static org.sample.zelda_horse_behavior.HorseSystem.HorseState.State.*;
import static org.sample.zelda_horse_behavior.LogicConfig.DEFAULT_DETECTION_RANGE;
import static org.sample.zelda_horse_behavior.LogicConfig.STOP_RANGE;

public class horseStateMachine {

    public static void updateStates(HorseEntity horse, PlayerEntity player, MobState mobState, PlayerState playerState){
        double distance = horse.distanceTo(player);
        switch (mobState.currentState){
            case DEFAULT_EMPTY:
                if(horse.isTame()){
                    mobState.currentState = FRIENDLY;
                }
                else if(distance <= playerState.detectionRange && FOVcheck(horse, player)){
                    mobState.currentState = FLEEING;
                }
                break;

            case FLEEING:
                if(distance >= STOP_RANGE){
                    mobState.currentState = DEFAULT_EMPTY;
                    horse.setAttacker(player);
                }
                break;
        }
    }
}
