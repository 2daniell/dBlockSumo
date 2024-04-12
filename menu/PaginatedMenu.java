package com.daniel.blocksumo.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public abstract class PaginatedMenu extends Menu {

    protected int page = 1;
    protected final int maxItensPerPage;

    public PaginatedMenu(Player player, String name, int size, int maxItensPerPage) {
        super(player, name, size);
        this.maxItensPerPage = maxItensPerPage;
    }

    public <T> List<T> subList(List<T> itens) {
        int inicial;
        int ultimo = ((inicial = (page - 1) * maxItensPerPage) + maxItensPerPage);

        if (ultimo > itens.size()) {
            ultimo = itens.size();
        }

        return itens.subList(inicial, ultimo);
    }

    public abstract void onClick(InventoryClickEvent e);

    public abstract void setItens(Inventory inventory);

}
