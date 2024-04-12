package com.daniel.blocksumo.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {

    protected final Player player;
    private final Inventory inventory;

    public Menu(Player player, String name, int size){
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, name);
    }

    public abstract void onClick(InventoryClickEvent e);
    public abstract void setItens(Inventory inventory);

    @Override
    public Inventory getInventory() {
        setItens(inventory);
        return inventory;
    }

}
