package com.moonlight.shipbattle.database;

import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.logging.Logging;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class EmeraldEconomy {

    private final Connection connection;

    public EmeraldEconomy() throws SQLException {
        final String url = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true", Configuration.database_host, Configuration.database_port, Configuration.database_name, Configuration.database_username, Configuration.database_password);
        Bukkit.getLogger().log(Level.INFO, "Connecting to: {0}", url);
        this.connection = DriverManager.getConnection(url);
        createTable();
    }

    public void createTable() {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `playerdata` (uuid VARCHAR(255), emeralds INT, kills INT, wins INT, loses INT, PRIMARY KEY(uuid))");
                PreparedStatement statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `shipbattle` (player VARCHAR(255), balance INT, PRIMARY KEY(player))");
                statement2.executeUpdate();
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Boolean> exists(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Bukkit.getLogger().info("Checking if player (" + uuid.toString() + ") exists");
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `playerdata` WHERE `uuid` = ?");
                statement.setString(1, uuid.toString());
                return statement.executeQuery().next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public void createFreshData(UUID uuid){
            CompletableFuture.runAsync(() -> {
                try {
                    Bukkit.getLogger().info("Trying to create player data for " + uuid);
                    PreparedStatement statement = connection.prepareStatement("INSERT IGNORE INTO `playerdata` (uuid, emeralds, kills, wins, loses) VALUES (?, 20, 0, 0, 0)");
                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
    }

    public CompletableFuture<Integer> getKills(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT `kills` FROM `playerdata` WHERE `uuid` = ?");
                int kills = 0;
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    kills = set.getInt("kills");
                }
                set.close();
                return kills;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public CompletableFuture<Integer> getWins(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT `wins` FROM `playerdata` WHERE `uuid` = ?");
                int kills = 0;
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    kills = set.getInt("wins");
                }
                set.close();
                return kills;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public CompletableFuture<Integer> getLoses(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT `loses` FROM `playerdata` WHERE `uuid` = ?");
                statement.setString(1, uuid.toString());
                int kills = 0;
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    kills = set.getInt("loses");
                }
                set.close();
                return kills;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public CompletableFuture<Integer> getEmeralds(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT `emeralds` FROM `playerdata` WHERE `uuid` = ?");
                int kills = 0;
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    kills = set.getInt("emeralds");
                }
                set.close();
                return kills;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public void setKills(UUID uuid, int increment) {
        CompletableFuture.runAsync(() -> {
            try {
                Bukkit.getLogger().info("Settings kills for player: " + uuid.toString() + " kills: " + (Main.getMain().getPlayerData().get(uuid).kills() + 1));
                PreparedStatement statement = connection.prepareStatement("UPDATE `playerdata` SET kills=? WHERE uuid =?");
                statement.setInt(1, increment);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void setWins(UUID uuid, int increment) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE `playerdata` SET wins=? WHERE uuid =?");
                statement.setInt(1, increment);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void setLoses(UUID uuid, int increment) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE `playerdata` SET loses=? WHERE uuid=?");
                statement.setInt(1, increment);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void setEmeralds(UUID uuid, int increment) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE `playerdata` SET emeralds=? WHERE uuid=?");
                statement.setInt(1, increment);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
