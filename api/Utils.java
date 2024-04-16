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
        String [] parts = s.split(";");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        UUID u = UUID.fromString(parts[3]);
        World w = Bukkit.getServer().getWorld(u);
        return new Location(w, x, y, z);
    }
}
