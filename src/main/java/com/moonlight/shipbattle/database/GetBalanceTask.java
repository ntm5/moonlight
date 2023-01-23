package com.moonlight.shipbattle.database;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.configuration.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

class GetBalanceTask extends BukkitRunnable
{
    private final String player;
    private final BalanceReceivedListener listener;
    
    GetBalanceTask(final String player, final BalanceReceivedListener listener) {
        this.player = player;
        this.listener = listener;
    }
    
    public void run() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = EconomyDatabase.getDatabase().getConnection().prepareStatement(EconomyDatabase.GET_QUERY);
            preparedStatement.setString(1, this.player);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                this.listener.onFinished(resultSet.getInt("balance"));
                return;
            }
            this.listener.onFinished(Configuration.defaultBalance);
            Logging.getLogger().log(Level.INFO, "Balance successfully received.");
        }
        catch (SQLException e) {
            Logging.getLogger().log(Level.SEVERE, "Could not get balance from database.", e);
            e.printStackTrace();
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
