package com.daniel.blocksumo.manager.config;

import com.daniel.blocksumo.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private static File configFile;
    private static FileConfiguration config;

    public static void setupConfig() {
        configFile = new File(Main.getPlugin(Main.class).getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Main.getPlugin(Main.class).saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static FileConfiguration getConfig() {
        if (config == null) {
            setupConfig();
        }
        return config;
    }

    public static void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reloadConfig() {
        if (configFile == null) {
            configFile = new File(Main.getPlugin(Main.class).getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}