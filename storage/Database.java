package com.daniel.blocksumo.storage;

import com.daniel.blocksumo.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    private static Connection con;

    private static void open() {
        if (Main.config().getBoolean("MySQL.Enable")) {

            String username = Main.config().getString("MySQL.Username");
            String database = Main.config().getString("MySQL.Database");
            String host = Main.config().getString("MySQL.Host");
            String port = Main.config().getString("MySQL.Port");
            String password = Main.config().getString("MySQL.Password");
            String url = "jdbc:mysql://"+host+":"+port+"/"+database+"?characterEncoding=utf8";

            try {
                con = DriverManager.getConnection(url, username, password);
                Bukkit.getConsoleSender().sendMessage(Main.prefix + "§aA conexao com o §fMySQL §aexecutada!");
            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cA conexao com o §fMySQL §cfalhou!");
                Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cAlterando para o §fSQLite");
                openSQLite();
            }
        } else {
            openSQLite();
        }
    }

    private static void openSQLite() {
        File datafolder = new File("plugins/dBlockSumo/databases");
        if (!datafolder.exists()) datafolder.mkdirs();
        File file = new File(datafolder, "database.db");
        String url = "jdbc:sqlite:"+file;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(url);
            Bukkit.getConsoleSender().sendMessage(Main.prefix + "§aA conexao com o §fSQLite §aexecutada!");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cA conexao com o §fSQLite §cfalhou!");
        }
    }


}
