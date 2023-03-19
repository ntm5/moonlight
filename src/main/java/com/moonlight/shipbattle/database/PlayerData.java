package com.moonlight.shipbattle.database;

import com.moonlight.shipbattle.Main;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    private Integer kills = 0;
    private Integer wins = 0;
    private Integer loses = 0;
    private Integer emeralds = 0;
    private final UUID uuid;
    private final EmeraldEconomy economy = Main.getMain().economy();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        loadData();
    }

    public void loadData() {
        Main.getMain().economy().getKills(uuid).whenComplete((val, __) -> kills = val);
        Main.getMain().economy().getWins(uuid).whenComplete((val, __) -> wins = val);
        Main.getMain().economy().getLoses(uuid).whenComplete((val, __) -> loses = val);
        Main.getMain().economy().getEmeralds(uuid).whenComplete((val, __) -> emeralds = val);
    }

    public void setKills() {
        economy.setKills(this.uuid, this.kills + 1);
        this.kills += kills;
    }

    public void setWins() {
        economy.setWins(this.uuid, this.wins + 1);
        this.wins += kills;
    }

    public void setLoses() {
        economy.setLoses(this.uuid, this.loses + 1);
        this.loses += loses;
    }

    public void setEmeralds(int emeralds) {
        economy.setEmeralds(this.uuid, this.emeralds + emeralds);
        this.emeralds += emeralds;
    }

    public Integer emeralds() {
        return emeralds;
    }

    public Integer kills() {
        return kills;
    }

    public Integer wins() {
        return wins;
    }

    public Integer loses() {
        return loses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
