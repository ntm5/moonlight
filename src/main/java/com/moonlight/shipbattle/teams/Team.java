package com.moonlight.shipbattle.teams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.moonlight.shipbattle.Game;
import com.moonlight.shipbattle.InventoryManager;
import com.moonlight.shipbattle.Lobby;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;

public class Team
{
    public static final Set<Team> teams;
    private Team enemyTeam;
    private final Game game;
    private final Set<Player> players;
    private final List<Player> lobbyPlayers;
    private final List<Player> queuedPlayers;
    private final Set<Player> eliminatedPlayers;
    private final BossBar bossBar;
    private final TeamType type;
    private final org.bukkit.scoreboard.Team scoreboardTeam;
    private Captain captain;
    private Chunk captainChunk;
    private int destroyedBlocks;
    
	public Team(final TeamType type, final Game game) {
        this.players = new HashSet<Player>();
        this.lobbyPlayers = new ArrayList<Player>();
        this.queuedPlayers = new ArrayList<Player>();
        this.eliminatedPlayers = new HashSet<Player>();
        this.destroyedBlocks = 0;
        this.game = game;
        this.type = type;
        this.bossBar = Bukkit.createBossBar(type.getPrefix() + type.getName().substring(2), BarColor.GREEN, BarStyle.SEGMENTED_20, new BarFlag[0]);
        (this.scoreboardTeam = game.getScoreboard().registerNewTeam(type.toString())).setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        this.scoreboardTeam.setAllowFriendlyFire(false);
        this.scoreboardTeam.setPrefix(type.getPrefix());
        Team.teams.add(this);
    }
    
    public void spawnCaptain() {
        final Location location = this.game.getArena().getLocation(this.type.toString() + ".captain_spawn");
        this.captainChunk = location.getChunk();
        this.captain = new Captain(location, this);
    }
    
    public BossBar getBossBar() {
        return this.bossBar;
    }
    
    public void setEnemyTeam(final Team enemyTeam) {
        this.enemyTeam = enemyTeam;
    }
    
    public Set<Player> getPlayers() {
        return this.players;
    }
    
	public void addPlayer(final Player player) {
        this.players.add(player);
        this.scoreboardTeam.addEntry(player.getName());
        InventoryManager.setGameInventory(player, this.type);
        this.scoreboardTeam.setColor(this.type == TeamType.NAVY ? ChatColor.GREEN : ChatColor.RED);
    }
    
	public void removePlayer(final Player player) {
        this.players.remove(player);
    }
    
    public void eliminate(final Player player) {
        this.removePlayer(player);
        this.eliminatedPlayers.add(player);
    }
    
    private Set<Player> getEliminatedPlayers() {
        return this.eliminatedPlayers;
    }
    
    public Set<Player> getAllPlayers() {
        final Set<Player> players = new HashSet<Player>();
        players.addAll(this.players);
        players.addAll(this.eliminatedPlayers);
        return players;
    }
    
    public Captain getCaptain() {
        return this.captain;
    }
    
    public Chunk getCaptainChunk() {
        return this.captainChunk;
    }
    
    public Location getSpawn() {
        return this.game.getArena().getLocation(this.type.toString() + ".spawn");
    }
    
    public Location[] getArea() {
        final Location minPoint = this.game.getArena().getLocation(this.type.toString() + ".area.min_point");
        final Location maxPoint = this.game.getArena().getLocation(this.type.toString() + ".area.max_point");
        return new Location[] { minPoint, maxPoint };
    }
    
    public TeamType getType() {
        return this.type;
    }
    
    public Game getGame() {
        return this.game;
    }
    
    public Team getEnemyTeam() {
        return this.enemyTeam;
    }
    
    public void addDestroyedBlocks(final int destroyedBlocks) {
        this.destroyedBlocks += destroyedBlocks;
        if (this.destroyedBlocks >= this.getGame().getArena().getShipHealths()) {
            this.game.end(Game.EndReason.SHIP_DESTROYED, this);
        }
        this.game.updateScoreboard();
    }
    
