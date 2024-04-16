package com.daniel.blocksumo.events;

import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.GamePlayer;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.objects.enums.MatchState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MinigameEvents implements Listener {

    public final MatchManager manager;

    public MinigameEvents(MatchManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        //
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        //code
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        //
    }

    @EventHandler //Not move item in inventory
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getState().equals(MatchState.WAITING)) {
            if (!match.getWaiting().contains(player.getUniqueId())) return;
            Inventory inventory = e.getClickedInventory();
            if (inventory.getHolder().equals(player)) e.setCancelled(true);
        }
    }

    @EventHandler //Not drop itens in waiting area
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getState().equals(MatchState.WAITING) && match.getWaiting().contains(player.getUniqueId())) e.setCancelled(true);
    }

    @EventHandler //return to lobby
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        var match = manager.findMatchByPlayer(player);
        if (match == null) return;
        switch (match.getState()) {
            case STARTED -> {

            } case WAITING -> {
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
}
