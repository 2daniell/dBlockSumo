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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaCommand implements CommandExecutor {

    private final Map<UUID, Location> arenaPos1 = new HashMap<>();
    private final Map<UUID, Location> arenaPos2 = new HashMap<>();
    private final Map<UUID, Location> waitSpawn = new HashMap<>();
    private final Map<UUID, Location> waitAreaPos1 = new HashMap<>();
    private final Map<UUID, Location> waitAreaPos2 = new HashMap<>();
    private final Map<UUID, Location> gold = new HashMap<>();

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

            if (manager.hasMatch()) {
                player.sendMessage(new String[]{
                        " ",
                        "§6Criar arena: §f/arena create <nome>",
                        "§6Definir posições: §f/arena pos1|pos2",
                        "§6Definir area de espera: §f/arena <nome> waitspawn",
                        "§6Definir posições da arena de espera: §f/arena wait pos1|pos2",
                        "§6Definir pontos de nascimentos dos players: §f/arena <nome> playerspawn"
                });
            } else {
                String[] msg = new String[]{
                        " ",
                        "§6Para criar uma arena será necessario seguir uma serie de comandos.",
                        "§7Obs: Caso a configuração da sua arena não seja concluida, a mesma não sera listada para jogo.",
                        " ",
                        "§6Criar arena: §f/arena create <nome>",
                        "§6Definir posições: §f/arena pos1|pos2",
                        "§6Definir area de espera: §f/arena <nome> waitspawn",
                        "§6Definir posições da arena de espera: §f/arena wait pos1|pos2",
                        "§6Definir pontos de nascimentos dos players: §f/arena <nome> playerspawn"
                };
                player.sendMessage(msg);
                return true;
            }
        }

        if (args.length == 2) {
            String arenaName = args[1];
            switch (args[0].toLowerCase()) {
                case "create" -> {
                    if (arenaPos1.containsKey(player.getUniqueId()) && arenaPos2.containsKey(player.getUniqueId()) &&
                            waitSpawn.containsKey(player.getUniqueId()) && waitAreaPos1.containsKey(player.getUniqueId())
                            && waitAreaPos2.containsKey(player.getUniqueId()) && gold.containsKey(player.getUniqueId())) {
                        if (!manager.hasWithName(arenaName)) {


                            Location pos1 = arenaPos1.get(player.getUniqueId());
                            Location pos2 = arenaPos2.get(player.getUniqueId());

                            if (!pos1.getWorld().equals(pos2.getWorld())) {
                                player.sendMessage("§cAs posiçoes não podem ser setadas em mundos diferentes");
                                arenaPos2.remove(player.getUniqueId());
                                arenaPos1.remove(player.getUniqueId());
                                return true;
                            }

                            Location wait = waitSpawn.get(player.getUniqueId());

                            if (!wait.getWorld().equals(pos1.getWorld())) {
                                player.sendMessage("§cAs posiçoes não podem ser setadas em mundos diferentes");
                                waitSpawn.remove(player.getUniqueId());
                                return true;
                            }

                            Location waiPos1 = waitAreaPos1.get(player.getUniqueId());
                            Location waiPos2 = waitAreaPos2.get(player.getUniqueId());

                            if (!waiPos1.getWorld().equals(pos2.getWorld()) && !waiPos1.getWorld().equals(wait.getWorld())) {
                                player.sendMessage("§cAs posiçoes não podem ser setadas em mundos diferentes");
                                waitAreaPos2.remove(player.getUniqueId());
                                waitAreaPos1.remove(player.getUniqueId());
                                return true;
                            }

                            Location b = gold.get(player.getUniqueId());

                            if (!waiPos2.getWorld().equals(b.getWorld())) {
                                player.sendMessage("§cAs posiçoes não podem ser setadas em mundos diferentes");
                                gold.remove(player.getUniqueId());
                                return true;
                            }

                            Match match = new Match(arenaName, pos1, pos2);
                            match.setGoldBlock(b);
                            if (match.getPlayerWaitingArea().getPos2() != null) {
                                match.getPlayerWaitingArea().setPos1(waitAreaPos1.get(player.getUniqueId()));
                            } else {
                                match.getPlayerWaitingArea().setPos1(waitAreaPos1.get(player.getUniqueId()));
                            }

                            if (match.getPlayerWaitingArea().getPos1() != null) {
                                match.getPlayerWaitingArea().setPos2(waitAreaPos2.get(player.getUniqueId()));
                            } else {
                                match.getPlayerWaitingArea().setPos2(waitAreaPos2.get(player.getUniqueId()));
                            }
                            match.getPlayerWaitingArea().setWorld(waitAreaPos2.get(player.getUniqueId()).getWorld());
                            match.setSpawnWaiting(wait);
                            manager.create(match);
                            arenaPos2.remove(player.getUniqueId());
                            arenaPos1.remove(player.getUniqueId());
                            waitSpawn.remove(player.getUniqueId());
                            gold.remove(player.getUniqueId());
                            waitAreaPos2.remove(player.getUniqueId());
                            waitAreaPos1.remove(player.getUniqueId());
                            player.sendMessage("§aA arena §f" + arenaName + " §afoi criada com sucesso");
                            return true;
                        } else {
                            player.sendMessage("§cA arena ja existe");
                            return true;
                        }
                    } else {
                        player.sendMessage("§cPrimeiro faça a configuração basica da arena.");
                        return true;
                    }
                }
                case "playerspawn" -> {
                    if (!manager.hasWithName(arenaName)) {
                        player.sendMessage("§cA arena não existe");
                        return true;
                    }
                    Match match = manager.findByName(arenaName);
                    if (match != null) {
                        if (match.getMatchSpawns().getLocations().size() >= MinigameConfig.MAX_PLAYERS) {
                            player.sendMessage("§cO limite maximo foi atingido");
                            return true;
                        }
                        match.getMatchSpawns().getLocations().add(player.getLocation());
                        if (Main.config().getBoolean("Data.AutoSaveDB")) {
                            manager.saveToDatabase(match);
                        }
                        System.out.println(match.getMatchSpawns().getLocations().size());
                        player.sendMessage("§aPosição setada com sucesso");
                        return true;
                    }
                }
                default -> {
                    player.sendMessage("§cComando invalido.");
                }
            }
        }
        switch (args[0].toLowerCase()) {
            case "pos1" -> {
                if (arenaPos1.containsKey(player.getUniqueId())) {
                    arenaPos1.replace(player.getUniqueId(), player.getLocation());
                    player.sendMessage("§aPosição 1 da arena setada.");
                    return true;
                }
                arenaPos1.put(player.getUniqueId(), player.getLocation());
                player.sendMessage("§aPosição 1 da arena setada.");
            }
            case "pos2" -> {
                if (arenaPos2.containsKey(player.getUniqueId())) {
                    arenaPos2.replace(player.getUniqueId(), player.getLocation());
                    player.sendMessage("§aPosição 2 da arena setada.");
                    return true;
                }
                arenaPos2.put(player.getUniqueId(), player.getLocation());
                player.sendMessage("§aPosição 2 da arena setada.");
                return true;
            }
            case "waitspawn" -> {
                if (waitSpawn.containsKey(player.getUniqueId())) {
                    waitSpawn.replace(player.getUniqueId(), player.getLocation());
                } else {
                    waitSpawn.put(player.getUniqueId(), player.getLocation());
                }
                player.sendMessage("§aO spawn da area de espera foi setado");
                return true;

            } case "setobjective" -> {
                if (gold.containsKey(player.getUniqueId())) {
                    gold.replace(player.getUniqueId(), player.getLocation());
                } else {
                    gold.put(player.getUniqueId(), player.getLocation());
                }
                player.sendMessage("§aBloco de objetivo setado com sucesso");
                return true;
            }
            case "setlobby" -> {
                Main.saveLobby(player.getLocation());
                player.sendMessage("§aLobby setado com sucesso");
            }
            case "wait" -> {
                if (args.length >= 2) {
                    switch (args[1].toLowerCase()) {
                        case "pos1" -> {
                            if (waitAreaPos1.containsKey(player.getUniqueId())) {
                                waitAreaPos1.replace(player.getUniqueId(), player.getLocation());
                            } else {
                                waitAreaPos1.put(player.getUniqueId(), player.getLocation());
                            }
                            player.sendMessage("§aPosição da area de espera setada");
                            return true;
                        }
                        case "pos2" -> {
                            if (waitAreaPos2.containsKey(player.getUniqueId())) {
                                waitAreaPos2.replace(player.getUniqueId(), player.getLocation());
                            } else {
                                waitAreaPos2.put(player.getUniqueId(), player.getLocation());
                            }
                            player.sendMessage("§aPosição da area de espera setada");
                            return true;
                        }
                        default -> {
                            player.sendMessage("§cComando invalido.");
                        }
                    }
                }
            }

        }
        return false;
    }
}
