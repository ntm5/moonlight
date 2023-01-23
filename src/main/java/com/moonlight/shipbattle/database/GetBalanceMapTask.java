package com.moonlight.shipbattle.database;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.configuration.Configuration;
import java.util.HashMap;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

class GetBalanceMapTask extends BukkitRunnable
{
    private final List<String> players;
    private final BalanceMapReceivedListener listener;
    
    GetBalanceMapTask(final List<String> players, final BalanceMapReceivedListener listener) {
        this.players = players;
        this.listener = listener;
    }
    
    public void run() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        final HashMap<String, Integer> map = new HashMap<String, Integer>(this.players.size());
        try {
            preparedStatement = EconomyDatabase.getDatabase().getConnection().prepareStatement(EconomyDatabase.GET_QUERY);
            for (final String player : this.players) {
                preparedStatement.setString(1, player);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    map.put(player, resultSet.getInt("balance"));
                }
                else {
                    map.put(player, Configuration.defaultBalance);
                }
            }
            this.listener.onFinished(map);
            Logging.getLogger().log(Level.INFO, "Balances successfully received.");
        }
        catch (SQLException e) {
            Logging.getLogger().log(Level.SEVERE, "Could not get balances from database.", e);
            e.printStackTrace();
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }
}
