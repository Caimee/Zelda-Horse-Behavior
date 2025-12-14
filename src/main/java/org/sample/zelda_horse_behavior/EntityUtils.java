package org.sample.zelda_horse_behavior;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.sample.zelda_horse_behavior.HorseSystem.HorseState.MobState;
import org.sample.zelda_horse_behavior.PlayerSystem.PlayerState;

import java.util.List;

import static org.sample.zelda_horse_behavior.Zelda_horse_behavior.MobStates;
import static org.sample.zelda_horse_behavior.Zelda_horse_behavior.playerStates;

public class EntityUtils {
    public static List<? extends HorseEntity> getAllLoadedHorses(ServerWorld world) {
        return world.getEntitiesByType(EntityType.HORSE, e -> true);
    }

    // Retrieve or create the PlayerState for a given player
    public static PlayerState getPlayerState(PlayerEntity player) {
        return playerStates.computeIfAbsent(player, p -> new PlayerState());
    }

    // Retrieve or create the MobState for a given mob
    public static MobState getMobState(MobEntity mob) {
        return MobStates.computeIfAbsent(mob, s -> new MobState());
    }

    public static PlayerEntity getNearbyPlayer(ServerWorld world, MobEntity mob) {
        return world.getClosestPlayer(mob, 42.6);
    }


    public static boolean FOVcheck(LivingEntity animal, PlayerEntity player) {
        int angle = getPlayerState(player).ANGLE;
        Vec3d vec = player.getPos().subtract(animal.getPos()).normalize();// vector from animal to player
        Vec3d facing = Vec3d.fromPolar(0, animal.getHeadYaw()).normalize();// vector animal's head

        // calculate and compare the dot product
        double dot = facing.dotProduct(vec);
        return dot > Math.cos(Math.toRadians(angle * 0.5));// ANGLE is the whole FOV !
    }
}
