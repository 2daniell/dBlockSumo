package com.daniel.blocksumo.model.game.config;

import com.daniel.blocksumo.Main;

public class MinigameConfig {

    public static final int MIN_PLAYERS = Main.config().getInt("Game.MinPlayers");
    public static final int MAX_PLAYERS = Main.config().getInt("Game.MaxPlayers");
    public static final int START_MIN_PLAYERS_COOLDOWN = Main.config().getInt("Game.CountdouwnMin"); //in segunds
    public static final int PLAYERS_LIFE = Main.config().getInt("Game.PlayersLife");;




}
