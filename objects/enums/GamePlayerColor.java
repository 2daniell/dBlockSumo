package com.daniel.blocksumo.objects.enums;

public enum GamePlayerColor {

    RED("Vermelho"),
    BLUE("Azul"),
    GREEN("Verde"),
    YELLOW("Amarelo"),
    ORANGE("Laranja"),
    PURPLE("Roxo"),
    WHITE("Branco"),
    BLACK("Preto");

    private String name;

    GamePlayerColor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
