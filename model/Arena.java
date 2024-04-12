package com.daniel.blocksumo.model;

import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

public class Arena {

    @Getter
    private String name;

    @Getter
    private World world;

    private int startX, startY, startZ;
    private int endX, endY, endZ;

    public Arena(String name, World world, Location pos1, Location pos2) {
        this.name = name;
        this.world = world;
        this.startX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.startY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.startZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.endX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.endY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.endZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        save();

    }

    public Match match() {
        return new Match(this);
    }

    public void save() {
        WorldGenerator.saveWorld(world, startX, startY, startZ, endX, endY, endZ);
    }

    public void reset() {
        WorldGenerator.resetWorld(world);
    }
}