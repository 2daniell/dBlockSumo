package com.daniel.blocksumo.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MatchSpawns {

    private List<Location> locations;

    public MatchSpawns() {
        this.locations = new ArrayList<>();
    }

    public String serializeLocations() {
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append(location.getWorld().getName()).append(",").append(location.getX()).append(",").append(location.getY()).append(",").append(location.getZ()).append(";");
        }
        return sb.toString();
    }

    public static List<Location> deserializeLocations(String serializedLocations) {
        List<Location> loc = new ArrayList<>();
        String[] locationStrings = serializedLocations.split(";");
        for (String locationString : locationStrings) {
            String[] parts = locationString.split(",");
            if (parts.length == 4) {
                String worldName = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                Location location = new Location(org.bukkit.Bukkit.getWorld(worldName), x, y, z);
                loc.add(location);
            }
        }
        return loc;
    }
}
