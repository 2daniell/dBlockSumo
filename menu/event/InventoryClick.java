package com.daniel.blocksumo.menu.event;

import com.daniel.blocksumo.menu.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClick implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof Menu)) return;
        e.setCancelled(true);
        if(e.getCurrentItem() == null) return;
        ((Menu ) holder).onClick(e);
    }
}
