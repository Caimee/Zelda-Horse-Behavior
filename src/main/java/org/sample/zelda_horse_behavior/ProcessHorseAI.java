package org.sample.zelda_horse_behavior;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.sample.zelda_horse_behavior.AnimalGroupManager.AnimalGroupFleeManager;
import org.sample.zelda_horse_behavior.HorseSystem.HorseState.MobState;
import org.sample.zelda_horse_behavior.PlayerSystem.PlayerState;
import org.sample.zelda_horse_behavior.PlayerSystem.PlayerStateMachine;

import java.util.List;

import static org.sample.zelda_horse_behavior.EntityUtils.*;
import static org.sample.zelda_horse_behavior.HorseSystem.HorseState.State.FLEEING;
import static org.sample.zelda_horse_behavior.HorseSystem.horseStateMachine.updateStates;
import static org.sample.zelda_horse_behavior.LogicConfig.FLEE_SPEED;

public class ProcessHorseAI {
    public static void processHorseAI(ServerWorld world) {
        var group = EntityUtils.getAllLoadedHorses(world);
        processAI(world, group);
    }

    public static void processAI(ServerWorld world,  List<? extends HorseEntity> group) {
        if (group == null || group.isEmpty()) {
            return;
        }

        for(HorseEntity horse : group) {
            // Find the nearest player
            PlayerEntity player = getNearbyPlayer(world, horse);
            if (player == null || player.isCreative()) {
                continue;
            }

            PlayerState playerState = getPlayerState(player);
            MobState horseState = getMobState(horse);

            PlayerStateMachine.updateSneakingState(player, playerState);
            PlayerStateMachine.playerStateExecute(playerState);

            updateStates(horse, player, horseState, playerState);
            AnimalGroupFleeManager.manageGroupFlee(horse);

            if (horseState.currentState == FLEEING) {
                applyFlee_logic(horse, player);
            }
        }
    }

    public static void applyFlee_logic(MobEntity animal, PlayerEntity player) {
        Vec3d fromPlayer = animal.getPos().subtract(player.getPos()).normalize();// vector from player to animal
        Vec3d fleeDir = fromPlayer.multiply(26.5);// flee distance
        Vec3d targetPos = animal.getPos().add(fleeDir);// target position
        // initiate movement towards the target position at flee speed
        animal.getNavigation().startMovingTo(
                targetPos.x,
                targetPos.y,
                targetPos.z,
                FLEE_SPEED
        );
    }
}
