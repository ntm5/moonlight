package com.moonlight.shipbattle.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.moonlight.shipbattle.logging.Logging;
import org.bukkit.scheduler.BukkitRunnable;

class SetBalanceTask extends BukkitRunnable
{
    private final String player;
    private final int balance;
    private final String query;


    SetBalanceTask(final String player, final int balance, String query) {
        this.player = player;
        this.balance = balance;
        this.query = query;
    }
    
    public void run() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = EconomyDatabase.getDatabase().getConnection().prepareStatement(query);
            preparedStatement.setString(1, this.player);
            preparedStatement.setInt(2, this.balance);
            preparedStatement.executeUpdate();
            Logging.getLogger().log(Level.INFO, "Balance successfully updated.");
        }
        catch (SQLException e) {
            Logging.getLogger().log(Level.SEVERE, "Could update database.", e);
            e.printStackTrace();
            try {
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
