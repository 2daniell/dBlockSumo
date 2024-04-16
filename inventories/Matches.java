package com.daniel.blocksumo.inventories;

import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.Menu;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Match> matches = manager.findMatchReady().stream().sorted(Comparator
                .comparing(Match::getPlayersSize).reversed()).collect(Collectors.toList());

        if (!e.getCurrentItem().hasItemMeta()) return;
        if (!e.getCurrentItem().getItemMeta().hasDisplayName()) return;
        if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aJogar")) {
            Match match = matches.getFirst();
            if (match.getPlayersSize() >= MinigameConfig.MAX_PLAYERS) return;
            match.joinPlayer(player);
            player.closeInventory();
        }

    }

    @Override
    public void setItens(Inventory inventory) {
        List<Match> matches = manager.findMatchReady();

        if(player.hasPermission("blocksumo.vip") || player.hasPermission("blocksumo.admin")) {

            inventory.setItem(12, new ItemBuilder(351, (matches.isEmpty()) ? (short) 8 : 10)
                    .setDisplayName((matches.isEmpty()) ? "§7Não encontramos nenhuma partida." : "§aJogar").build());

            inventory.setItem(14, new ItemBuilder(Material.BOOK).setDisplayName("§aPartidas").build());

            inventory.setItem(31, new ItemBuilder(Material.BARRIER).setDisplayName("§cSair").build());
        } else {

            inventory.setItem(13, new ItemBuilder(351, (matches.isEmpty()) ? (short) 8 : 10)
                    .setDisplayName((matches.isEmpty()) ? "§7Não encontramos nenhuma partida." : "§aJogar").build());

            inventory.setItem(31, new ItemBuilder(Material.BARRIER).setDisplayName("§cSair").build());
        }
    }
}
