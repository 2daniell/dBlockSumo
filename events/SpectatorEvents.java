package com.daniel.blocksumo.events;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.inventories.Matches;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.objects.enums.MatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class SpectatorEvents implements Listener {

    private final MatchManager manager;

    public SpectatorEvents(MatchManager manager) {
        this.manager = manager;
    }

    @EventHandler //bater em player
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getState() == MatchState.STARTED) {
            if (match.getSpectators().contains(player.getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler //interact spec itens // SPEC
    public void onInteractItens(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (!match.getSpectators().contains(player.getUniqueId())) return;

        ItemStack item = e.getItem();

        if(!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.equalsIgnoreCase("§eVoltar ao Lobby")) {
            match.getSpectators().remove(player.getUniqueId());
            player.teleport(Main.lobby);
        } else if (name.equalsIgnoreCase("§eJogar Novamente")) {
            player.openInventory(new Matches(player, manager).getInventory());
        }

    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if(match == null) return;
        if (match.getState() == MatchState.STARTED) {
            if (match.getSpectators().contains(player.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSpecChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Match match = manager.findMatchByPlayer(player);
        if (match == null) return;
        if (match.getState() == MatchState.STARTED) {
            if (match.getSpectators().contains(player.getUniqueId())) {
                player.sendMessage(Main.config().getString("Message.SpectatorChat").replace('&', '§'));
                e.setCancelled(true);
            }
        }
    }
}
