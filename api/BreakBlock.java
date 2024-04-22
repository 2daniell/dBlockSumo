package com.daniel.blocksumo.api;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.enums.Version;
import com.daniel.blocksumo.model.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BreakBlock {

    private static final Set<Block> blocksBeingBroken = new HashSet<>();
    private static final AtomicInteger nextEntityId = new AtomicInteger(0);


    /*public static void playBlockBreakAnimation(Player player, Location location) {
        Block block = location.getBlock();

        if (blocksBeingBroken.contains(block)) {
            return;
        }
        blocksBeingBroken.add(block);

        int entityId = nextEntityId.incrementAndGet();
        int initialDestroyStage = 0;

        new BukkitRunnable() {
            int destroyStage = initialDestroyStage;

            @Override
            public void run() {
                if (block.getType().equals(Material.AIR)) {
                    blocksBeingBroken.remove(block);
                    cancel();
                    return;
                }

                if (destroyStage <= 9) {
                    sendBlockBreakPacket(player, location, entityId, destroyStage);
                    destroyStage++;
                } else {
                    sendBlockBreakPacket(player, location, entityId, 10);
                    block.setType(Material.AIR);
                    blocksBeingBroken.remove(block);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
    }*/

    private static final Map<UUID, AtomicInteger> entityIdMap = new HashMap<>();

    public static void playBlockBreakAnimation(List<GamePlayer> players, Location location) {
        Block block = location.getBlock();

        if (blocksBeingBroken.contains(block)) {
            return;
        }
        blocksBeingBroken.add(block);

        int initialDestroyStage = 0;

        new BukkitRunnable() {
            int destroyStage = initialDestroyStage;
            int entityId = nextEntityId.incrementAndGet();

            @Override
            public void run() {
                if (block.getType().equals(Material.AIR)) {
                    blocksBeingBroken.remove(block);
                    cancel();
                    return;
                }

                players.forEach(player -> {
                    sendBlockBreakPacket(player.getPlayer(), location, entityId, destroyStage);
                });

                if (destroyStage >= 10) {
                    block.setType(Material.AIR);
                    blocksBeingBroken.remove(block);
                    cancel();
                }
                destroyStage++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
    }



    private static AtomicInteger getNextEntityId(UUID playerId) {
        return entityIdMap.computeIfAbsent(playerId, k -> new AtomicInteger(0));
    }

    private static void sendBlockBreakPacket(Player player, Location location, int entityId, int destroyStage) {
        try {
            Class<?> packetClass = ReflectionUtils.getNMSClass("PacketPlayOutBlockBreakAnimation");

            Constructor<?> constructor = packetClass.getConstructor(int.class, ReflectionUtils.getNMSClass("BlockPosition"), int.class);

            Class<?> blockPositionClass = ReflectionUtils.getNMSClass("BlockPosition");

            Constructor<?> blockPositionConstructor = blockPositionClass.getConstructor(double.class, double.class, double.class);

            Object blockPosition = blockPositionConstructor.newInstance(location.getX(), location.getY(), location.getZ());

            Object packet = constructor.newInstance(entityId, blockPosition, destroyStage);

            ReflectionUtils.sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
