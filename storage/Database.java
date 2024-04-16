package com.daniel.blocksumo.storage;

import com.daniel.blocksumo.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;

public class Database {

    private static Connection con;

    public static Connection open() {
        if (Main.config().getBoolean("MySQL.Enable")) {

            String username = Main.config().getString("MySQL.Username");
            String database = Main.config().getString("MySQL.Database");
            String host = Main.config().getString("MySQL.Host");
            String port = Main.config().getString("MySQL.Port");
            String password = Main.config().getString("MySQL.Password");
            String url = "jdbc:mysql://"+host+":"+port+"/"+database+"?characterEncoding=utf8";

            try {
                con = DriverManager.getConnection(url, username, password);
                createTable(con);
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cA conexao com o §fMySQL §cfalhou!");
                Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cAlterando para o §fSQLite");
                openSQLite();
            }
        } else {
            openSQLite();
        }

        return con;
    }

    private static void createTable(Connection con) {
        if (con == null) return;
        final String sqlMatches = "CREATE TABLE IF NOT EXISTS matches (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(255)," +
                "world VARCHAR(255)," +
                "spawn_waiting VARCHAR(255)," +
                "spawn_area VARCHAR(255)" +
                ")";

        final String sqlSpawns = "CREATE TABLE IF NOT EXISTS spawns (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    match_id VARCHAR(36)," +
                "    location VARCHAR(255)," +
                "    FOREIGN KEY (match_id) REFERENCES matches(id)" +
                ")";

        try {
            PreparedStatement stm = con.prepareStatement(sqlMatches);
            stm.executeUpdate();
            PreparedStatement st = con.prepareStatement(sqlSpawns);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //fazer jaja
    private static void openSQLite() {
        File datafolder = new File("plugins/dBlockSumo/databases");
        if (!datafolder.exists()) datafolder.mkdirs();
        File file = new File(datafolder, "database.db");
        String url = "jdbc:sqlite:"+file;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(url);
            createTable(con);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Main.prefix + "§cA conexao com o §fSQLite §cfalhou!");
            Main.getPlugin(Main.class).getPluginLoader().disablePlugin(Main.getPlugin(Main.class));
        }
    }

}
