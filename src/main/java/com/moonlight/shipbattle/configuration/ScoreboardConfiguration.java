package com.moonlight.shipbattle.configuration;

import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.scoreboard.ScoreboardState;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class ScoreboardConfiguration {
    private static FileConfiguration config;
    private static File configFile;
    public static int UPDATE_INTERVAL;

    static void load() throws ItemLoadException {
        if (ScoreboardConfiguration.configFile == null) {
            ScoreboardConfiguration.configFile = new File(Main.getMain().getDataFolder(), "scoreboard.yml");
        }
        if (!ScoreboardConfiguration.configFile.exists()) {
            Main.getMain().saveResource("scoreboard.yml", false);
        }

        ScoreboardConfiguration.config = YamlConfiguration.loadConfiguration(ScoreboardConfiguration.configFile);

        UPDATE_INTERVAL = config.getInt("update_interval");
    }

   public static String TITLE_NAME() {
        return config.getString("title");
   }

   public static List<String> getScoreboard(ScoreboardState state) {
        return config.getStringList(state.name());
   }
 }
