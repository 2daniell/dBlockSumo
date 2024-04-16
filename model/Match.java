package com.daniel.blocksumo.model;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.PlayerWaitingArea;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Match extends MinigameConfig {


    //fazer sobrecarga e iniciar
    @Getter
    @Setter
    private UUID id;
    @Getter
    private final String name;
    @Getter
    @Setter
    private World world;
    @Getter
    private final Location spawnWaiting;
    @Setter
    @Getter
    private List<Location> spawns;
    @Getter
    private transient PlayerWaitingArea playerWaitingArea;
    @Getter
    private transient List<GamePlayer> players;
    @Getter
    private transient List<UUID> spectator;
    @Getter
    private transient List<UUID> waiting;
    private transient Timer timer;
    @Getter
    private transient MatchState state;
    private transient WorldGenerator generator;


    public Match(String name, Location spawnWaiting, PlayerWaitingArea area) {
        this.name = name;
        this.spawnWaiting = spawnWaiting;
        this.state = MatchState.WAITING;
        this.spawns = new ArrayList<>();
        this.players = new ArrayList<>();
        this.spectator = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.playerWaitingArea = area;
        this.generator = new WorldGenerator(name);
        generator.loadArena();
    }

    public Match(String name, World world, Location spawnWaiting, Location pos1, Location pos2, PlayerWaitingArea waitingArea) {
        this(name, spawnWaiting, waitingArea);
        final int startX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int startY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int startZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int endX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int endY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int endZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        this.generator = new WorldGenerator(startX, startY, startZ, endX, endY, endZ, name);
        this.world = world;
        this.id = UUID.randomUUID();
        if(!generator.hasBlocksForWorld(world)) {
            save();
        }
    }

    public void run() {
        //code
    }

    public void joinPlayer(Player player) {
        if (state.equals(MatchState.WAITING) && players.size() < MAX_PLAYERS) {
            waiting.add(player.getUniqueId());
            player.getInventory().clear();
            player.teleport(spawnWaiting);
            player.getInventory().setItem(8, new ItemBuilder(Material.BED).setDisplayName("§cVoltar ao Lobby").build());
            List<UUID> copy = new ArrayList<>(waiting);
            copy.forEach(e -> Bukkit.getPlayer(e).sendMessage(Main.config().getString("Message.JoinGame").replace('&', '§' ).replaceAll("%count%", String.valueOf(waiting.size())).replaceAll("%player%", player.getName()).replaceAll("%max%", String.valueOf(MAX_PLAYERS))));
        }
    }

    public void quitPlayer(Player player) {
        switch (state){
            case state.WAITING -> {
                if (!waiting.contains(player.getUniqueId())) return;
                waiting.remove(player.getUniqueId());
                player.teleport(Main.lobby);
                player.getInventory().clear();
                final List<UUID> copy = new ArrayList<>(waiting);

                copy.forEach(e -> {

                    Player p = Bukkit.getPlayer(e);

                    p.sendMessage(Main.config().getString("Message.QuitGameStarted")
                            .replace('&', '§' )
                            .replaceAll("%count%", String.valueOf(waiting.size()))
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%max%", String.valueOf(MAX_PLAYERS)));

                });
            } case STARTED -> {
                if (!players.contains(findByUUID(player.getUniqueId()))) return;
                players.remove(findByUUID(player.getUniqueId()));
                player.getInventory().clear();
                player.teleport(Main.lobby);
                final List<GamePlayer> copy = new ArrayList<>(players);
                copy.forEach(e -> {

                    Player p = e.getPlayer();

                    p.sendMessage(Main.config().getString("Message.QuitGameStarted")
                            .replace('&', '§' )
                            .replaceAll("%count%", String.valueOf(players.size()))
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%max%", String.valueOf(MAX_PLAYERS)));

                });
            }

        }
    }

    private void save() {
        generator.saveBlocksInArena(world);
    }

    public void reset() {
        generator.resetWorld(world);
    }

    public static void resetAll(WorldGenerator generator) {
        for (World w : Bukkit.getWorlds()) {
            generator.resetWorld(w);
        }
    }

    public int getPlayersSize() {
        return players.size();
    }

    public GamePlayer findByUUID(UUID id) {
        return players.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }
}
