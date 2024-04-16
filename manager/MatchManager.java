package com.daniel.blocksumo.manager;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.Utils;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.PlayerWaitingArea;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.storage.Cache;
import com.daniel.blocksumo.storage.Database;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.crypto.Mac;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchManager extends Cache<Match> {

    private void saveToDatabase(Match match) {
        try (Connection con = Database.open()) {
            // Inserção ou atualização da partida na tabela matches
            final String matchQuery = "INSERT INTO matches (id, name, world, spawn_waiting, spawn_area) VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), world = VALUES(world), spawn_waiting = VALUES(spawn_waiting), spawn_area = VALUES(spawn_area)";
            try (PreparedStatement matchStm = con.prepareStatement(matchQuery)) {
                matchStm.setString(1, match.getId().toString());
                matchStm.setString(2, match.getName());
                matchStm.setString(3, match.getWorld().getUID().toString());
                matchStm.setString(4, Utils.getSerializedLocation(match.getSpawnWaiting()));
                matchStm.setString(5, PlayerWaitingArea.serialize(match.getPlayerWaitingArea()));
                matchStm.executeUpdate();
            }

            // Inserção dos spawns na tabela spawns
            final String spawnQuery = "INSERT INTO spawns (match_id, location) VALUES (?, ?)";
            try (PreparedStatement spawnStm = con.prepareStatement(spawnQuery)) {
                spawnStm.setString(1, match.getId().toString());
                for (Location spawn : match.getSpawns()) {
                    spawnStm.setString(2, Utils.getSerializedLocation(spawn));
                    spawnStm.addBatch();
                }
                spawnStm.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveAll(List<Match> list, MatchManager manager) {
        list.forEach(manager::saveToDatabase);
    }

    private void load(String name) {
        Match match = null;
        try {
            Connection con = Database.open();

            PreparedStatement stmMatch = con.prepareStatement("SELECT * FROM matches WHERE name = ?");
            stmMatch.setString(1, name);
            ResultSet rsMatch = stmMatch.executeQuery();

            if (rsMatch.next()) {
                UUID id = UUID.fromString(rsMatch.getString("id"));
                String nm = rsMatch.getString("name");
                World world = Bukkit.getServer().getWorld(UUID.fromString(rsMatch.getString("world")));
                Location spawnWaiting = Utils.getDeserializedLocation(rsMatch.getString("spawn_waiting"));
                PlayerWaitingArea waitingArea = PlayerWaitingArea.deserialize(rsMatch.getString("spawn_area"));
                match = new Match(nm, spawnWaiting, waitingArea);
                if (world != null && Bukkit.getWorlds().contains(world)) {
                    match.setWorld(world);
                    System.out.println("SETADO");
                }
                match.setId(id);

                List<Location> locs = new ArrayList<>();
                PreparedStatement stmSpawns = con.prepareStatement("SELECT * FROM spawns WHERE match_id = ?");
                stmSpawns.setString(1, match.getId().toString());
                ResultSet rsSpawns = stmSpawns.executeQuery();
                while (rsSpawns.next()) {
                    Location loc = Utils.getDeserializedLocation(rsSpawns.getString("location"));
                    locs.add(loc);
                }

                match.setSpawns(locs);
                add(match);
            }

            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void loadAll(MatchManager manager) {
        try {
            Connection con = Database.open();

            PreparedStatement stmMatch = con.prepareStatement("SELECT * FROM matches");
            ResultSet rsMatch = stmMatch.executeQuery();

            while(rsMatch.next()) {
                UUID id = UUID.fromString(rsMatch.getString("id"));
                String nm = rsMatch.getString("name");
                World world = Bukkit.getServer().getWorld(UUID.fromString(rsMatch.getString("world")));
                Location spawnWaiting = Utils.getDeserializedLocation(rsMatch.getString("spawn_waiting"));
                PlayerWaitingArea waitingArea = PlayerWaitingArea.deserialize(rsMatch.getString("spawn_area"));
                Match match = new Match(nm, spawnWaiting, waitingArea);
                if (world != null && Bukkit.getWorlds().contains(world)) {
                    match.setWorld(world);
                    System.out.println("SETADO");
                }
                match.setId(id);
                List<Location> locs = new ArrayList<>();
                PreparedStatement stmSpawns = con.prepareStatement("SELECT * FROM spawns WHERE match_id = ?");
                stmSpawns.setString(1, match.getId().toString());
                ResultSet rsSpawns = stmSpawns.executeQuery();
                while (rsSpawns.next()) {
                    Location loc = Utils.getDeserializedLocation(rsSpawns.getString("location"));
                    locs.add(loc);
                }


                match.setSpawns(locs);
                manager.add(match);
            }

            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Match> findAll() {
        return getAll();
    }

    public List<Match> findMatchReady() {
        List<Match> list = new ArrayList<>();
        forEach(e -> {
            if (e.getState().equals(MatchState.WAITING)) {
                if(!e.getSpawns().isEmpty() && e.getSpawns().size() >= MinigameConfig.MIN_PLAYERS) {
                    list.add(e);
                }
            }
        });
        return list;
    }

    public Match findByName(String name) {
        return find(e -> e.getName().equalsIgnoreCase(name));
    }

    public boolean hasWithName(String name) {
        return stream().anyMatch(e -> e.getName().equalsIgnoreCase(name));
    }

    public Match findMatchByPlayer(Player player) {
        return stream().filter(e -> e.getPlayers().stream().anyMatch(gamePlayer -> gamePlayer.getId().equals(player.getUniqueId()))).findFirst()
                .orElse(stream().filter(e -> e.getWaiting().stream().anyMatch(id -> id.equals(player.getUniqueId()))).findFirst()
                        .orElse(stream().filter(e -> e.getSpectator().stream().anyMatch(id -> id.equals(player.getUniqueId()))).findFirst()
                                .orElse(null)));
    }

    public void create(Match match) {
        add(match);
        if (Main.config().getBoolean("Data.AutoSaveDB")) {
            saveToDatabase(match);
        }
    }

    public boolean remove(Match match) {
        return super.remove(match);
    }
}
