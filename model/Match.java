package com.daniel.blocksumo.model;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.api.ActionBarAPI;
import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.api.TitleAPI;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.MatchSpawns;
import com.daniel.blocksumo.objects.PlayerWaitingArea;
import com.daniel.blocksumo.objects.enums.GamePlayerColor;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Match extends MinigameConfig {

    private UUID id;
    private final String name;
    private World world;
    private Location spawnWaiting;
    private MatchSpawns matchSpawns;
    private Location goldBlock;
    private transient PlayerWaitingArea playerWaitingArea;
    private transient List<GamePlayer> players;
    private transient final List<GamePlayerColor> avaliables = new ArrayList<>(Arrays.asList(GamePlayerColor.values()));
    private transient List<UUID> waiting;
    private transient List<UUID> spectators;
    private transient MatchState state;
    private transient WorldGenerator generator;
    private transient Random random = new Random();

    private final Map<Player, Scoreboard> scoreboards = new HashMap<>();
    private boolean isStarted = false;

    public Match(String name, Location spawnWaiting, PlayerWaitingArea area) {
        this.name = name;
        this.spawnWaiting = spawnWaiting;
        this.state = MatchState.WAITING;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.playerWaitingArea = area;
        this.generator = new WorldGenerator(name);
        this.matchSpawns = new MatchSpawns();
        generator.loadArena();
    }

    public Match(String name, Location arenaPos1, Location arenaPos2) {
        this.name = name;
        this.players = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.matchSpawns = new MatchSpawns();
        this.playerWaitingArea = new PlayerWaitingArea();
        this.state = MatchState.WAITING;
        this.generator = new WorldGenerator(arenaPos1, arenaPos2, name);
        this.world = arenaPos1.getWorld();
        this.id = UUID.randomUUID();
        if(!generator.hasBlocksForWorld(world)) {
            save();
        }
    }

    public void run() {
        if (state.equals(MatchState.WAITING) && waiting.size() >= MIN_PLAYERS) {

            new BukkitRunnable() {

                int countdown = START_MIN_PLAYERS_COOLDOWN;

                @Override
                public void run() {
                    if (waiting.isEmpty() || waiting.size() < MIN_PLAYERS) {
                        cancel();
                        return;
                    }
                    if (waiting.size() == MAX_PLAYERS) countdown = START_MAX_PLAYERS_COOLDOWN;

                    if (countdown <= 5) {
                        waiting.forEach(e -> {
                            Player player = Bukkit.getPlayer(e);
                            TitleAPI.sendTitle(player, 20, 20, 20, Main.config().getString("Titles.Starting.Title")
                                    .replace('&', '§' ).replaceAll("%count%", String.valueOf(countdown)), Main.config().getString("Titles.Starting.Subtitle")
                                    .replace('&', '§' ).replaceAll("%count%", String.valueOf(countdown)));
                        });
                    }

                    if(countdown == 0) {
                        if (waiting.size() >= MIN_PLAYERS) {
                            cancel();
                            state = MatchState.STARTING;

                            waiting.forEach(e -> {
                                Player player = Bukkit.getPlayer(e);
                                if (player != null) {
                                    var gamePlayer = new GamePlayer(player.getUniqueId(), player.getName());
                                    int index = new Random().nextInt(avaliables.size());
                                    gamePlayer.setColor(avaliables.remove(index));
                                    players.add(gamePlayer);
                                }
                            });

                            waiting.clear();

                            List<Location> locs = matchSpawns.getLocations();

                            Collections.shuffle(locs);

                            for(int i = 0; i <= players.size(); i++) {
                                if (i == players.size()) continue;
                                GamePlayer gamePlayer = players.get(i);
                                Player player = gamePlayer.getPlayer();
                                setupItens(player);
                                player.teleport(locs.get(i));
                            }
                            getPlayerWaitingArea().setToAir();
                        }

                    }

                    countdown--;

                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);

        }
    }

    public List<Location> getNearbyBlocks(int radius) {
        List<Location> blocks = new ArrayList<>();
        for(int x = goldBlock.getBlockX() - radius; x <= goldBlock.getBlockX() + radius; x++) {
            for(int y = goldBlock.getBlockY() - radius; y <= goldBlock.getBlockY() + radius; y++) {
                for(int z = goldBlock.getBlockZ() - radius; z <= goldBlock.getBlockZ() + radius; z++) {
                    Block block = goldBlock.getWorld().getBlockAt(x, y, z);
                    if (!generator.isOriginalBlock(block) && !block.getLocation().equals(goldBlock)) {
                        if (block.getType() == Material.WOOL) {
                            blocks.add(block.getLocation());
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public void startMatch() {
        if (state == MatchState.STARTED && !isStarted) {
            isStarted = true;
            List<ItemStack> dropCenter = setupGoldBlockDropItens();
            List<ItemStack> giveToPlayers = setupPlayerRandomItens();

            new BukkitRunnable() {

            int dropCenterCountdown = 60;
            int givePlayerCountdown = 30;
                @Override
                public void run() {
                    if (state == MatchState.FINISHING) {
                        System.out.println("VERDADE");
                        cancel();
                        return;
                    }

                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < players.size(); i++) {
                        GamePlayer player = players.get(i);
                        builder.append(player.getDisplay());

                        if (i < players.size() - 1) {
                            builder.append(" §7| ");
                        }
                    }

                    players.forEach(e -> {
                        if (!e.isDead()) {
                            Player player = e.getPlayer();
                            if (player != null && player.isOnline()) {
                                ActionBarAPI.sendActionBar(player, builder.toString());
                            }
                        }
                    });

                    dropCenterCountdown--;
                    if (dropCenterCountdown <= 0) {
                        ItemStack drop = dropCenter.get(random.nextInt(dropCenter.size()));
                        goldBlock.getWorld().dropItem(goldBlock, drop);
                        dropCenterCountdown = 60;
                    }

                    givePlayerCountdown--;
                    if (givePlayerCountdown <= 0) {
                        for (GamePlayer player : players) {
                            if (!player.isDead()) {
                                Player p = player.getPlayer();
                                if (p != null && p.isOnline()) {
                                    ItemStack itemToGive = giveToPlayers.get(random.nextInt(giveToPlayers.size()));
                                    p.getInventory().addItem(itemToGive);
                                }
                            }
                        }
                        givePlayerCountdown = 30;
                    }
                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
        }
    }


    public void stopMatch() {

    }

    public void joinPlayer(Player player) {
        if (state.equals(MatchState.WAITING) && players.size() < MAX_PLAYERS) {
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
            waiting.add(player.getUniqueId());
            setupItens(player);
            player.teleport(spawnWaiting);
            List<UUID> copy = new ArrayList<>(waiting);
            copy.forEach(e -> Bukkit.getPlayer(e).sendMessage(Main.config().getString("Message.JoinGame").replace('&', '§' ).replaceAll("%count%", String.valueOf(waiting.size())).replaceAll("%player%", player.getName()).replaceAll("%max%", String.valueOf(MAX_PLAYERS))));
            run();
        }
    }

    public void dead(GamePlayer gamePlayer) {
        if (!state.equals(MatchState.STARTED)) return;
        if (gamePlayer.getLifes() > 1) {
            double lifes = gamePlayer.getLifes()-1;
            gamePlayer.setLifes(lifes);
            gamePlayer.setDead(true);
            Player player = gamePlayer.getPlayer();
            players.forEach(gm -> {
                Player target = gm.getPlayer();
                target.hidePlayer(player);
                target.sendMessage(Main.config().getString("Message.Death").replaceAll("%player%", player.getName())
                        .replace('&', '§'));
            });
            player.teleport(spawnWaiting);
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlying(true);
            new BukkitRunnable() {

                int countdown = RESPAWN_TIME;

                @Override
                public void run() {
                    if (countdown == 0) {
                        respawn(gamePlayer);
                        cancel();
                    }
                    TitleAPI.sendTitle(player, 20, 20, 20, Main.config().getString("Titles.Death.Title")
                            .replace('&', '§').replaceAll("%count%", String.valueOf(countdown)), Main.config().getString("Titles.Death.Subtitle")
                            .replace('&', '§').replaceAll("%count%", String.valueOf(countdown)));
                    countdown--;
                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
        } else {
            TitleAPI.sendTitle(gamePlayer.getPlayer(), 20, 20, 20, Main.config()
                    .getString("Title.FinalDeath.Title").replace('&', '§'), Main.config().getString(
                            "Title.FinalDeath.Subtitle"
            ).replace('&', '§'));
            setSpec(gamePlayer);
        }
    }

    private void setSpec(GamePlayer gamePlayer) {
        players.remove(gamePlayer);
        Player player = gamePlayer.getPlayer();
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        player.spigot().setCollidesWithEntities(false);

        players.forEach(e -> {
            Player p = e.getPlayer();
            p.hidePlayer(player);
            p.sendMessage(Main.config().getString("Message.FinalDeath").replaceAll("%player%", player.getName())
                    .replace('&', '§'));
        });

        spectators.add(player.getUniqueId());
        ending(player);
        player.teleport(spawnWaiting);
    }

    public void goldBlockWin(GamePlayer gamePlayer) {

        Player player = gamePlayer.getPlayer();

        List<String> msgWin = Main.config().getStringList("Message.WinGame");
        msgWin.replaceAll(msg -> msg.replace('&', '§').replaceAll("%player%", player.getName()));

        List<Player> all = sendPlayersWinMessage(msgWin.toArray(new String[0]));
        ending(all);
        state = MatchState.FINISHING;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), () -> {
            //manda geral pro lobby
        }, 20*15);

    }

    private void ending(List<Player> all) {
        all.forEach(this::ending);
    }

    private void ending(Player player) {

        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        player.setMaxHealth(20);
        player.setHealth(20);

        setupEndItens(player);

    }

    private List<Player> sendPlayersWinMessage(String... msg) {
        List<Player> all = players.stream().map(GamePlayer::getPlayer).collect(Collectors.toList());
        players.forEach(e -> {spectators.forEach(s -> {var spec = Bukkit.getPlayer(s);e.getPlayer().showPlayer(spec); spec.spigot().setCollidesWithEntities(true);});});
        spectators.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(all::add);
        all.forEach(e -> sendCenteredMessage(e, msg));
        return all;
    }

    private static void sendCenteredMessage(Player player, String... messages) {
        int screenWidth = 100;
        int maxLength = 0;

        for (String message : messages) {
            int length = ChatColor.stripColor(message).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }

        int padSize = (int) Math.floor((screenWidth - maxLength) / 2);
        String pad = StringUtils.repeat(" ", padSize);

        for (String message : messages) {
            player.sendMessage(pad + message);
        }
    }

    private void respawn(GamePlayer gamePlayer) {
        gamePlayer.setDead(false);
        Player player = gamePlayer.getPlayer();
        Location location = matchSpawns.getLocations().get(new Random().nextInt(matchSpawns.getLocations().size()-1));
        player.teleport(location);

        player.setMaxHealth(gamePlayer.getLifes()*2);
        player.setHealth(gamePlayer.getLifes()*2);


        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        setupItens(player);
        players.forEach(gm -> {
            Player target = gm.getPlayer();
            target.showPlayer(player);
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                TitleAPI.sendTitle(player, 20, 20, 20, Main.config().getString("Titles.Respawn.Title").replace('&', '§')
                        , Main.config().getString("Titles.Respawn.Title").replace('&', '§'));
            }
        }.runTaskLater(Main.getPlugin(Main.class), 20);
    }

    public void quitPlayer(Player player) {
        switch (state) {

            case WAITING -> {
                waiting.remove(player.getUniqueId());
                player.getInventory().clear();
                waiting.forEach(e -> {
                player.teleport(Main.lobby);
                    var p = Bukkit.getPlayer(e);
                    p.sendMessage(Main.config().getString("Message.QuitGameWaiting").replace('&', '§')
                            .replaceAll("%player%", player.getName()).replaceAll("%count%", String.valueOf(waiting.size()))
                            .replaceAll("%max%", String.valueOf(MinigameConfig.MAX_PLAYERS)));
                });
            } case STARTING, STARTED -> {
                players.remove(findByUUID(player.getUniqueId()));
                player.getInventory().clear();
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
                players.forEach(e -> {
                    var p = e.getPlayer();
                    p.sendMessage(Main.config().getString("Message.QuitGameStarted").replace('&', '§')
                            .replaceAll("%player%", player.getName()).replaceAll("%count%", String.valueOf(waiting.size()))
                            .replaceAll("%max%", String.valueOf(MinigameConfig.MAX_PLAYERS)));
                });
                if (players.isEmpty() || players.size() == 1) {
                    System.out.println("PARTIDA ACABOU");
                    state = MatchState.FINISHING;
                }
            }
        }
    }

    public void setupEndItens(Player player) {
        player.getInventory().setItem(3, new ItemBuilder(Material.NETHER_STAR).setDisplayName(
                "§eJogar Novamente"
        ).build());
        player.getInventory().setItem(5, new ItemBuilder(Material.BED)
                .setDisplayName("§eVoltar ao Lobby").build());
    }

    public void setupItens(Player player) {
        switch (state) {
            case STARTING, STARTED -> {
                player.getInventory().clear();
                player.getInventory().setItem(0, new ItemBuilder(Material.SHEARS).build());
                player.getInventory().setItem(1, new ItemStack(Material.WOOL, 64));

                GamePlayer gamePlayer = findByUUID(player.getUniqueId());

                ItemBuilder helmet = new ItemBuilder(Material.LEATHER_HELMET);
                ItemBuilder chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE);
                ItemBuilder leggins = new ItemBuilder(Material.LEATHER_LEGGINGS);
                ItemBuilder boots = new ItemBuilder(Material.LEATHER_BOOTS);

                switch (gamePlayer.getColor()) {
                    case RED -> {
                        helmet.setColor(Color.RED);
                        chestplate.setColor(Color.RED);
                        leggins.setColor(Color.RED);
                        boots.setColor(Color.RED);
                    } case BLUE -> {
                        helmet.setColor(Color.BLUE);
                        chestplate.setColor(Color.BLUE);
                        leggins.setColor(Color.BLUE);
                        boots.setColor(Color.BLUE);
                    } case BLACK -> {
                        helmet.setColor(Color.BLACK);
                        chestplate.setColor(Color.BLACK);
                        leggins.setColor(Color.BLACK);
                        boots.setColor(Color.BLACK);
                    } case PURPLE -> {
                        helmet.setColor(Color.PURPLE);
                        chestplate.setColor(Color.PURPLE);
                        leggins.setColor(Color.PURPLE);
                        boots.setColor(Color.PURPLE);
                    } case GREEN -> {
                        helmet.setColor(Color.GREEN);
                        chestplate.setColor(Color.GREEN);
                        leggins.setColor(Color.GREEN);
                        boots.setColor(Color.GREEN);
                    } case ORANGE -> {
                        helmet.setColor(Color.ORANGE);
                        chestplate.setColor(Color.ORANGE);
                        leggins.setColor(Color.ORANGE);
                        boots.setColor(Color.ORANGE);
                    } case WHITE -> {
                        helmet.setColor(Color.WHITE);
                        chestplate.setColor(Color.WHITE);
                        leggins.setColor(Color.WHITE);
                        boots.setColor(Color.WHITE);
                    } case YELLOW -> {
                        helmet.setColor(Color.YELLOW);
                        chestplate.setColor(Color.YELLOW);
                        leggins.setColor(Color.YELLOW);
                        boots.setColor(Color.YELLOW);
                    }
                }

                player.getInventory().setHelmet(helmet.build());
                player.getInventory().setChestplate(chestplate.build());
                player.getInventory().setLeggings(leggins.build());
                player.getInventory().setBoots(boots.build());
            } case WAITING -> {
                player.getInventory().clear();
                player.getInventory().setItem(8, new ItemBuilder(Material.BED).setDisplayName("§eVoltar ao Lobby").build());
            }
        }
    }

    private void save() {
        generator.saveBlocksInArena(world);
    }

    public void reset() {
        generator.resetWorld(world);
    }

    public int getPlayersSize() {
        return players.size();
    }

    public GamePlayer findByUUID(UUID id) {
        return players.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    public List<ItemStack> setupPlayerRandomItens() {
        List<ItemStack> itens = new ArrayList<>();

        itens.add(new ItemBuilder(Material.SNOW_BALL).setAmount(16).build());
        itens.add(new ItemBuilder(Material.FIREBALL).build());
        itens.add(new ItemBuilder(Material.WOOD_SWORD).addEnchant(Enchantment.KNOCKBACK, 2).build());
        itens.add(new ItemBuilder(Material.TNT).build());

        return itens;
    }

    public List<ItemStack> setupGoldBlockDropItens() {
        List<ItemStack> itens = new ArrayList<>();

        itens.add(new ItemBuilder(Material.ENDER_PEARL).build());
        itens.add(new ItemBuilder(Material.IRON_SWORD).addEnchant(Enchantment.KNOCKBACK, 3).build());
        ItemStack potion = new ItemStack(Material.POTION);
        var m = (PotionMeta) potion.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 20*15, 2), true);
        potion.setItemMeta(m);
        itens.add(potion);
        itens.add(new ItemBuilder(351,(short)1).setDisplayName("§cVida Extra").build());

        return itens;
    }

    /*private void startScore(Player player){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("health", "health");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName("§c❤");

        player.setScoreboard(scoreboard);
        scoreboards.put(player, scoreboard);
    }

    private void updateHealthScore(Player player) {
        UUID playerId = player.getUniqueId();
        GamePlayer gamePlayer = findByUUID(playerId);
        Scoreboard scoreboard = scoreboards.get(player);
        if (scoreboard != null) {
            Objective objective = scoreboard.getObjective("health");
            if (objective != null) {
                Score score = objective.getScore(player.getName());
                score.setScore((int) gamePlayer.getLifes());
            }
        }
    }

    private void clearScore(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }*/
}
