package com.daniel.blocksumo.events;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.ActionBarAPI;
import com.daniel.blocksumo.api.BreakBlock;
import com.daniel.blocksumo.api.TitleAPI;
import com.daniel.blocksumo.api.Utils;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.GamePlayer;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.enums.MatchState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MinigameEvents implements Listener {

    private final MatchManager manager;
    private transient boolean isTimerRunning = false;
    private final List<UUID> starting;
    private final List<UUID> dead;

    public MinigameEvents(MatchManager manager) {
        this.manager = manager;
        this.starting = new ArrayList<>();
        this.dead = new ArrayList<>();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (dead.contains(player.getUniqueId()) || match.getSpectators().contains(player.getUniqueId())) {
            e.setCancelled(true); return;
        }
        switch (match.getState()) {
            case WAITING -> {
                if (!match.getWaiting().contains(player.getUniqueId())) return;
                e.setCancelled(true);
            }
            case STARTING -> {
                if (!starting.contains(player.getUniqueId())) return;
                e.setCancelled(true);
            }
            case STARTED -> {
                if (!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
                if (dead.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                    return;
                }
                if (match.getGenerator().isOriginalBlock(e.getBlock())) {
                    e.setCancelled(true);
                    player.sendMessage(Main.config().getString("Message.BreakOriginalBlocks"));
                } else e.getBlock().getDrops().clear();
            }
        }
    }

    @EventHandler //colocar bloco
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (dead.contains(player.getUniqueId()) || match.getSpectators().contains(player.getUniqueId())) {
            e.setCancelled(true); return;
        }
        switch (match.getState()) {
            case WAITING -> {
                if (match.getWaiting().contains(player.getUniqueId())) e.setCancelled(true);
            }
            case STARTING -> {
                if (starting.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
            case STARTED -> {
                if (e.getBlockPlaced().getLocation().equals(match.getGoldBlock())) {
                    e.setCancelled(true);
                    return;
                }

                Block block = e.getBlockPlaced();
                DyeColor color = getRandomColor();

                if (block.getType() == Material.TNT) {
                    block.setType(Material.AIR);
                    var loc = block.getLocation();
                    loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                    return;
                }

                if (block.getType() == Material.WOOL) {
                    block.setType(Material.WOOL);
                    Wool wool = new Wool(color);
                    block.setData(wool.getData());
                }
                List<Location> blockList = match.getNearbyBlocks(4);
                blockList.forEach(b -> {
                    BreakBlock.playBlockBreakAnimation(match.getPlayers(), b);
                });
            }
        }
    }

    @EventHandler //on quit server
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match != null) {
            match.quitPlayer(player);
        }
    }

    //say in chat
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if(match == null) return;
        switch (match.getState()) {
            case WAITING -> {
                e.setCancelled(true);
                match.getWaiting().forEach(target -> {
                    Player t = Bukkit.getPlayer(target);
                    t.sendMessage((player.hasPermission("blocksumo.vip") ? "§f" : "§7")+player.getName() + " » " + e.getMessage());
                });
            } case STARTING, STARTED -> {
                e.setCancelled(true);
                if (match.getSpectators().contains(player.getUniqueId())) return;
                GamePlayer gm = match.findByUUID(player.getUniqueId());
                match.getPlayers().forEach(target -> {
                    Player t = target.getPlayer();
                    t.sendMessage(gm.getColor().getTag() + (player.hasPermission("blocksumo.vip") ? "§f" : "§7")+player.getName() + " » " + e.getMessage());
                });
            }
        }
    }

    @EventHandler //fall in void
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case STARTING -> {
                if (starting.contains(player.getUniqueId())) {
                    var to = e.getTo();
                    var from = e.getFrom();

                    if (from.getX() != to.getX() || from.getZ() != to.getZ()) e.setTo(e.getFrom());
                }
            }
            case STARTED -> {
                if (dead.contains(player.getUniqueId())) {
                    if (player.getLocation().getY() < 0) {
                        e.setTo(match.getSpawnWaiting());
                        return;
                    }
                }
                if (match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) {
                    if (player.getLocation().getY() < 0) {
                        GamePlayer gamePlayer = match.findByUUID(player.getUniqueId());
                        player.getInventory().clear();
                        if (gamePlayer.getLifes() == 1) {
                            match.dead(gamePlayer);
                            return;
                        }
                        setTimingDead(player.getUniqueId(), match);
                        return;
                    }
                    if (Utils.compare(player.getLocation(), match.getGoldBlock()) && !isTimerRunning) {
                        isTimerRunning = true;
                        new BukkitRunnable() {

                            int segunds = 0;

                            @Override
                            public void run() {
                                if (!Utils.compare(player.getLocation(), match.getGoldBlock())) {
                                    isTimerRunning = false;
                                    cancel();
                                    return;
                                }

                                if (segunds >= MinigameConfig.TIME_WIN) {
                                    match.goldBlockWin(match.findByUUID(player.getUniqueId()));
                                    cancel();
                                    isTimerRunning = false;
                                    return;
                                }
                                segunds++;
                            }
                        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTP(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getState() == MatchState.STARTING) {
            if (!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
            if (!starting.contains(player)) {
                starting.add(player.getUniqueId());

                new BukkitRunnable() {

                    int countdown = MinigameConfig.START_MATCH_TIME;

                    @Override
                    public void run() {

                        if (countdown <= 0) {
                            starting.remove(player.getUniqueId());

                            GamePlayer gamePlayer = match.findByUUID(player.getUniqueId());
                            player.setMaxHealth(gamePlayer.getLifes() * 2);
                            player.setHealth(gamePlayer.getLifes() * 2);

                            ActionBarAPI.sendActionBar(player, Main.config().getString("Message.StartedMatch")
                                    .replace('&', '§'));
                            match.setState(MatchState.STARTED);

                            match.startMatch();
                            cancel();
                            return;
                        }

                        ActionBarAPI.sendActionBar(player, Main.config().getString(
                                "Message.MatchStartCountdown"
                        ).replaceAll("%count%", String.valueOf(countdown)).replace('&', '§'));
                        countdown--;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0 ,20);
            }
        }
    }

    @EventHandler //Not move item in inventory
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        Inventory inventory = e.getClickedInventory();
        if (inventory == null || !(inventory.getHolder() instanceof Player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        switch (match.getState()) {
            case WAITING -> {
                if (!match.getWaiting().contains(player.getUniqueId())) return;
                if (inventory.getHolder().equals(player)) e.setCancelled(true);
            }
            case STARTING -> {
                if (!starting.contains(player.getUniqueId())) return;
                if (inventory.getHolder().equals(player)) e.setCancelled(true);
            }
            case STARTED -> {
                if (!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
                if (inventory.getHolder().equals(player)) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        var match = manager.findMatchByPlayer(player);
        if(match == null) return;
        switch (match.getState()) {
            case WAITING -> {
                if (!match.getWaiting().contains(player.getUniqueId())) return;
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK ||
                        e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                    e.setCancelled(true);
                }
            } case STARTING -> {
                if (!starting.contains(player.getUniqueId())) return;
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK ||
                        e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                        e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                    e.setCancelled(true);
                }
            } case STARTED -> {
                e.setDamage(0);
            }
        }
    }

    @EventHandler //hit on dead
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case WAITING -> {
                if (match.getWaiting().contains(player.getUniqueId())) e.setCancelled(true);
            } case STARTING -> {
                if (starting.contains(player.getUniqueId())) e.setCancelled(true);
            }
        }
    }

    @EventHandler //Not drop itens in waiting area
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case WAITING -> {
                if (match.getWaiting().contains(player.getUniqueId())) e.setCancelled(true);
            }
            case STARTING -> {
                if (starting.contains(player.getUniqueId())) e.setCancelled(true);
            }
            case STARTED -> {
                if (match.getPlayers().contains(match.findByUUID(player.getUniqueId())) || match.getSpectators()
                        .contains(player.getUniqueId())) e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        var match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getSpectators().contains(player.getUniqueId())) return;
        switch (match.getState()) {
            case STARTED -> {
                if(player.hasPermission("blocksumo.admin")) return;
                List<String> cmdConfig = Main.config().getStringList("CommandBlock.MatchStarted");
                if (!cmdConfig.contains(e.getMessage())) {
                    player.sendMessage(Main.config().getString("Message.UnavaliableCommandInMatch"));
                    e.setCancelled(true);
                }
            } case WAITING -> {
                if(player.hasPermission("blocksumo.admin")) return;
                List<String> cmdConfig = Main.config().getStringList("CommandBlock.MatchWaiting");
                if (!cmdConfig.contains(e.getMessage())) {
                    player.sendMessage(Main.config().getString("Message.UnavaliableCommandInMatch"));
                    e.setCancelled(true);
                }
            } case STARTING -> {
                if(player.hasPermission("blocksumo.admin")) return;
                List<String> cmdConfig = Main.config().getStringList("CommandBlock.MatchStarting");
                if (!cmdConfig.contains(e.getMessage())) {
                    player.sendMessage(Main.config().getString("Message.UnavaliableCommandInMatch"));
                    e.setCancelled(true);
                }
            }
        }
    }

    private DyeColor getRandomColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[new Random().nextInt(colors.length)];
    }

    private void setTimingDead(UUID uuid, Match match) {
        dead.add(uuid);
        match.dead(match.findByUUID(uuid));
        int time = MinigameConfig.RESPAWN_TIME;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), () -> {
            dead.remove(uuid);
        }, (long) 20 * time);
    }
}
