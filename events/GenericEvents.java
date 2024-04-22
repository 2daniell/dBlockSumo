package com.daniel.blocksumo.events;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GenericEvents implements Listener {

    private final MatchManager manager;

    public GenericEvents(MatchManager manager) {
        this.manager = manager;
    }

    @EventHandler //waiting
    public void onClickItens(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        Match match = manager.findMatchByPlayer(player);

        if (match == null) return;
        if (match.getSpectators().contains(player.getUniqueId())) return;
        ItemStack item = e.getItem();
        if (item == null) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.equalsIgnoreCase("§eVoltar ao Lobby")) {
            player.sendMessage(Main.config().getString("Message.ReturnToLobby").replace('&', '§'));
            match.quitPlayer(player);
        }
    }


}
