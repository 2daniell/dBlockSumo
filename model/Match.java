package com.daniel.blocksumo.model;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Match extends MinigameConfig {

    @Getter
    private String name;
    @Getter
    private World world;
    @Getter
    private Location spawn;
    @Getter
    private transient List<Player> players;
    private transient Timer timer;
    @Getter
    private transient MatchState state;
    private transient WorldGenerator generator;
    private transient int startX, startY, startZ;
    private transient int endX, endY, endZ;

    public Match(String name, WorldGenerator generator, Location spawn, Location pos1, Location pos2) {
        this.generator = generator;
        this.name = name;
        this.world = spawn.getWorld();
        this.spawn = spawn;
        this.startX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.startY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.startZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.endX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.endY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.endZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        this.players = new ArrayList<>();
        this.state = MatchState.WAITING;
        if(!generator.hasBlocksForWorld(world)) {
            save();
        }

    }

    public void run() {
        if (players.size() >= MIN_PLAYERS) {
            //codigo aqui
        }
    }

    public void addPlayer(Player player) {
        if (state.equals(MatchState.WAITING) && players.size() < MAX_PLAYERS) {
            players.add(player);
            players.forEach(e -> e.sendMessage(Main.config().getString("Message.JoinGame").replace('&', '§' ).replaceAll("%count%", String.valueOf(players.size())).replaceAll("%player%", player.getName()).replaceAll("%max%", String.valueOf(MAX_PLAYERS))));
        }
    }

    private void save() {
        generator.saveWorld(world, name, startX, startY, startZ, endX, endY, endZ);
    }

    public void reset() {
        generator.resetWorld(world);
    }

    public static void loadArenas(WorldGenerator generator) {
        generator.loadAllArenas();
    }

    public static void resetAll(WorldGenerator worldGenerator) {
        for (World w : Bukkit.getWorlds()) {
            worldGenerator.resetWorld(w);
        }
    }
}
