package com.daniel.blocksumo.model;

import com.daniel.blocksumo.objects.enums.MatchState;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Getter
public class Match extends Minigame {

    private List<Player> players;
    private Timer timer;
    private Arena arena;
    private MatchState state;

    public Match(Arena arena) {
        this.arena = arena;
        this.state = MatchState.WAITING;
        this.players = new ArrayList<>();
    }

    public void startCountdown() {

    }

    public void addPlayer(Player player) {
        if ((!(players.size() < MAX_PLAYERS)) && (state.equals(MatchState.WAITING))) {
            players.add(player);

            players.forEach(e -> e.sendMessage("§aAgora possuem §a(" + players.size() + "/8)"));
            startCountdown();
        }
    }
}
