package com.moonlight.shipbattle.configuration;

import com.moonlight.shipbattle.InventoryManager;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.ScoreboardManager;
import com.moonlight.shipbattle.database.EmeraldTask;
import com.moonlight.shipbattle.shop.Shop;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScoreboardConfiguration {
    private static FileConfiguration config;
    private static File configFile;

    static void load() throws ItemLoadException {
        if (ScoreboardConfiguration.configFile == null) {
            ScoreboardConfiguration.configFile = new File(Main.getMain().getDataFolder(), "items.yml");
        }
        if (!ScoreboardConfiguration.configFile.exists()) {
            Main.getMain().saveResource("items.yml", false);
        }
    }

    public static List<String> getScoreboard(ScoreboardManager.ScoreboardState state) {
        return config.getStringList(state.getValue() + ".display");
    }

    public static String parseCode(String value, Player player) {
        AtomicInteger bal = new AtomicInteger(0);
        try {
            new EmeraldTask().getBalance(player.getName(), bal::set);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        value = value.replace("{player}", player.getName());
        PlayerPointsAPI api = new PlayerPointsAPI(PlayerPoints.getInstance());
        value = value.replace("{player_gold}", String.valueOf(api.look(player.getUniqueId())));
        value = value.replace("{player_emerald}", String.valueOf(bal.get()));
        return value;
    }
 }
