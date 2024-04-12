package com.daniel.blocksumo.inventories;

import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.Menu;
import com.daniel.blocksumo.model.Match;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class Matches extends Menu {

    private Player player;
    private final MatchManager manager;

    public Matches(Player player, MatchManager manager) {
        super(player, "Partidas", 4*9);
        this.player = player;
        this.manager = manager;
    }

    @Override
    public void onClick(InventoryClickEvent e) {

    }

    @Override
    public void setItens(Inventory inventory) {
        if (player.hasPermission("blocksumo.vip")){
            inventory.setItem(31, new ItemBuilder(Material.BARRIER).build());
        } else {
            List<Match> matches = manager.findAllByState();
            inventory.setItem(13, new ItemBuilder(351, (matches.isEmpty()) ? (short)8 : (short)10 ).build());
            inventory.setItem(31, new ItemBuilder(Material.BARRIER).build());
        }
    }
}
