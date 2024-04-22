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

    public String getTag() {
        String primeiraLetra = this.name().substring(0, 1).toUpperCase();
        return switch (this) {
            case RED -> "§c[" + primeiraLetra + "] §f";
            case BLUE -> "§1[" + primeiraLetra + "] §f";
            case GREEN -> "§2[" + primeiraLetra + "] §f";
            case YELLOW -> "§e[" + primeiraLetra + "] §f";
            case ORANGE -> "§6[" + primeiraLetra + "] §f";
            case PURPLE -> "§5[" + primeiraLetra + "] §f";
            case WHITE -> "§f[" + primeiraLetra + "] §f";
            case BLACK -> "§0[" + primeiraLetra + "] §f";
        };
    }

}
