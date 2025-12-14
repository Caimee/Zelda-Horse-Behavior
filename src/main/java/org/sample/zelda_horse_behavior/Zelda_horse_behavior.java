package org.sample.zelda_horse_behavior;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import org.sample.zelda_horse_behavior.HorseSystem.HorseState.MobState;
import org.sample.zelda_horse_behavior.PlayerSystem.PlayerState;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static org.sample.zelda_horse_behavior.HorseSystem.HorseState.State.DEFAULT_EMPTY;
import static org.sample.zelda_horse_behavior.ProcessHorseAI.processHorseAI;

public class Zelda_horse_behavior implements ModInitializer {
    public static final String MOD_ID = "FleeOnSight";
    public static WeakHashMap<MobEntity, MobState> MobStates = new WeakHashMap<>();// Store mob states with weak references
    public static WeakHashMap<PlayerEntity, PlayerState> playerStates = new WeakHashMap<>();// Store player states with weak references
    public static final Map<UUID, Long> dismountLockUntil = new HashMap<>();
    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        // Handle forced mounting in dismount lock period
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.getTicks();

            for (ServerWorld world : server.getWorlds()) {
                for (PlayerEntity player : world.getPlayers()) {

                    Long until = dismountLockUntil.get(player.getUuid());
                    if (until == null) continue;

                    if (tick >= until) {
                        dismountLockUntil.remove(player.getUuid());
                        continue;
                    }

                    if (!player.hasVehicle()) {
                        PlayerState ps = Zelda_horse_behavior.playerStates.get(player);
                        if (ps != null && ps.forceMountedHorse != null) {
                            Entity last = world.getEntity(ps.forceMountedHorse);
                            if (last instanceof HorseEntity horse && horse.isAlive()) {
                                horse.setEatingGrass(false);
                                horse.setAngry(false);
                                player.setYaw(horse.getYaw());
                                player.setPitch(horse.getPitch());
                                player.startRiding(horse);
                            }
                        }
                    }
                }
            }
        });

        System.out.println("FleeOnSight Mod initialized!");

        // Prevent interaction with fleeing horses
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof HorseEntity horse) {
                MobState horseState = EntityUtils.getMobState(horse);
                if (horseState.currentState ==
                        org.sample.zelda_horse_behavior.HorseSystem.HorseState.State.FLEEING) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });


        //Cover Vanilla tame interaction to implement forced mounting
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (world.isClient) {
                return ActionResult.PASS;
            }

            if (!(entity instanceof HorseEntity horse)) {
                return ActionResult.PASS;
            }

            MobState horseState = EntityUtils.getMobState(horse);
            if (horseState.currentState != DEFAULT_EMPTY) {
                return ActionResult.PASS;
            }

            if (horse.isTame() || horse.isBaby()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) {
                return ActionResult.FAIL;
            }

            if (stack.getItem() != Items.SADDLE){
                return ActionResult.FAIL;
            }


            PlayerState ps = Zelda_horse_behavior.playerStates
                    .computeIfAbsent(player, p -> new PlayerState());

            ps.forceMountedHorse = horse.getUuid();
            ps.forceMountTick = world.getTime();

            ((ServerWorld) world).getServer().execute(() -> {
                if (!player.hasVehicle() && horse.isAlive()) {

                    if (player.isSneaking()) {
                        player.setSneaking(false);
                    }

                    horse.bondWithPlayer(player);

                    horse.saddle(SoundCategory.NEUTRAL);

                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    horse.setEatingGrass(false);
                    horse.setAngry(false);
                        player.setYaw(horse.getYaw());
                        player.setPitch(horse.getPitch());
                        player.startRiding(horse);
                    long unlockTick = world.getServer().getTicks() + 22;
                    dismountLockUntil.put(player.getUuid(), unlockTick);
                }
            });
            return ActionResult.CONSUME;
        });
    }

    private void onWorldTick(ServerWorld world) {
        processHorseAI(world);
    }
}
