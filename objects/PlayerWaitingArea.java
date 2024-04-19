package com.daniel.blocksumo.objects;

import com.daniel.blocksumo.api.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PlayerWaitingArea {

    private World world;
    private Location pos1;
    private Location pos2;

    public void setToAir() {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.getType().equals(Material.AIR)) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public static String serialize(PlayerWaitingArea playerWaitingArea) {
        String serializedLocation1 = Utils.getSerializedLocation(playerWaitingArea.getPos1());
        String serializedLocation2 = Utils.getSerializedLocation(playerWaitingArea.getPos2());
        return playerWaitingArea.getWorld().getUID() + "/" + serializedLocation1 + "/" + serializedLocation2;
    }

    public static PlayerWaitingArea deserialize(String str) {
        System.out.println(str);
        String[] parts = str.split("/");
        World world = Bukkit.getServer().getWorld(UUID.fromString(parts[0]));
        Location pos1 = Utils.getDeserializedLocation(parts[1]);
        Location pos2 = Utils.getDeserializedLocation(parts[2]);
        return new PlayerWaitingArea(world, pos1, pos2);
    }

}
