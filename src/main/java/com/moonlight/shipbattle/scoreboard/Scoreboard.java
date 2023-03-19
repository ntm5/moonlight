package com.moonlight.shipbattle.scoreboard;

import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.configuration.ScoreboardConfiguration;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;

public class Scoreboard {
    private final org.bukkit.scoreboard.Scoreboard scoreboard;
    private final Player player;
    private ScoreboardState state;
    private Objective objective;

    public Scoreboard( Player player, ScoreboardState state) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(ScoreboardConfiguration.TITLE_NAME(), "dummy", ChatColor.translateAlternateColorCodes('&', ScoreboardConfiguration.TITLE_NAME()), RenderType.INTEGER);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.player = player;
        this.state = state;
        updateScoreboard();
    }

    public void updateScoreboard() {
        Bukkit.getScheduler().runTaskTimer(Main.getMain(), () -> {
            if (state == ScoreboardState.IN_GAME)
                return;

            for (int i = 0; i < ScoreboardConfiguration.getScoreboard(state).size(); i++) {
                Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', parseCode(ScoreboardConfiguration.getScoreboard(state).get(i))));
                score.setScore((i + 1));
            }

            player.setScoreboard(this.scoreboard);
        }, 5L, 5L);
    }

    public String parseCode(String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public void setState(ScoreboardState state) {
        this.state = state;
    }
}
