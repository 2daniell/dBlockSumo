package com.daniel.blocksumo.objects.enums;

public enum MatchState {

    WAITING("Waiting"), STARTING("Starting"),STARTED("Started"), RELOADING("Reloading");

    private String name;

    MatchState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
