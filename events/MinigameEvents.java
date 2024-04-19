package com.daniel.blocksumo.events;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.BreakBlock;
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
        if (dead.contains(player.getUniqueId())) e.setCancelled(true);
        switch (match.getState()) {
            case WAITING -> {
                if (!match.getWaiting().contains(player.getUniqueId())) return;
                e.setCancelled(true);
            } case STARTING -> {
                if (!starting.contains(player.getUniqueId())) return;
                e.setCancelled(true);
            } case STARTED -> {
                if(!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
                if(match.getGenerator().isOriginalBlock(e.getBlock())) {
                    e.setCancelled(true);
                    player.sendMessage("§4§lERRO §cVocê so pode quebrar blocos colocados por players");
                } else e.getBlock().getDrops().clear();
            }
        }
    }

    @EventHandler //colocar bloco
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (dead.contains(player.getUniqueId())) e.setCancelled(true);
        switch (match.getState()) {
            case WAITING -> {
                if(match.getWaiting().contains(player.getUniqueId())) e.setCancelled(true);
            } case STARTING -> {
                if (starting.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                }
            } case STARTED -> {
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
                match.getPlayers().forEach(gamePlayer -> {
                        Player p = gamePlayer.getPlayer();
                        blockList.forEach(b -> {
                            BreakBlock.playBlockBreakAnimation(p, b);
                        });
                });

            }
        }
    }

    @EventHandler //tnt explosion
    public void onExplosion(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        Match match = manager.findMatchInGameByWorld(entity.getWorld());
        if (match == null) return;
        if (entity.getType() == EntityType.PRIMED_TNT) {
            e.setCancelled(true);

            Location locationExplosion = entity.getLocation();

            for(Entity nearby : locationExplosion.getWorld().getNearbyEntities(locationExplosion, 5 ,5, 5)) {
                if (nearby != entity) {
                    Vector vector = nearby.getLocation().toVector().subtract(locationExplosion.toVector()).normalize().multiply(3);
                    nearby.setVelocity(vector);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    locationExplosion.getBlock().setType(Material.AIR);
                }
            }.runTaskLater(Main.getPlugin(Main.class), 2);
        }
    }

    @EventHandler //on quit server
    public void onQuit(PlayerQuitEvent e) {
        //
    }

    //say in chat
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        //code
    }

    @EventHandler //fall in void
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case STARTING -> {
                if(starting.contains(player.getUniqueId())) {
                    var to = e.getTo();
                    var from = e.getFrom();

                    if(from.getX() != to.getX() || from.getZ() != to.getZ()) e.setTo(e.getFrom());
                }
            } case STARTED -> {
                if (dead.contains(player.getUniqueId())) {
                    if (player.getLocation().getY() < 0) {
                        e.setTo(match.getSpawnWaiting());
                        return;
                    }
                }
                if (match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) {
                    if(player.getLocation().getY() < 0) {
                        GamePlayer gamePlayer = match.findByUUID(player.getUniqueId());
                        player.getInventory().clear();
                        match.dead(gamePlayer);
                        dead.add(player.getUniqueId());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case STARTED -> {

                //

            } case STARTING -> {
                if (!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
                if (starting.contains(player.getUniqueId())) return;
                int time = MinigameConfig.START_MATCH_TIME;
                player.sendMessage("§aA partida ira iniciar em §a" + time + " §asegundos");
                starting.add(player.getUniqueId());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        starting.remove(player.getUniqueId());
                        player.sendMessage(new String[] {
                                " ", "§aA partida iniciou", " "
                        });
                        match.setState(MatchState.STARTED);

                    }
                }.runTaskLater(Main.getPlugin(Main.class), (long)20*time);
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
            } case STARTING -> {
                if (!starting.contains(player.getUniqueId())) return;
                if (inventory.getHolder().equals(player)) e.setCancelled(true);
            } case STARTED -> {
                if(!match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) return;
                if (inventory.getHolder().equals(player)) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK || e.getCause() == EntityDamageEvent.DamageCause.FALL)) return;
        Player player = (Player) e.getEntity();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case WAITING -> {
                if (match.getWaiting().contains(player.getUniqueId())) {
                    e.setCancelled(true);
                }
            } case STARTING -> {
                if(starting.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                }
            } case STARTED -> {
                if (match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) {
                    e.setCancelled(true);
                }
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
            } case STARTING -> {
                if (starting.contains(player.getUniqueId())) e.setCancelled(true);
            } case STARTED -> {
                if (match.getPlayers().contains(match.findByUUID(player.getUniqueId()))) e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        var match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case STARTED -> {
                List<String > cmdConfig = Main.config().getStringList("CommandBlock.MatchStarted");
                if (!cmdConfig.contains(e.getMessage())) {
                    player.sendMessage("§4§lERRO §cVocê não pode executar esse comando em uma partida");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler //return to lobby
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        var match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
             case WAITING -> {
                if (!match.getWaiting().contains(player.getUniqueId())) return;

                if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

                ItemStack item = e.getItem();
                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

                if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cVoltar ao Lobby")) {
                    player.sendMessage("§aVoltando ao lobby");
                    match.quitPlayer(player);
                }
            }
        }
    }

    private DyeColor getRandomColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[new Random().nextInt(colors.length)];
    }
}
