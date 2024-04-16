package com.daniel.blocksumo.objects;

import com.daniel.blocksumo.api.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Setter
public class PlayerWaitingArea {

    private World world;
    private Location pos1;
    private Location pos2;

    public void setToAir() {
        for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
            for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
                for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
    }

    public static String serialize(PlayerWaitingArea playerWaitingArea) {
        String serializedLocation1 = Utils.getSerializedLocation(playerWaitingArea.getPos1());
        String serializedLocation2 = Utils.getSerializedLocation(playerWaitingArea.getPos2());
        return playerWaitingArea.getWorld().getUID() + "/" + serializedLocation1 + "/" + serializedLocation2;
    }

    // Desserializa uma string para um objeto PlayerWaitingArea
    public static PlayerWaitingArea deserialize(String str) {
        System.out.println(str);
        String[] parts = str.split("/");
        World world = Bukkit.getServer().getWorld(UUID.fromString(parts[0]));
        Location pos1 = Utils.getDeserializedLocation(parts[1]);
        Location pos2 = Utils.getDeserializedLocation(parts[2]);
        return new PlayerWaitingArea(world, pos1, pos2);
    }

}