    public void updateScoreboard(int entries) {
        this.game.getSidebarObjective().getScore(LangConfiguration.getString("scoreboard.team_header").replace("$a", this.type.getPrefix()).replace("$b", this.type.getName())).setScore(entries);
        --entries;
        final String alivePlayers = LangConfiguration.getString("scoreboard.alive_players").replace("$a", this.type.getPrefix()).replace("$b", this.players.size() + "");
        this.game.getSidebarObjective().getScore(alivePlayers).setScore(entries);
        --entries;
        String captainHealthPercentageS;
        if (this.captain != null) {
            if (!this.captain.getVillager().isDead()) {
                final int captainHealthPercentage = (int)Math.round(this.captain.getVillager().getHealth() / Configuration.captainHealth * 100.0);
                captainHealthPercentageS = Utils.getPercentageColor(captainHealthPercentage) + captainHealthPercentage + "%";
            }
            else {
                captainHealthPercentageS = "§c\u271d";
            }
        }
        else {
            captainHealthPercentageS = "§a100%";
        }
        final String captainHealth = LangConfiguration.getString("scoreboard.captain_hp").replace("$a", this.type.getPrefix()).replace("$b", captainHealthPercentageS);
        this.game.getSidebarObjective().getScore(captainHealth).setScore(entries);
        --entries;
        final int shipHealths = this.game.getArena().getShipHealths();
        final int shipHealthPercentage = (int)Math.round((shipHealths - this.getEnemyTeam().destroyedBlocks) / (double)shipHealths * 100.0);
        final String shipHealthPercentageS = (shipHealthPercentage > 0) ? (Utils.getPercentageColor(shipHealthPercentage) + shipHealthPercentage + "%") : "§c-";
        final String shipHealth = LangConfiguration.getString("scoreboard.ship_hp").replace("$a", this.type.getPrefix()).replace("$b", shipHealthPercentageS);
        this.game.getSidebarObjective().getScore(shipHealth).setScore(entries);
    }
    
	public ItemStack getJoinItemStack(final int maxPlayers) {
        final ItemStack itemStack = (this.type == TeamType.NAVY) ? Lobby.navyItemStack : Lobby.piratesItemStack;
        if (this.lobbyPlayers.isEmpty()) {
            itemStack.setAmount(1);
        }
        else {
            itemStack.setAmount(this.lobbyPlayers.size());
        }
        final ItemMeta meta = itemStack.getItemMeta();
        final List<String> lores = Arrays.asList(LangConfiguration.getString("lobby.join.info").replace("$a", this.lobbyPlayers.size() + "").replace("$b", maxPlayers + ""), "", LangConfiguration.getString("lobby.join.click"));
        meta.setLore(lores);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    
    public BukkitTask spawnFireworks() {
        final Location minPoint = this.getArea()[0];
        final Location maxPoint = this.getArea()[1];
        final World world = minPoint.getWorld();
        final double midX = (minPoint.getX() + maxPoint.getX()) / 2.0;
        final double midY = (minPoint.getY() + maxPoint.getY()) / 2.0;
        final double midZ = (minPoint.getZ() + maxPoint.getZ()) / 2.0;
        return Bukkit.getScheduler().runTaskTimer(Main.getMain(), () -> {
        	Random random = new Random();
        	double modX = (random.nextInt(60) - 30.0) / 10.0;
        	double modY = random.nextInt(4) + 1;
        	double modZ = (random.nextInt(60) - 30.0) / 10.0;
        	double x = midX + modX;
        	double y = midY + modY;
        	double z = midZ + modZ;
        	Firework firework = (Firework) world.spawnEntity(new Location(world, x, y, z), EntityType.FIREWORK);
        	FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(Utils.getFirework(this.type));
            meta.setPower(0);
            firework.setFireworkMeta(meta);
        }, 0L, 15L);
    }
    
    public List<Player> getLobbyPlayers() {
        return this.lobbyPlayers;
    }
    
    public List<Player> getQueuedPlayers() {
        return this.queuedPlayers;
    }
    
    @Override
    public String toString() {
        return String.format("Team@%d[game=%s, type=%s]", this.hashCode(), this.game, this.type.toString());
    }
    
    public static Team getTeam(final Player player) {
        for (final Team team : Team.teams) {
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }
        return null;
    }
    
    public static Team getTeamEliminated(final Player player) {
        for (final Team team : Team.teams) {
            if (team.getPlayers().contains(player) || team.getEliminatedPlayers().contains(player)) {
                return team;
            }
        }
        return null;
    }
    
    static {
        teams = new HashSet<Team>();
    }
}
