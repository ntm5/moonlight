package com.moonlight.shipbattle;

import com.moonlight.shipbattle.configuration.ScoreboardConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class ScoreboardManager {
    private final Player player;
    private final Scoreboard scoreboard;
    private Objective obj;
    private final Team team;
    private ScoreboardState state;

    public ScoreboardManager(Player player, ScoreboardState state) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.obj = scoreboard.registerNewObjective("ServerName", "dummy", "Test Server");
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.team = scoreboard.registerNewTeam(player.getUniqueId().toString());
        this.state = state;
    }

    public void setState(ScoreboardState state) {
        this.state = state;
    }

    public void updateScoreboard() {
        List<String> list = ScoreboardConfiguration.getScoreboard(state);
        for (int i = 0; i < list.size(); i++) {
            org.bukkit.scoreboard.Score score = this.obj.getScore(ChatColor.translateAlternateColorCodes('&', ScoreboardConfiguration.parseCode(list.get(i), player)));
            score.setScore(15 - i);
        }
        player.setScoreboard(this.scoreboard);
    }

    public enum ScoreboardState {
        LOBBY("lobby"),
        QUEUE("queue"),
        IN_GAME("in-game");

        private final String s;

        ScoreboardState(String s) {
            this.s = s;
        }

        public String getValue() {
            return this.s;
        }
    }
}
