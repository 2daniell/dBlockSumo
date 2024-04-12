package com.daniel.blocksumo.command;

import com.daniel.blocksumo.inventories.Matches;
import com.daniel.blocksumo.manager.MatchManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockSumoCommand implements CommandExecutor {

    private final MatchManager manager;

    public BlockSumoCommand(MatchManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cPara executar esse comando você precisa ser um player");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.openInventory(new Matches(player, manager).getInventory());
        }

        return false;
    }
}
