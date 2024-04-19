package com.daniel.blocksumo.inventories;

import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.Menu;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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
        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cSair")) player.closeInventory();

    }

    @Override
    public void setItens(Inventory inventory) {
        List<Match> matches = manager.findMatchReady();

        inventory.setItem(0, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(1, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(9, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());

        inventory.setItem(17, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(7, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());

        inventory.setItem(28, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(27, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(18, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());

        inventory.setItem(26, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(34, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());
        inventory.setItem(35, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 2).setDisplayName(" ").build());

        inventory.setItem(12, new ItemBuilder(Material.WOOL).addEnchant(Enchantment.KNOCKBACK, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setDisplayName((matches.isEmpty()) ? "§7Não encontramos nenhuma partida." : "§aJogar").build());

        inventory.setItem(14, new ItemBuilder(Material.BOOK).setDisplayName("§aPartidas").addEnchant(Enchantment.KNOCKBACK, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS).build());

        inventory.setItem(31, new ItemBuilder(Material.BARRIER).setDisplayName("§cSair").build());

        inventory.setItem(31, new ItemBuilder(Material.BARRIER).setDisplayName("§cSair").build());
    }
}
