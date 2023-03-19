package com.moonlight.shipbattle;

import com.moonlight.shipbattle.database.EmeraldEconomy;
import com.moonlight.shipbattle.database.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            PlayerData data = Main.getMain().getPlayerData().getOrDefault(player.getUniqueId(), new PlayerData(player.getUniqueId()));
            EmeraldEconomy econ = Main.getMain().economy();
            player.sendMessage("Kills: " + data.kills(), "Emeralds: " + data.emeralds(), "Loses: " + data.loses(), "Wins: " + data.wins());
            econ.getEmeralds(player.getUniqueId()).whenComplete((i, p) -> {
                player.sendMessage("Emeralds (Database): " + i);
            });
            return true;
        }
        return false;
    }
}
