package com.moonlight.shipbattle;

import com.moonlight.shipbattle.configuration.ArenaConfiguration;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.database.EconomyDatabase;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.scoreboard.ScoreboardState;
import com.moonlight.shipbattle.teams.Team;
import com.moonlight.shipbattle.teams.TeamType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Game implements Listener {
    private Status status;
    private final Map<Player, Long> lootTimes;
    private final List<Player> players;
    private boolean firstBloodDrawn;
    private final HashMap<Player, Integer> balances;
    private final Arena arena;
    private final Communicator communicator;
    private final Lobby lobby;
    private final Scoreboard scoreboard;
    private final Objective sidebarObjective;
    public final Team navy;
    public final Team pirates;
    public final Team[] teams;
    private LocalTime remainingTime;
    private final DateTimeFormatter formatter;
    private BukkitTask timerTask;
    public Map<Player, Integer> kills;

    Game(final Arena arena) {
        this.kills = new HashMap<>();
        this.status = Status.WAITING;
        this.lootTimes = new HashMap<>();
        this.firstBloodDrawn = false;
        this.teams = new Team[2];
        this.remainingTime = LocalTime.ofSecondOfDay(Configuration.timer);
        this.formatter = DateTimeFormatter.ofPattern("mm:ss");
        Logging.getLogger().info("Initialising game: " + this.hashCode());
        this.arena = arena;
        final int maxPlayers = arena.getMaxPlayers() * 2;
        this.players = new ArrayList<>(maxPlayers);
        this.balances = new HashMap<>(maxPlayers);
        this.lobby = new Lobby(this);
        this.communicator = new Communicator(this);
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.navy = new Team(TeamType.NAVY, this);
        this.pirates = new Team(TeamType.PIRATES, this);
        this.navy.setEnemyTeam(this.pirates);
        this.pirates.setEnemyTeam(this.navy);
        this.teams[0] = this.navy;
        this.teams[1] = this.pirates;
        (this.sidebarObjective = this.scoreboard.registerNewObjective("sb_sidebar", "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
        this.sidebarObjective.setDisplayName(LangConfiguration.getString("scoreboard.display_name"));
        Logging.getLogger().info("Game initialized: " + this.toString());
        respawnProtection();
    }

    public Status getStatus() {
        return this.status;
    }

    void setStatus(final Status status) {
        this.status = status;
    }

    public Map<Player, Long> getLootTimes() {
        return this.lootTimes;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public boolean isFirstBloodDrawn() {
        return this.firstBloodDrawn;
    }

    public void setFirstBloodDrawn() {
        this.firstBloodDrawn = true;
    }

    public HashMap<Player, Integer> getBalances() {
        return this.balances;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public Objective getSidebarObjective() {
        return this.sidebarObjective;
    }

    public Communicator getCommunicator() {
        return this.communicator;
    }

    public Lobby getLobby() {
        return this.lobby;
    }

    public boolean hasStarted() {
        return this.status == Status.STARTED;
    }

    private void addPlayer(final Player player) {
        Logging.getLogger().log(Level.INFO, "[0] {0}: joining player: {1}", new Object[]{this.toString(), player.getName()});
        if (this.players.contains(player)) {//added by TeddyBear_2004 to avoid duplicate players in the game
            Logging.getLogger().log(Level.INFO, "[0] {0}: player already in game: {1}", new Object[]{this.toString(), player.getName()});
            return;
        }
        this.communicator.broadcastMessage(LangConfiguration.getString("game.join.broadcast").replace("$a", player.getName()).replace("$b", this.players.size() + 1 + "").replace("$c", this.arena.getMaxPlayers() * 2 + ""));
        player.sendMessage(Main.prefix + LangConfiguration.getString("game.join").replace("$a", this.players.size() + 1 + "").replace("$b", this.arena.getMaxPlayers() * 2 + ""));
        this.communicator.sendActionBar(player, LangConfiguration.getString("lobby.choose_team"));
        if (this.players.size() + 1 < this.arena.getMinPlayers() * 2) {
            this.communicator.broadcastActionBar(LangConfiguration.getString("lobby.players_needed").replace("$", this.arena.getMinPlayers() * 2 - this.players.size() + ""));
        }
        this.communicator.broadcastActionBar(LangConfiguration.getString("lobby.players_needed").replace("$", this.arena.getMinPlayers() * 2 - this.players.size() + ""));
        this.players.add(player);
        Score.setupScore(player);
        player.teleport(this.arena.getLocation("lobby"));
        InventoryManager.saveInventory(player);
        this.arena.updateSigns();
        this.lobby.update();
        Logging.getLogger().log(Level.INFO, "[1] {0}: joined player: {1}", new Object[]{this.toString(), player.getName()});
        if (!this.lobby.started && this.players.size() == this.arena.getMinPlayers() * 2) {
            this.lobby.run();
            this.lobby.started = true;
        }
    }

    public void removePlayer(final Player player, final boolean check) {
        Logging.getLogger().log(Level.INFO, "[0] {0}: removing player: {1}", new Object[]{this.toString(), player.getName()});
        this.players.remove(player);
        player.teleport(this.arena.getLocation("outside"));
        InventoryManager.restoreInventory(player);
        if (this.status == Status.STARTED) {
            this.arena.updateSigns();
            final Team team = Team.getTeam(player);
            if (team != null) {
                team.getBossBar().removePlayer(player);
                team.removePlayer(player);
                this.updateScoreboard();
                if (team.getPlayers().size() == 0) {
                    this.end(EndReason.TEAM_LEFT, team.getEnemyTeam());
                }
            }
            this.balances.remove(player);
        }else if (this.status == Status.STARTING) {
            player.setWalkSpeed(0.2f);
            if (check) {
                player.sendMessage(Main.prefix + LangConfiguration.getString("game.quit"));
                this.arena.updateSigns();
                final Team team = Team.getTeam(player);
                if (team != null) {
                    team.removePlayer(player);
                    if (team.getPlayers().size() == 0) {
                        this.communicator.broadcastActionBar(LangConfiguration.getString("lobby.not_enough_players"));
                        this.lobby.reset();
                        return;
                    }
                }
                this.lobby.update();
                this.arena.updateSigns();
                if (this.getPlayers().size() <= 0) {
                    this.lobby.reset();
                    return;
                }
                this.arena.updateSigns();
            }
        }else if (this.status == Status.WAITING) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("game.quit"));
            this.arena.updateSigns();
            this.lobby.removePlayer(player);
            this.lobby.update();
            if (this.getPlayers().size() <= 0) {
                this.lobby.cancel();
                this.arena.reset();
                return;
            }
            this.arena.updateSigns();
        }else if (this.status == Status.ENDING && !check) {
            return;
        }
        Logging.getLogger().log(Level.INFO, "[1] {0}: removed player: {1}", new Object[]{this.toString(), player.getName()});
        this.communicator.broadcastMessage(LangConfiguration.getString("game.quit.broadcast").replace("$a", player.getName()).replace("$b", this.players.size() + "").replace("$c", this.arena.getMaxPlayers() * 2 + ""));
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    void requestBalances() {
        final List<String> playerList = this.players.stream().map(Player::getName).collect(Collectors.toList());
        EconomyDatabase.getDatabase().getBalances(playerList, map -> {
            final Iterator<Player> iterator = this.players.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (!map.containsKey(player.getName())) {
                    throw new AssertionError();
                }else{
                    int balance = map.get(player.getName());
                    this.balances.put(player, balance);
                }
            }
        });
    }

    private void updateBalances() {
        final Map<String, Integer> map = this.balances.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getName(), entry -> entry.getValue()));
        EconomyDatabase.getDatabase().setBalances(map);
        Logging.getLogger().info(this + ": balance map: " + map);
    }

    void start() {
        for (Player player : getPlayers()) {
            Main.getMain().getScoreboardManager().setState(player.getUniqueId(), ScoreboardState.IN_GAME);
        }

        Logging.getLogger().log(Level.INFO, "[0] {0}: starting", this.toString());
        if (this.balances.isEmpty()) {
            Logging.getLogger().log(Level.SEVERE, "[0]: balances is empty, stopping");
            this.forceStop();
            return;
        }
        Logging.getLogger().log(Level.INFO, "{0}: balances: {1}", new Object[]{this.toString(), this.balances});
        this.status = Status.STARTED;
        this.updateScoreboard();
        this.timerTask = Main.getMain().getServer().getScheduler().runTaskTimer(Main.getMain(), this::timerTask, 0L, 20L);
        Logging.getLogger().log(Level.INFO, "{0}: timerTask: {1}", new Object[]{this.toString(), this.timerTask});
        this.arena.updateSigns();
        for (final Team team : this.teams) {
            team.getLobbyPlayers().clear();
            team.getQueuedPlayers().clear();
            team.spawnCaptain();
            Logging.getLogger().log(Level.INFO, "{0}: captain spawned: team={1}", new Object[]{this.toString(), team});
            for (final Player player : team.getPlayers()) {
                team.getBossBar().addPlayer(player);
                player.setScoreboard(this.scoreboard);
                player.setWalkSpeed(0.2f);
                this.communicator.sendTitle(player, LangConfiguration.getString("game.started#title"), LangConfiguration.getString("game.started#subtitle").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()), Communicator.TitleLength.MEDIUM);
            }
        }
        Logging.getLogger().log(Level.INFO, "[1] {0}: started", this.toString());
        Bukkit.getScheduler().runTaskLater(Main.getMain(), () -> Utils.fixCaptains(this), 20L);
    }

    public void end(final EndReason reason, Team winner) {
        Logging.getLogger().log(Level.INFO, "[0] {0}: ending reason={1} winner={2}", new Object[]{this, reason, winner});
        this.status = Status.ENDING;
        this.timerTask.cancel();
        switch (reason) {
            case TEAM_KILLED -> {
                final String loser = winner.getEnemyTeam().getType().getPlayerSingular();
                this.communicator.broadcastTitle(LangConfiguration.getString("game.ended").replace("$a", winner.getType().getPrefix()).replace("$b", winner.getType().getName()), LangConfiguration.getString("game.end.team_killed").replace("$", loser), Communicator.TitleLength.LONG);
            }
            case SHIP_DESTROYED -> {
                final String loser = winner.getEnemyTeam().getType().getPlayerPlural();
                winner.getSpawn().getWorld().playSound(winner.getEnemyTeam().getSpawn(), Sound.ENTITY_ENDER_DRAGON_DEATH, 30.0f, 1.0f);
                this.communicator.broadcastTitle(LangConfiguration.getString("game.ended").replace("$a", winner.getType().getPrefix()).replace("$b", winner.getType().getName()), LangConfiguration.getString("game.end.ship_destroyed").replace("$", loser), Communicator.TitleLength.LONG);
                break;
            }
            case TEAM_LEFT -> {
                final String loser = winner.getEnemyTeam().getType().getPlayerSingular();
                this.communicator.broadcastTitle(LangConfiguration.getString("game.ended").replace("$a", winner.getType().getPrefix()).replace("$b", winner.getType().getName()), LangConfiguration.getString("game.end.team_left").replace("$", loser), Communicator.TitleLength.LONG);
                break;
            }
            case TIMES_UP -> {
                this.players.forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 30.0f, 1.0f));
                Bukkit.getScheduler().runTaskLater(Main.getMain(), () -> this.players.forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 30.0f, 1.0f)), 30L);
                for (final Team team : this.teams) {
                    if (team.getCaptain() != null && team.getCaptain().getVillager().getHealth() > 0.0 && team.getEnemyTeam().getCaptain().getVillager().getHealth() == 0.0) {
                        winner = team;
                    }
                }
                if (winner == null) {
                    this.communicator.broadcastTitle(LangConfiguration.getString("game.ended_draw"), LangConfiguration.getString("game.end.times_up"), Communicator.TitleLength.LONG);
                    break;
                }
                final String loser = winner.getEnemyTeam().getType().getPlayerPlural();
                this.communicator.broadcastTitle(LangConfiguration.getString("game.ended").replace("$a", winner.getType().getPrefix()).replace("$b", winner.getType().getPlayerPlural()), LangConfiguration.getString("game.end.times_up_captain_killed").replace("$", loser), Communicator.TitleLength.LONG);
            }
        }
        BukkitTask task = null;
        if (winner != null) {
            task = winner.spawnFireworks();
        }
        final Team finalWinner = winner;
        final BukkitTask finalTask = task;


        for (final Player player2 : this.players) {
            player2.getInventory().setArmorContents(null);
            Score.reward(player2, finalWinner != null && finalWinner.getAllPlayers().contains(player2), this);
        }


        this.updateBalances();
        final int endId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getMain(), () -> {
            while (this.players.size() > 0) {
                Player player3 = this.players.get(0);
                if (finalWinner != null && finalWinner.getAllPlayers().contains(player3)) {
                    finalWinner.removePlayer(player3);
                }
                this.removePlayer(this.players.get(0), false);
            }
            this.arena.reset();
            if (finalTask != null)
                finalTask.cancel(); //added by TeddyBear_2004
            Logging.getLogger().log(Level.INFO, "[1] {0}: ended (2)", this);
        }, 160L);
        Logging.getLogger().log(Level.INFO, "[1] {0}: ended endTaskId={1}(1)", new Object[]{this, endId});

        for (Player player : winner.getPlayers()) {
            Main.getMain().getPlayerData().get(player.getUniqueId()).setWins();
        }

        for (Player player : winner.getEnemyTeam().getPlayers()) {
            Main.getMain().getPlayerData().get(player.getUniqueId()).setLoses();
        }
    }

    public static final Map<UUID, Integer> integerMap = new HashMap<>();

    public boolean killPlayer(final Player player) {
        Logging.getLogger().log(Level.INFO, "[{0}: killPlayer player={1}", new Object[]{this, player.getName()});
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(player.getLocation().add(0.0, 2.0, 0.0));
        final Team team = Team.getTeam(player);
        assert team != null;
        if (team.getCaptain() != null && !team.getCaptain().getVillager().isDead()) {
            new BukkitRunnable() {
                int timeUntilRespawn = Configuration.respawnCooldown;

                public void run() {
                    if (Game.this.status != Status.STARTED || !Game.this.players.contains(player)) {
                        this.cancel();
                        return;
                    }
                    if (this.timeUntilRespawn > 0) {
                        Communicator.TitleLength length;
                        if (this.timeUntilRespawn == Configuration.respawnCooldown) {
                            length = Communicator.TitleLength.ONLY_FADE_IN;
                        }else if (this.timeUntilRespawn == 1) {
                            length = Communicator.TitleLength.ONLY_FADE_OUT;
                        }else{
                            length = Communicator.TitleLength.ONLY_STAY;
                        }
                        Game.this.communicator.sendTitle(player, LangConfiguration.getString("game.death.respawn_cooldown#title").replace("$", this.timeUntilRespawn + ""), LangConfiguration.getString("game.death.respawn_cooldown#subtitle"), length);
                        --this.timeUntilRespawn;
                    } else {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.teleport(team.getSpawn());
                        InventoryManager.setGameInventory(player, team.getType());
                        integerMap.put(player.getUniqueId(), 5);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Main.getMain(), 0L, 20L);
            return false;
        }
        if (team.getPlayers().size() - 1 > 0) {
            this.communicator.broadcastMessage(LangConfiguration.getString("game.death.eliminated").replace("$", player.getName()), team);
            this.communicator.sendTitle(player, LangConfiguration.getString("game.death.eliminated#title"), LangConfiguration.getString("game.death.eliminated#subtitle"), Communicator.TitleLength.MEDIUM);
            team.eliminate(player);
            this.updateScoreboard();
            return false;
        }
        this.end(EndReason.TEAM_KILLED, team.getEnemyTeam());
        return true;
    }

    public void updateScoreboard() {
        Bukkit.getScheduler().runTask(Main.getMain(), this::updateScoreboard2);
    }

    private void updateScoreboard2() {
        if (this.scoreboard.getEntries() != null) {
            for (final String entry : this.scoreboard.getEntries()) {
                if (this.sidebarObjective.getScore(entry).isScoreSet()) {
                    this.scoreboard.resetScores(entry);
                }
            }
        }
        final String time = this.remainingTime.format(this.formatter);
        this.getSidebarObjective().getScore(LangConfiguration.getString("scoreboard.remaining_time").replace("$", time)).setScore(8);
        this.navy.updateScoreboard(3);
        this.pirates.updateScoreboard(7);
    }

    private void timerTask() {
        this.remainingTime = this.remainingTime.minusSeconds(1L);
        if (this.remainingTime.getMinute() == 0 && this.remainingTime.getSecond() == 0) {
            this.end(EndReason.TIMES_UP, null);
        }
        this.updateScoreboard2();
    }

    void forceStop() {
        Logging.getLogger().log(Level.INFO, "{0}: FORCE STOPPING", this);
        this.status = Status.ENDING;
        this.communicator.broadcastMessage(LangConfiguration.getString("game.force_stop"));
        this.communicator.broadcastTitle(LangConfiguration.getString("game.force_stop_title"), null, Communicator.TitleLength.MEDIUM);
        while (this.players.size() > 0) {
            this.removePlayer(this.players.get(0), false);
        }
        this.arena.reset();
    }

    public void respawnProtection() {
        Bukkit.getScheduler().runTaskTimer(Main.getMain(), () -> {
            for (Player player : players) {
                int time = integerMap.getOrDefault(player.getUniqueId(), 0);
                if (time == 0) {
                    integerMap.remove(player.getUniqueId());
                    continue;
                }
                integerMap.put(player.getUniqueId(), time - 1);
            }
        },0 ,20);
    }

    public static boolean joinPlayer(final Player player, final String arenaName, final Location signLocation) {
        Logging.getLogger().log(Level.INFO, "[0] {0}: static joining player={1} arenaName={2} signLocation={3}", new Object[]{player, arenaName, signLocation});
        if (!player.hasPermission("shipbattle.play")) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.no_permission"));
            Logging.getLogger().log(Level.INFO, "[1] {0}: static joining player={1} arenaName={2} signLocation={3} fail=no_permission", new Object[]{player, arenaName, signLocation});
            return false;
        }
        if (getGame(player) != null) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.join.error.already_in_game"));
            Logging.getLogger().log(Level.INFO, "[1] {0}: static joining player={1} arenaName={2} signLocation={3} fail=already_in_game", new Object[]{player, arenaName, signLocation});
            return false;
        }
        final Arena arena = ArenaConfiguration.getArena(arenaName);
        if (arena == null) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.join.error.arena_not_found"));
            Logging.getLogger().log(Level.INFO, "[1] {0}: static joining player={1} arenaName={2} signLocation={3} fail=arena_not_found", new Object[]{player, arenaName, signLocation});
            return false;
        }
        if (!arena.isEnabled()) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.join.error.disabled"));
            Logging.getLogger().log(Level.INFO, "[1] {0}: static joining player={1} arenaName={2} signLocation={3} fail=disabled", new Object[]{player, arenaName, signLocation});
            return false;
        }
        if (!Main.getMain().isAwake()) {
            Main.getMain().awake();
        }
        if (signLocation != null && !arena.getSigns().contains(signLocation)) {
            return false;
        }
        if (arena.getGame() == null) {
            arena.initGame();
        }
        final Game game = arena.getGame();
        if (game.status == Status.WAITING && game.players.size() < arena.getMaxPlayers() * 2) {
            game.addPlayer(player);
            return true;
        }
        player.sendMessage(Main.prefix + LangConfiguration.getString("commands.join.error.already_started"));
        Logging.getLogger().log(Level.INFO, "[1] {0}: static joining player={1} arenaName={2} signLocation={3} fail=already_started", new Object[]{player, arenaName, signLocation});
        return false;
    }

    public static Game getGame(final Player player) {
        for (final Game game : Main.games) {
            if (game.players.contains(player)) {
                return game;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Game@%d[arena=%s, status=%s]", this.hashCode(), this.arena.getName(), this.status);
    }

    public enum Status {
        WAITING,
        STARTED,
        STARTING,
        ENDING;
    }

    public enum EndReason {
        TEAM_KILLED,
        SHIP_DESTROYED,
        TEAM_LEFT,
        TIMES_UP;
    }
}
