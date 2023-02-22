package com.moonlight.shipbattle;

import com.moonlight.shipbattle.database.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScoreboardManager extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "shipbattle";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ntm";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {

        PlayerData data = new PlayerData(p.getUniqueId());

        if (identifier.equals("emeralds")) {
            return String.valueOf(data.emeralds());
        }
        if (identifier.equals("gold")) {
            return new PlayerPointsAPI(PlayerPoints.getInstance()).lookShorthand(p.getUniqueId());
        }

        if (identifier.equals("loses")) {
            return String.valueOf(data.loses());
        }

        if (identifier.equals("wins")) {
            return String.valueOf(data.wins());
        }

        if (identifier.equals("kills")) {
            return String.valueOf(data.kills());
        }

        return null;
    }
}
