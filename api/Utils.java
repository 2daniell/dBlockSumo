package com.daniel.blocksumo.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Utils {

    public static String getSerializedLocation(Location loc) {
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getUID();
    }

    public static Location getDeserializedLocation(String s) {
        String[] parts = s.split(";");
        int x = (int) Double.parseDouble(parts[0]);
        int y = (int) Double.parseDouble(parts[1]);
        int z = (int) Double.parseDouble(parts[2]);
        UUID u = UUID.fromString(parts[3]);
        World w = Bukkit.getServer().getWorld(u);
        return new Location(w, x, y, z);
    }

    public static boolean compare(Location loc1, Location loc2) {
        int loc1X = (int) loc1.getX();
        int loc1Y = (int) loc1.getY();
        int loc1Z = (int) loc1.getZ();

        int loc2X = (int) loc2.getX();
        int loc2Y = (int) loc2.getY();
        int loc2Z = (int) loc2.getZ();

        return (loc1X == loc2X && loc1Y == loc2Y && loc1Z == loc2Z);
    }

}




