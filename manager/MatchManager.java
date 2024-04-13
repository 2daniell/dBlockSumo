package com.daniel.blocksumo.manager;

import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.storage.Cache;

import java.util.List;

public class MatchManager extends Cache<Match> {

    public List<Match> findAllByState(MatchState state) {
        return findList(e -> e.getState().equals(state));
    }

    public Match findByName(String name) {
        return find(e -> e.getName().equalsIgnoreCase(name));
    }

    public boolean create(Match match) {
        return add(match);
    }

    public boolean remove(Match match) {
        return super.remove(match);
    }
}
