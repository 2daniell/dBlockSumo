package com.daniel.blocksumo.objects.enums;

public enum GamePlayerColor {

    RED("Vermelho", "§c"),
    BLUE("Azul", "§1"),
    GREEN("Verde", "§2"),
    YELLOW("Amarelo", "§e"),
    ORANGE("Laranja", "§6"),
    PURPLE("Roxo", "§5"),
    WHITE("Branco", "§f"),
    BLACK("Preto", "§0");

    private String name;
    private String simbol;

    GamePlayerColor(String name, String simbol) {
        this.name = name;
        this.simbol = simbol;
    }

    public String getSimbol() {
        return simbol;
    }

    public String getName() {
        return name;
    }
}
