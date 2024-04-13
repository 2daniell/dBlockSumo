package com.daniel.blocksumo.inventories;

import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.Menu;
import com.daniel.blocksumo.model.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class Matches extends Menu {

    private final MatchManager manager;
    private final Player player;

    public Matches(Player player, MatchManager manager) {
        super(player, "Partidas", 4*9);
        this.manager = manager;
        this.player = player;
    }

    @Override
    public void onClick(InventoryClickEvent e) {

    }

    @Override
    public void setItens(Inventory inventory) {

    }
}
