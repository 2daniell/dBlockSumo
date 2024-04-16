package com.daniel.blocksumo;

import com.daniel.blocksumo.api.enums.Version;
import com.daniel.blocksumo.command.ArenaCommand;
import com.daniel.blocksumo.command.BlockSumoCommand;
import com.daniel.blocksumo.events.MinigameEvents;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.manager.config.ConfigManager;
import com.daniel.blocksumo.menu.event.InventoryClick;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.storage.Database;
import com.daniel.blocksumo.world.WorldGenerator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends JavaPlugin {

    @Getter
    private static Version version;
    private final MatchManager manager = new MatchManager();

    @Override
    public void onEnable() {
        Database.open();
        registerClass();
        registerCommand();
        registerEvents();
        loadLobby();
        ConfigManager.setupConfig();
        MatchManager.loadAll(manager);


    }

    @Override
    public void onDisable() {
    }

    public static FileConfiguration config() {
        return ConfigManager.getConfig();
    }

    private void registerClass() {
        version = Version.getServerVersion();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
        Bukkit.getPluginManager().registerEvents(new MinigameEvents(manager), this);
    }

    public void registerCommand() {
        getCommand("arena").setExecutor(new ArenaCommand(manager));
        getCommand("blocksumo").setExecutor(new BlockSumoCommand(manager));
    }

    public static String prefix = "§f[§6BlockSumo§f] §7-> ";
    public static Location lobby;
    private static File dataFolder = new File("plugins/dBlockSumo/lobby");
    private static File file = new File(dataFolder, "lobby.json");
    public static void saveLobby(Location location) throws IOException {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        JSONObject lobbyJson = new JSONObject();
        lobbyJson.put("world", location.getWorld().getName());
        lobbyJson.put("x", location.getX());
        lobbyJson.put("y", location.getY());
        lobbyJson.put("z", location.getZ());
        lobbyJson.put("yaw", location.getYaw());
        lobbyJson.put("pitch", location.getPitch());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(lobbyJson.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLobby() {
        if (!file.exists()) return;
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(file)) {
            Object obj = parser.parse(reader);
            JSONObject lobbyJson = (JSONObject) obj;

            String worldName = (String) lobbyJson.get("world");
            World world = Bukkit.getWorld(worldName);
            double x = (double) lobbyJson.get("x");
            double y = (double) lobbyJson.get("y");
            double z = (double) lobbyJson.get("z");
            float yaw = Float.parseFloat(lobbyJson.get("yaw").toString());
            float pitch = Float.parseFloat(lobbyJson.get("pitch").toString());

            lobby = new Location(world, x, y, z, yaw, pitch);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
