package com.daniel.blocksumo.inventories;

import com.daniel.blocksumo.Main;
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
        if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§e§lBlockSumo")) {
            Match match = matches.getFirst();
            if (match.getPlayersSize() >= MinigameConfig.MAX_PLAYERS) return;
            match.joinPlayer(player);
            player.closeInventory();
        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cSair")) player.closeInventory();

    }

    @Override
    public void setItens(Inventory inventory) {
        List<Match> matches = manager.findMatchReady();

        List<String> lore = Main.config().getStringList("Menu.BlockSumo.Lore");
        lore.replaceAll(e -> e.replace('&', '§'));

        inventory.setItem(12, new ItemBuilder(Material.WOOL).addEnchant(Enchantment.KNOCKBACK, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setDisplayName((matches.isEmpty()) ? "§7Não encontramos nenhuma partida." : "§e§lBlockSumo")
                .setLore(lore).build());

        if (player.hasPermission("blocksumo.vip")) {

            List<String> loreMap = Main.config().getStringList("Menu.EscolherMapa.PermLore");
            loreMap.replaceAll(e -> e.replace('&', '§'));

            inventory.setItem(14, new ItemBuilder(Material.BOOK).setDisplayName("§e§lEscolher Mapa").setLore(loreMap)
                    .addEnchant(Enchantment.KNOCKBACK, 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS).build());

        } else {

            List<String> loreNoPerm = Main.config().getStringList("Menu.EscolherMapa.NoPermLore");
            loreNoPerm.replaceAll(e -> e.replace('&', '§'));

            inventory.setItem(14, new ItemBuilder(Material.BOOK).setDisplayName("§e§lEscolher Mapa").addEnchant(Enchantment.KNOCKBACK, 1)
                            .setLore(loreNoPerm)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS).build());
        }
        inventory.setItem(31, new ItemBuilder(Material.BARRIER).setDisplayName("§cSair").build());
    }
}
