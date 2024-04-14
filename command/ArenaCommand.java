package com.daniel.blocksumo.command;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.world.WorldGenerator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private Location pos1;
    private Location pos2;
    private Location spawn;
    private final MatchManager manager;

    public ArenaCommand(MatchManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cPara executar esse comando você precisa ser um player");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("blocksumo.admin")) {
            player.sendMessage(Main.config().getString("Message.NoPermission").replace('&', '§'));
            return true;
        }

        if (args.length == 0) {
            String[] msg = {
                    " ",
                    " "
            };
            player.sendMessage(msg);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUse /arena create <nome>");
                return true;
            }

            if (pos1 == null || pos2 == null || spawn == null) {
                player.sendMessage("§cDefina primeiramente as posições e o spawn da arena.");
                player.sendMessage("§cUse, /arena setspawn");
                player.sendMessage("§cUse, /arena pos1 e /arena pos2");
                return true;
            }

            if (!pos1.getWorld().equals(pos2.getWorld())) {
                player.sendMessage("§cAs posições foram setadas em mundos diferentes.");
                return true;
            }

            if (!pos1.getWorld().equals(spawn.getWorld())) {
                player.sendMessage("§cO spawn está em um mundo diferente das posições");
                return true;
            }

            String name = StringUtils.join(args, " ", 1, args.length);
            Match match = new Match(name, spawn, pos1, pos2);
            manager.create(match);
            player.sendMessage("§aA arena §f" + name + " §afoi definida com sucesso");
            return true;

        } else if (args[0].equalsIgnoreCase("setspawn")) {
            this.spawn = player.getLocation();
            player.sendMessage("§aO spawn foi setado com sucesso");
            return true;
        } else if (args[0].equalsIgnoreCase("pos1")) {
            this.pos1 = player.getLocation();
            player.sendMessage("§aA posição 1 foi definida com sucesso!");
            return true;
        } else if (args[0].equalsIgnoreCase("pos2")) {
            this.pos2 = player.getLocation();
            player.sendMessage("§aA posição 2 foi definida com sucesso!");
            return true;
        } else {
            player.sendMessage("§cComando desconhecido.");
        }
        return false;
    }
}
