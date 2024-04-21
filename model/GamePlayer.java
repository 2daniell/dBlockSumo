package com.daniel.blocksumo.model;

import com.daniel.blocksumo.api.ItemBuilder;
import com.daniel.blocksumo.model.game.config.MinigameConfig;
import com.daniel.blocksumo.objects.enums.GamePlayerColor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class GamePlayer extends MinigameConfig {

    @Setter
    private double lifes;

    private UUID id;
    private String name;
    @Setter
    private boolean isDead;
    @Setter
    private GamePlayerColor color;

    public GamePlayer(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.lifes = PLAYERS_LIFE;
        this.isDead = false;
    }

    public String getDisplay() {
        return color.getSimbol()+color.getName() + " [§f"+(int)lifes+color.getSimbol()+"]";
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }


}
