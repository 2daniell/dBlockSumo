package com.daniel.blocksumo.model;

import com.daniel.blocksumo.model.game.config.MinigameConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class GamePlayer extends MinigameConfig {

    private UUID id;
    private String name;
    @Setter
    private int lifes;

    public GamePlayer(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.lifes = PLAYERS_LIFE;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }
}
