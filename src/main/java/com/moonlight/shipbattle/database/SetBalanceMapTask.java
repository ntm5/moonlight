package com.moonlight.shipbattle.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.moonlight.shipbattle.logging.Logging;

import java.util.Map;
import org.bukkit.scheduler.BukkitRunnable;

class SetBalanceMapTask extends BukkitRunnable
{
    private final Map<String, Integer> map;
    private final String query;


    SetBalanceMapTask(final Map<String, Integer> map, String query) {
        this.map = map;
        this.query = query;
    }
    
    public void run() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = EconomyDatabase.getDatabase().getConnection().prepareStatement(query);
            for (final Map.Entry<String, Integer> entry : this.map.entrySet()) {
                preparedStatement.setString(1, entry.getKey());
                preparedStatement.setInt(2, entry.getValue());
                preparedStatement.executeUpdate();
            }
            Logging.getLogger().log(Level.INFO, "Balances successfully updated.");
        }
        catch (Exception e) {
            Logging.getLogger().log(Level.SEVERE, "Could not set balances map.", e);
            e.printStackTrace();
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (SQLException e3) {
                e3.printStackTrace();
            }
        }
    }
}
