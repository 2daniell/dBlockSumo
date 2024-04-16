package com.daniel.blocksumo.command;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.PlayerWaitingArea;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private Location pos1;
    private Location pos2;
    private Location spawnWaiting;
    private Location posWaitArea1;
    private Location posWaitArea2;
    private final MatchManager manager;

    public ArenaCommand(MatchManager manager) {
        this.manager = manager;
    }

    @Override
    @SneakyThrows
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
                    "§cPara criar uma arena, será necessario que você use uma sequencia de comandos.",
                    " ",
                    "§f/arena pos1 §7- §cUse para setar a posição da primeira extremidade da arena.",
                    "§f/arena pos2 §7- §cUse para setar a posição da segunda extremidade da arena.",
                    " ",
                    "§f/arena wait pos1 §7- §cUse para setar a posição da primeira extremidade da area de espera.",
                    "§f/arena wait pos2 §7- §cUse para setar a posição da segunda extremidade da area de espera.",
                    "§f/arena wait setspawn §7- §cUse para definir onde os players vão nascer na area de espera.",
                    " ",
                    "§f/arena create <name> §7- §cUse para criar a arena, apos configurar todas as posições.",
                    " ",
                    "§f/arena setlobby §f7- §cUse para definir o lobby principal do seu servidor. So é necessario usar esse comando uma vez."

            };
            player.sendMessage(msg);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUse /arena create <nome>");
                return true;
            }

            if (pos1 == null || pos2 == null || spawnWaiting == null || posWaitArea2 == null || posWaitArea1 == null) {
                String[] msg = {
                        " ",
                        "§cPara criar uma arena, será necessario que você use uma sequencia de comandos.",
                        " ",
                        "§f/arena pos1 §7- §cUse para setar a posição da primeira extremidade da arena.",
                        "§f/arena pos2 §7- §cUse para setar a posição da segunda extremidade da arena.",
                        " ",
                        "§f/arena wait pos1 §7- §cUse para setar a posição da primeira extremidade da area de espera.",
                        "§f/arena wait pos2 §7- §cUse para setar a posição da segunda extremidade da area de espera.",
                        "§f/arena wait setspawn §7- §cUse para definir onde os players vão nascer na area de espera.",
                        " ",
                        "§f/arena create <name> §7- §cUse para criar a arena, apos configurar todas as posições.",
                        "§f/arena <nome> setspawn §7- §cDefine o local de spawn dos players ao iniciar a partida" ,
                        " ",
                        "§f/arena setlobby §f7- §cUse para definir o lobby principal do seu servidor. So é necessario usar esse comando uma vez."

                };
                player.sendMessage(msg);
                return true;
            }

            if (!pos1.getWorld().equals(pos2.getWorld())) {
                player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                return true;
            }

            if (!posWaitArea1.getWorld().equals(posWaitArea2.getWorld())) {
                player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                return true;
            }

            if (!posWaitArea1.getWorld().equals(pos1.getWorld())) {
                player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                return true;
            }

            if (!pos1.getWorld().equals(spawnWaiting.getWorld())) {
                player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                return true;
            }

            String name = StringUtils.join(args, " ", 1, args.length);

            if (manager.hasWithName(name)) {
                player.sendMessage("§cJá possui uma arena com esse nome.");
                return true;
            }

            Match match = new Match(name, pos1.getWorld(), spawnWaiting, pos1, pos2, new PlayerWaitingArea(posWaitArea1.getWorld(), posWaitArea1, posWaitArea2));
            manager.create(match);
            player.sendMessage("§aA arena §f" + name + " §afoi criada com sucesso.");
            return true;

        } else if (args[0].equalsIgnoreCase("pos1")) {
            if (pos2 != null) {
                if (!pos2.getWorld().equals(player.getWorld())) {
                    player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                    return true;
                }
            }

            pos1 = player.getLocation();
            player.sendMessage("§aPosição 1 da arena foi setada.");
            return true;
        } else if (args[0].equalsIgnoreCase("pos2")) {
            if (pos1 != null) {
                if (!pos1.getWorld().equals(player.getWorld())) {
                    player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                    return true;
                }

            }

            pos2 = player.getLocation();
            player.sendMessage("§aPosição 2 da arena foi setada.");
            return true;
        } else if (args[0].equalsIgnoreCase("setlobby")) {
            if (Main.lobby != null) {
                player.sendMessage("§aAlterando lobby.");
                Main.lobby = player.getLocation();
                Main.saveLobby(Main.lobby);
                return true;
            }
            Main.lobby = player.getLocation();
            Main.saveLobby(Main.lobby);
            player.sendMessage("§aLobby setado com sucesso.");
            return true;
        } else if (args[0].equalsIgnoreCase("wait")) {
            if (args.length < 2) {
                player.sendMessage("§cUse /arena wait pos1|pos2");
                return true;
            }

            if (args[1].equalsIgnoreCase("pos1")) {
                if (posWaitArea2 != null) {
                    if (!posWaitArea2.getWorld().equals(player.getWorld())) {
                        player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                        return true;
                    }
                }

                posWaitArea1 = player.getLocation();
                player.sendMessage("§aPosição 1 da arena de espera setada.");
                return true;
            } else if (args[1].equalsIgnoreCase("pos2")) {
                if (posWaitArea1 != null) {
                    if (!posWaitArea1.getWorld().equals(player.getWorld())) {
                        player.sendMessage("§cAs posições devem ser setadas no mesmo mundo.");
                        return true;
                    }
                }

                posWaitArea2 = player.getLocation();
                player.sendMessage("§aPosição 2 da arena de espera setada.");
                return true;
            } else if (args[1].equalsIgnoreCase("setspawn")) {
                spawnWaiting = player.getLocation();
                player.sendMessage("§aLocal de espera setado.");
                return true;
            } else {
                player.sendMessage("§cComando desconhecido.");
            }

        }else if(args.length >= 2 && args[args.length - 1].equalsIgnoreCase("setspawn")) {

            String name = StringUtils.join(args, " ", 0, args.length - 1);

            Match match = manager.findByName(name);

            if (match == null) {
                player.sendMessage("§cA arena não existe");
                return true;
            }

            if (match.getSpawns().size() >= MinigameConfig.MAX_PLAYERS) {
                player.sendMessage("Uma partida suporta o maximo de 8 jogadores.");
                return true;
            }

            match.getSpawns().add(player.getLocation());
            player.sendMessage("§aVocê setou um spawn.");
            return true;
        } else {
            player.sendMessage("§cComando desconhecido.");
        }
        return false;
    }
}
