package com.moonlight.shipbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.teams.Team;

public class Lobby
{
    public static ItemStack navyItemStack;
    public static ItemStack piratesItemStack;
    public static ItemStack random;
    public static ItemStack teamChooserItem;
    private final Inventory inventory;
    private final Game game;
    private BukkitTask task;
    private int timeUntilStart;
    private int maxPlayersPerTeam;
    boolean started;
    
	Lobby(final Game game) {
        this.inventory = Bukkit.createInventory(null, 9, LangConfiguration.getString("lobby.inventory_name"));
        this.started = false;
        this.game = game;
        this.maxPlayersPerTeam = game.getArena().getMinPlayers();
    }
    
    public Inventory getInventory() {
        return this.inventory;
    }
    
    void update() {
        if (this.game.getPlayers().size() > this.game.getArena().getMinPlayers() * 2) {
            final int remainder = this.game.getPlayers().size() % 2;
            this.maxPlayersPerTeam = this.game.getPlayers().size() / 2 + remainder;
        }
        for (final Team team : this.game.teams) {
            while (team.getLobbyPlayers().size() > this.maxPlayersPerTeam) {
                final Player player = team.getLobbyPlayers().get(team.getLobbyPlayers().size() - 1);
                team.getLobbyPlayers().remove(player);
                team.getQueuedPlayers().add(0, player);
                player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.joined_team_queue").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()).replace("$c", team.getQueuedPlayers().size() + ""));
            }
            while (team.getLobbyPlayers().size() < this.maxPlayersPerTeam && team.getQueuedPlayers().size() > 0) {
                final Player player = team.getQueuedPlayers().get(0);
                team.getLobbyPlayers().add(player);
                team.getQueuedPlayers().remove(player);
                player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.joined_team").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()));
            }
        }
        this.inventory.setItem(2, this.game.navy.getJoinItemStack(this.maxPlayersPerTeam));
        this.inventory.setItem(4, Lobby.random);
        this.inventory.setItem(6, this.game.pirates.getJoinItemStack(this.maxPlayersPerTeam));
    }
    
    public void addPlayer(final Player player, final Team team) {
        if (team.getLobbyPlayers().contains(player) || team.getQueuedPlayers().contains(player)) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.already_in_team").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()));
            return;
        }
        team.getEnemyTeam().getLobbyPlayers().remove(player);
        team.getEnemyTeam().getQueuedPlayers().remove(player);
        if (team.getLobbyPlayers().size() < this.maxPlayersPerTeam) {
            team.getLobbyPlayers().add(player);
            player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.joined_team").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()));
        }
        else {
            team.getQueuedPlayers().add(player);
            player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.joined_team_queue").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()).replace("$c", team.getQueuedPlayers().size() + ""));
        }
        this.update();
    }
    
    public void removePlayer(final Player player) {
        for (final Team team : this.game.teams) {
            if (team.getLobbyPlayers().contains(player)) {
                team.getLobbyPlayers().remove(player);
                this.update();
                return;
            }
            if (team.getQueuedPlayers().contains(player)) {
                team.getQueuedPlayers().remove(player);
                this.update();
                return;
            }
        }
    }
    
    private void arrangeTeams() {
        final List<Player> remainingPlayers = new ArrayList<Player>(this.game.getPlayers());
        for (final Team team : this.game.teams) {
            remainingPlayers.removeAll(team.getLobbyPlayers());
            final List<Player> lobbyPlayers = team.getLobbyPlayers();
            final Team obj = team;
            Objects.requireNonNull(obj);
            lobbyPlayers.forEach(obj::addPlayer);
            while (team.getQueuedPlayers().size() > 0 && team.getPlayers().size() < this.maxPlayersPerTeam) {
                team.addPlayer(team.getQueuedPlayers().get(0));
                remainingPlayers.remove(0);
            }
        }
        final Random random = new Random();
        while (remainingPlayers.size() > 0) {
            if (this.game.teams[0].getPlayers().size() == this.game.teams[1].getPlayers().size()) {
                this.game.teams[random.nextInt(1)].addPlayer(remainingPlayers.get(0));
            }
            else if (this.game.teams[0].getPlayers().size() < this.game.teams[1].getPlayers().size()) {
                this.game.teams[0].addPlayer(remainingPlayers.get(0));
            }
            else {
                this.game.teams[1].addPlayer(remainingPlayers.get(0));
            }
            remainingPlayers.remove(0);
        }
    }
    
    void run() {
        this.timeUntilStart = Configuration.gameStartCooldown;
        this.task = Main.getMain().getServer().getScheduler().runTaskTimer(Main.getMain(), this::tick, 0L, 20L);
    }
    
    void cancel() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = null;
    }
    
    private void tick() {
        if (this.timeUntilStart > 0) {
            if (this.timeUntilStart == 5) {
                if (this.game.getPlayers().size() < 2) {
                    this.game.getCommunicator().broadcastActionBar(LangConfiguration.getString("lobby.not_enough_players"));
                    this.reset();
                    return;
                }
                this.game.setStatus(Game.Status.STARTING);
                this.update();
                this.arrangeTeams();
                this.game.requestBalances();
                for (final Team team : this.game.teams) {
                    team.getPlayers().forEach(player -> {
                        player.teleport(team.getSpawn());
                        player.setWalkSpeed(0.0f);
                        player.closeInventory();
                        player.setGameMode(GameMode.SURVIVAL);
                        return;
                    });
                }
            }
            if (this.timeUntilStart <= 5) {
                this.game.getPlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
            }
            this.game.getCommunicator().broadcastActionBar(LangConfiguration.getString("lobby.time_until_start").replace("$", this.timeUntilStart + ""));
            --this.timeUntilStart;
        }
        else {
            this.game.start();
            this.cancel();
        }
    }
    
    void reset() {
        this.cancel();
        this.started = false;
        while (this.game.getPlayers().size() > 0) {
            this.game.removePlayer(this.game.getPlayers().get(0), false);
        }
        if (this.game.getStatus() == Game.Status.STARTING) {
            this.game.getArena().reset();
        }
    }
}
