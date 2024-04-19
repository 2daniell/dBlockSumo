package com.daniel.blocksumo.model;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.api.TitleAPI;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.MatchSpawns;
import com.daniel.blocksumo.objects.PlayerWaitingArea;
import com.daniel.blocksumo.objects.enums.GamePlayerColor;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class Match extends MinigameConfig {

    private UUID id;
    private final String name;
    private World world;
    private Location spawnWaiting;
    private MatchSpawns matchSpawns;
    private Location goldBlock;
    private transient PlayerWaitingArea playerWaitingArea;
    private transient List<GamePlayer> players;
    private transient final List<GamePlayerColor> avaliables = new ArrayList<>(Arrays.asList(GamePlayerColor.values()));
    private transient List<UUID> spectator;
    private transient List<UUID> waiting;
    private transient BukkitRunnable runnable;
    private transient MatchState state;
    private transient WorldGenerator generator;


    public Match(String name, Location spawnWaiting, PlayerWaitingArea area) {
        this.name = name;
        this.spawnWaiting = spawnWaiting;
        this.state = MatchState.WAITING;
        this.players = new ArrayList<>();
        this.spectator = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.playerWaitingArea = area;
        this.generator = new WorldGenerator(name);
        this.matchSpawns = new MatchSpawns();
        generator.loadArena();
    }

    public Match(String name, Location arenaPos1, Location arenaPos2) {
        this.name = name;
        this.players = new ArrayList<>();
        this.spectator = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.matchSpawns = new MatchSpawns();
        this.playerWaitingArea = new PlayerWaitingArea();
        this.state = MatchState.WAITING;
        this.generator = new WorldGenerator(arenaPos1, arenaPos2, name);
        this.world = arenaPos1.getWorld();
        this.id = UUID.randomUUID();
        if(!generator.hasBlocksForWorld(world)) {
            save();
        }
    }

    public void run() {
        if (state.equals(MatchState.WAITING) && waiting.size() >= MIN_PLAYERS) {

            runnable = new BukkitRunnable() {

                int countdown = START_MIN_PLAYERS_COOLDOWN;

                @Override
                public void run() {
                    if (waiting.size() == MAX_PLAYERS) countdown = START_MAX_PLAYERS_COOLDOWN;

                    if (countdown <= 5) {
                        waiting.forEach(e -> {
                            Player player = Bukkit.getPlayer(e);
                            TitleAPI.sendTitle(player, 20, 20, 20, Main.config().getString("Titles.Starting.Title")
                                    .replace('&', '§' ).replaceAll("%count%", String.valueOf(countdown)), Main.config().getString("Titles.Starting.Subtitle")
                                    .replace('&', '§' ).replaceAll("%count%", String.valueOf(countdown)));
                        });
                    }

                    if(countdown == 0) {
                        if (waiting.size() >= MIN_PLAYERS) {
                            cancel();
                            state = MatchState.STARTING;

                            players = new ArrayList<>();

                            waiting.forEach(e -> {
                                Player player = Bukkit.getPlayer(e);
                                if (player != null) {
                                    var gamePlayer = new GamePlayer(player.getUniqueId(), player.getName());
                                    int index = new Random().nextInt(avaliables.size());
                                    gamePlayer.setColor(avaliables.remove(index));
                                    players.add(gamePlayer);
                                }
                            });

                            waiting.clear();

                            List<Location> locs = matchSpawns.getLocations();

                            Collections.shuffle(locs);

                            for(int i = 0; i <= players.size(); i++) {
                                if (i == players.size()) continue;
                                GamePlayer gamePlayer = players.get(i);
                                Player player = gamePlayer.getPlayer();
                                setupItens(player);
                                player.teleport(locs.get(i));
                            }
                            getPlayerWaitingArea().setToAir();
                        }

                    }

                    countdown--;

                }
            };
            runnable.runTaskTimer(Main.getPlugin(Main.class), 0, 20);

        }
    }

    public List<Location> getNearbyBlocks(int radius) {
        List<Location> blocks = new ArrayList<>();
        for(int x = goldBlock.getBlockX() - radius; x <= goldBlock.getBlockX() + radius; x++) {
            for(int y = goldBlock.getBlockY() - radius; y <= goldBlock.getBlockY() + radius; y++) {
                for(int z = goldBlock.getBlockZ() - radius; z <= goldBlock.getBlockZ() + radius; z++) {
                    Block block = goldBlock.getWorld().getBlockAt(x, y, z);
                    if (!generator.isOriginalBlock(block) && !block.getLocation().equals(goldBlock)) {
                        if (block.getType() != Material.TNT) {
                            blocks.add(block.getLocation());
                        }
                    }
                }
            }
        }
        return blocks;
    }


    public void joinPlayer(Player player) {
        if (state.equals(MatchState.WAITING) && players.size() < MAX_PLAYERS) {
            waiting.add(player.getUniqueId());
            setupItens(player);
            player.teleport(spawnWaiting);
            List<UUID> copy = new ArrayList<>(waiting);
            copy.forEach(e -> Bukkit.getPlayer(e).sendMessage(Main.config().getString("Message.JoinGame").replace('&', '§' ).replaceAll("%count%", String.valueOf(waiting.size())).replaceAll("%player%", player.getName()).replaceAll("%max%", String.valueOf(MAX_PLAYERS))));
            run();
        }
    }

    public void dead(GamePlayer gamePlayer) {
        if (!state.equals(MatchState.STARTED)) return;
        if (gamePlayer.getLifes() > 1) {
            double lifes = gamePlayer.getLifes()-1;
            gamePlayer.setLifes(lifes);
            Player player = gamePlayer.getPlayer();
            players.forEach(gm -> {
                Player target = gm.getPlayer();
                target.hidePlayer(player);
            });
            player.teleport(spawnWaiting);
            player.setAllowFlight(true);
            player.setFlying(true);
            new BukkitRunnable() {

                int countdown = RESPAWN_TIME;

                @Override
                public void run() {
                    if (countdown == 0) {
                        respawn(gamePlayer);
                        cancel();
                    }
                    TitleAPI.sendTitle(player, 20, 20, 20, Main.config().getString("Titles.Death.Title")
                            .replace('&', '§').replaceAll("%count%", String.valueOf(countdown)), Main.config().getString("Titles.Death.Subtitle")
                            .replace('&', '§').replaceAll("%count%", String.valueOf(countdown)));
                    countdown--;
                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
        } else {
            System.out.println("MORREU OTARO");
        }
    }

    private void respawn(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        Location location = matchSpawns.getLocations().get(new Random().nextInt(matchSpawns.getLocations().size()-1));
        player.teleport(location);

        player.setMaxHealth(gamePlayer.getLifes()*2);
        player.setHealth(gamePlayer.getLifes()*2);

        player.setFlying(false);
        player.setAllowFlight(false);
        setupItens(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                TitleAPI.sendTitle(player, 20, 20, 20, "§a§lVOCE NASCEU FDP", "A");
            }
        }.runTaskLater(Main.getPlugin(Main.class), 20);
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
            } case STARTING -> {
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

    public void setupItens(Player player) {
        switch (state) {
            case RELOADING -> {

            } case STARTING, STARTED -> {
                player.getInventory().clear();
                player.getInventory().setItem(0, new ItemBuilder(Material.SHEARS).build());
                player.getInventory().setItem(1, new ItemStack(Material.WOOL, 64));

                GamePlayer gamePlayer = findByUUID(player.getUniqueId());

                ItemBuilder helmet = new ItemBuilder(Material.LEATHER_HELMET);
                ItemBuilder chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE);
                ItemBuilder leggins = new ItemBuilder(Material.LEATHER_LEGGINGS);
                ItemBuilder boots = new ItemBuilder(Material.LEATHER_BOOTS);

                switch (gamePlayer.getColor()) {
                    case RED -> {
                        helmet.setColor(Color.RED);
                        chestplate.setColor(Color.RED);
                        leggins.setColor(Color.RED);
                        boots.setColor(Color.RED);
                    } case BLUE -> {
                        helmet.setColor(Color.BLUE);
                        chestplate.setColor(Color.BLUE);
                        leggins.setColor(Color.BLUE);
                        boots.setColor(Color.BLUE);
                    } case BLACK -> {
                        helmet.setColor(Color.BLACK);
                        chestplate.setColor(Color.BLACK);
                        leggins.setColor(Color.BLACK);
                        boots.setColor(Color.BLACK);
                    } case PURPLE -> {
                        helmet.setColor(Color.PURPLE);
                        chestplate.setColor(Color.PURPLE);
                        leggins.setColor(Color.PURPLE);
                        boots.setColor(Color.PURPLE);
                    } case GREEN -> {
                        helmet.setColor(Color.GREEN);
                        chestplate.setColor(Color.GREEN);
                        leggins.setColor(Color.GREEN);
                        boots.setColor(Color.GREEN);
                    } case ORANGE -> {
                        helmet.setColor(Color.GREEN);
                        chestplate.setColor(Color.GREEN);
                        leggins.setColor(Color.GREEN);
                        boots.setColor(Color.GREEN);
                    } case WHITE -> {
                        helmet.setColor(Color.WHITE);
                        chestplate.setColor(Color.WHITE);
                        leggins.setColor(Color.WHITE);
                        boots.setColor(Color.WHITE);
                    } case YELLOW -> {
                        helmet.setColor(Color.YELLOW);
                        chestplate.setColor(Color.YELLOW);
                        leggins.setColor(Color.YELLOW);
                        boots.setColor(Color.YELLOW);
                    }
                }

                player.getInventory().setHelmet(helmet.build());
                player.getInventory().setChestplate(chestplate.build());
                player.getInventory().setLeggings(leggins.build());
                player.getInventory().setBoots(boots.build());
            } case WAITING -> {
                player.getInventory().clear();
                player.getInventory().setItem(8, new ItemBuilder(Material.BED).setDisplayName("§cVoltar ao Lobby").build());
            }
        }
    }

    private void save() {
        generator.saveBlocksInArena(world);
    }

    public void reset() {
        generator.resetWorld(world);
    }

    public int getPlayersSize() {
        return players.size();
    }

    public GamePlayer findByUUID(UUID id) {
        return players.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }
}
