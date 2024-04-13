package com.daniel.blocksumo;

import com.daniel.blocksumo.command.ArenaCommand;
import com.daniel.blocksumo.command.BlockSumoCommand;
import com.daniel.blocksumo.manager.config.ConfigManager;
import com.daniel.blocksumo.menu.event.InventoryClick;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.storage.Database;
import com.daniel.blocksumo.world.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static String prefix = "§f[§6BlockSumo§f] §7-> ";

    private final WorldGenerator generator = new WorldGenerator();

    @Override
    public void onEnable() {
        ConfigManager.setupConfig();
        Database.open();
        Match.loadArenas(generator);
        Match.resetAll(generator);
        registerCommand();
        registerEvents();
    }


    public static FileConfiguration config() {
        return ConfigManager.getConfig();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
    }

    public void registerCommand() {
        getCommand("arena").setExecutor(new ArenaCommand(generator));
        getCommand("blocksumo").setExecutor(new BlockSumoCommand());
    }
}