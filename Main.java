package com.daniel.blocksumo;

import com.daniel.blocksumo.command.ArenaCommand;
import com.daniel.blocksumo.command.BlockSumoCommand;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.event.InventoryClick;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final MatchManager manager = new MatchManager();

    @Override
    public void onEnable() {
        registerCommand();
        registerEvents();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
    }

    public void registerCommand() {
        getCommand("arena").setExecutor(new ArenaCommand(manager));
        getCommand("blocksumo").setExecutor(new BlockSumoCommand(manager));
    }
}