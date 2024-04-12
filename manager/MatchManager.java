package com.daniel.blocksumo.manager;

import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.objects.enums.MatchState;
import com.daniel.blocksumo.storage.Cache;

import java.util.List;

public class MatchManager extends Cache<Match> {

    public List<Match> findAllByState() {
        return findList(e -> e.getState().equals(MatchState.WAITING));
    }

    public boolean add(Match match) {
        return super.add(match);
    }

    public boolean remove(Match match) {
        return remove(match);
    }
}
