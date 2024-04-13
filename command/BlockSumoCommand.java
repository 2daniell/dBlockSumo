package com.daniel.blocksumo.command;

import com.daniel.blocksumo.inventories.Matches;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockSumoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cPara executar esse comando você precisa ser um player");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
        }
        return false;
    }
}
