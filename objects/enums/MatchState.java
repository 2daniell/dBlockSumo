package com.daniel.blocksumo.objects.enums;

public enum MatchState {

    WAITING("Waiting"), STARTING("Starting"),STARTED("Started"), FINISHING("Finishing"), RELOADING("Reloading");

    private String name;

    MatchState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
