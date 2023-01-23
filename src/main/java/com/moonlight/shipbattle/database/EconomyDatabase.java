package com.moonlight.shipbattle.database;

import java.util.Map;
import java.util.List;

import com.moonlight.shipbattle.Main;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.logging.Level;

import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.configuration.Configuration;
import java.sql.Connection;

public class EconomyDatabase
{
    private static EconomyDatabase instance;
    private Connection connection;
    static String GET_QUERY;
    static String UPDATE_QUERY;
    
    public static void setup() {
        EconomyDatabase.GET_QUERY = "SELECT `balance` FROM `$` WHERE `player` = ?".replace("$", Configuration.database_table);
        EconomyDatabase.UPDATE_QUERY = "INSERT INTO `$` (player, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance);".replace("$", Configuration.database_table);
    }
    
    public EconomyDatabase() throws SQLException {
        EconomyDatabase.instance = this;
        final String url = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true", Configuration.database_host, Configuration.database_port, Configuration.database_name, Configuration.database_username, Configuration.database_password);
        Logging.getLogger().log(Level.INFO, "Connecting to: {0}", url);
        this.connection = DriverManager.getConnection(url);
    }
    
    Connection getConnection() {
        return this.connection;
    }
    
    public void setBalance(final String player, final int balance) {
        new SetBalanceTask(player, balance).runTaskAsynchronously((Plugin) Main.getMain());
    }
    
    public void getBalance(final String player, final BalanceReceivedListener listener) {
        new GetBalanceTask(player, listener).runTaskAsynchronously((Plugin)Main.getMain());
    	//listener.onFinished(8700); // test only
    }
    
    public void getBalances(final List<String> players, final BalanceMapReceivedListener listener) {
        new GetBalanceMapTask(players, listener).runTaskAsynchronously((Plugin)Main.getMain());
    	//listener.onFinished(players.stream().collect(Collectors.toMap(f -> f, f -> 8700))); // test only
    }
    
    public void setBalances(final Map<String, Integer> map) {
        new SetBalanceMapTask(map).runTaskAsynchronously((Plugin)Main.getMain());
    }
    
    public static EconomyDatabase getDatabase() {
        return EconomyDatabase.instance;
    }
}
