package com.moonlight.shipbattle.listeners.game;

import com.moonlight.shipbattle.Game;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Supply;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.cannon.Cannon;
import com.moonlight.shipbattle.cannon.ShootSession;
import com.moonlight.shipbattle.cannon.projectile.ProjectileType;
import com.moonlight.shipbattle.configuration.ItemConfiguration;
import com.moonlight.shipbattle.database.BalanceReceivedListener;
import com.moonlight.shipbattle.database.EmeraldTask;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.shop.Shop;
import com.moonlight.shipbattle.teams.Team;
import org.bukkit.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventPriority;
import org.bukkit.block.Block;
import com.moonlight.shipbattle.configuration.Configuration;

import com.moonlight.shipbattle.configuration.LangConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.bukkit.event.Listener;

public class PlayerListener implements Listener
{
    private static PlayerListener instance;
    
    public PlayerListener() {
        Logging.getLogger().log(Level.INFO, "init listener", this);
        PlayerListener.instance = this;
    }
    
    public static PlayerListener getInstance() {
        return PlayerListener.instance;
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Game game = Game.getGame(player);
        if (game != null) {
            game.removePlayer(player, true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Game game = Game.getGame(player);
        if (game == null) {
            return;
        }
        if (game.getStatus() == Game.Status.WAITING) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
                player.openInventory(game.getLobby().getInventory());
            }
        } else if (game.hasStarted()) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getInventory().getItemInMainHand().getType() == Material.WATER_BUCKET) {
                event.setCancelled(true);
                final Block block = player.getTargetBlock(null, 3);
                game.getArena().getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                for (int dx = -1; dx <= 1; ++dx) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        for (int dy = -1; dy <= 1; ++dy) {
                            final Location location1 = block.getLocation().add((double)dx, (double)dz, (double)dy);
                            if (location1.getBlock().getType() == Material.FIRE) {
                                location1.getBlock().setType(Material.AIR);
                                game.getArena().getWorld().spawnParticle(Particle.WATER_DROP, location1, 100, 0.2, 0.2, 0.2);
                            }
                        }
                    }
                }
                player.getInventory().getItemInMainHand().setType(Material.BUCKET);
                player.updateInventory();
            }
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Utils.isButton(event.getClickedBlock().getType())) {
                    final Cannon cannon = new Cannon(game, event.getClickedBlock().getLocation().subtract(0.0, 1.0, 0.0));
                    if (!cannon.update()) {
                        return;
                    }
                    if (cannon.getTeam() != Team.getTeam(player)) {
                        return;
                    }
                    event.setCancelled(true);
                    final ShootSession shootSession = new ShootSession(player, cannon);
                    ShootSession.getMap().put(player, shootSession);
                    player.sendMessage(Main.prefix + LangConfiguration.getString("cannon.shoot"));
                } else if (event.getClickedBlock().getType() == Material.CAULDRON && player.getInventory().getItemInMainHand().getType() == Material.BUCKET) {
                    event.setCancelled(true);
                    player.getInventory().getItemInMainHand().setType(Material.WATER_BUCKET);
                    player.updateInventory();
                } else if ((event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST) && player.getGameMode() != GameMode.SPECTATOR) {
                    event.setCancelled(true);
                    if (game.getLootTimes().containsKey(player)) {
                        final long time = System.currentTimeMillis() - game.getLootTimes().get(player);
                        if (time < Configuration.supplyCooldown) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("supply.wait").replace("$", (Configuration.supplyCooldown - time) / 1000L + ""));
                            return;
                        }
                    }
                    Supply.getInstance().openInventory(player);
                    game.getLootTimes().put(player, System.currentTimeMillis());
                }
            } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                final ShootSession shootSession2 = ShootSession.getMap().get(player);
                if (shootSession2 != null) {
                    shootSession2.fire();
                    ShootSession.getMap().remove(player);
                    game.getCommunicator().sendActionBar(player, "");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        final Player player = event.getPlayer();
        final Team playerTeam = Team.getTeam(player);
        if (playerTeam != null) {
            if (entity instanceof Villager) {
                final Villager villager = (Villager)entity;
                for (final Team team : Team.teams) {
                    if (team.getCaptain() != null && team.getCaptain().getVillager().equals(villager)) {
                        event.setCancelled(true);
                        if (team == playerTeam) {
                            Shop.openShopInventory(player);
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.balance").replace("$", team.getGame().getBalances().get(player).toString()));
                        }
                    }
                }
            }
            else if (entity instanceof ItemFrame) {
                for (final ProjectileType type : ProjectileType.getList()) {
                    if (player.getInventory().getItemInMainHand().getType() == type.getItemStack().getType()) {
                        if (!Utils.isInArea(entity.getLocation(), playerTeam.getGame().getArena().getWorld(), playerTeam.getArea()[0], playerTeam.getArea()[1])) {
                            event.setCancelled(true);
                        }
                        return;
                    }
                }
                event.setCancelled(true);
            }
        }
    }
    
	@EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Team team = Team.getTeamEliminated(player);
        if (team != null) {
            event.setCancelled(true);
            if (team.getPlayers().contains(player)) {
                if (event.getMessage().startsWith("!")) {
                    for (final Player gamePlayer : team.getGame().getPlayers()) {
                        gamePlayer.sendMessage(LangConfiguration.getString("event.chat.message.global").replace("$a", team.getType().getPrefix()).replace("$b", player.getName()).replace("$c", event.getMessage().substring(1)));
                    }
                }
                else {
                    for (final Player teamPlayer : team.getPlayers()) {
                        teamPlayer.sendMessage(LangConfiguration.getString("event.chat.message").replace("$a", team.getType().getPrefix()).replace("$b", player.getName()).replace("$c", event.getMessage()));
                    }
                }
            }
            else if (team.getAllPlayers().contains(player)) {
                for (final Player gamePlayer : team.getGame().getPlayers()) {
                    gamePlayer.sendMessage(LangConfiguration.getString("event.chat.message.eliminated").replace("$a", team.getType().getPrefix()).replace("$b", player.getName()).replace("$c", event.getMessage()));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerExecuteCommand(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (Game.getGame(player) != null && !player.hasPermission("shipbattle.admin")) {
            final String command = event.getMessage().split(" ")[0];
            if (!command.equalsIgnoreCase("/sb")) {
                event.setCancelled(true);
                player.sendMessage(Main.prefix + LangConfiguration.getString("event.command_not_allowed"));
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (Game.getGame(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        List<Material> mats = new ArrayList<>();
        for (ProjectileType type : ProjectileType.getList()) {
            if (type.getItemStack().getType() == event.getBlock().getType())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Game.getGame(event.getEntity()).kills.putIfAbsent(event.getEntity().getKiller(), Game.getGame(event.getEntity()).kills.get(event.getEntity()) == null ? 1 : Game.getGame(event.getEntity()).kills.get(event.getEntity()) + 1);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) throws SQLException {
        if (!event.getPlayer().hasPlayedBefore()) {
            new EmeraldTask().setBalance(event.getPlayer().getName(), 0);
        }
    }
}
