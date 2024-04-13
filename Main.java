package com.daniel.blocksumo;

import com.daniel.blocksumo.command.ArenaCommand;
import com.daniel.blocksumo.command.BlockSumoCommand;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.event.InventoryClick;
import com.daniel.blocksumo.model.Arena;
import com.daniel.blocksumo.world.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final MatchManager manager = new MatchManager();
    private final WorldGenerator generator = new WorldGenerator();

    @Override
    public void onEnable() {
        Arena.loadArenas(generator);
        Arena.resetAll(generator);
        registerCommand();
        registerEvents();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
    }

    public void registerCommand() {
        getCommand("arena").setExecutor(new ArenaCommand(manager, generator));
        getCommand("blocksumo").setExecutor(new BlockSumoCommand(manager));
    }
}