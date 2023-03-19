package com.moonlight.shipbattle.scoreboard;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ScoreboardManager {
    private final Map<UUID, Scoreboard> scoreboardManagerMap = new HashMap<>();

    public void addPlayer(Player uuid) {
        this.scoreboardManagerMap.putIfAbsent(uuid.getUniqueId(), new Scoreboard(uuid, ScoreboardState.IDLE));
    }

    public boolean setState(UUID uuid, ScoreboardState state) {
        if (this.scoreboardManagerMap.get(uuid) == null)
            return false;

        this.scoreboardManagerMap.get(uuid).setState(state);
        return true;
    }

    public void removePlayer(UUID uuid) {
        this.scoreboardManagerMap.remove(uuid);
    }
}
