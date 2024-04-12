package com.daniel.blocksumo.command;

import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.Arena;
import com.daniel.blocksumo.model.Match;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private Location pos1;
    private Location pos2;

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
        if (args.length == 0) {
            player.sendMessage("§cUse um comando válido");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUse /arena create <nome>");
                return true;
            }

            if(pos1 != null && pos2 != null) {
                if (pos1.getWorld().equals(pos2.getWorld())) {
                    String name = StringUtils.join(args, " ", 1, args.length);
                    Arena arena = new Arena(name, pos1.getWorld(), pos1, pos2);
                    Match match = arena.match();
                    manager.add(match);
                    player.sendMessage("§aArena §f" + name + " §adefinida com sucesso");
                } else {
                    player.sendMessage("§cAs posições foram setadas em mundos diferentes.");
                }
            } else {
                player.sendMessage("§cDefina primeiramente as posições da arena.");
                player.sendMessage("§cUse, /arena pos1 e /arena pos2");
            }
            return true;

        } else if (args[0].equalsIgnoreCase("pos1")) {
            pos1 = player.getLocation();
            player.sendMessage("§aPosição 1 inserida com sucesso.");
            return true;
        } else if (args[0].equalsIgnoreCase("pos2")) {
            pos2 = player.getLocation();
            player.sendMessage("§aPosição 2 inserida com sucesso.");
            return true;
        } else if (args[0].equalsIgnoreCase("res")) {

            player.sendMessage("resetado");
            return true;
        } else {
            player.sendMessage("§cComando inválido. Use /arena pos1 | pos2");
            return true;
        }

        //return false
    }
}
