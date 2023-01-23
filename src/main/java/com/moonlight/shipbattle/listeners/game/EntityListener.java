package com.moonlight.shipbattle.listeners.game;

import com.moonlight.shipbattle.*;
import com.moonlight.shipbattle.cannon.projectile.ProjectileEntity;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.teams.Team;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EntityListener implements Listener {
    private static EntityListener instance;

    public EntityListener() {
        Logging.getLogger().log(Level.INFO, "init listener", this);
        EntityListener.instance = this;
    }

    public static EntityListener getInstance() {
        return EntityListener.instance;
    }

    @EventHandler
    public void onShoot(final EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (player.getGameMode() == GameMode.SPECTATOR) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleEnter(final VehicleEnterEvent event) {
        if (event.getEntered() instanceof Villager) {
            final Villager villager = (Villager) event.getEntered();
            for (final Team team : Team.teams) {
                if (team.getCaptain() != null && team.getCaptain().getVillager() == villager) {
                    event.setCancelled(true);
                    villager.teleport(team.getGame().getArena().getLocation(team.getType().toString() + ".captain_spawn"));
                }
            }
        }
    }

    @EventHandler
    public void a(final BlockExplodeEvent event) { // no idea if this is needed or not (sus name)
        if (event.getBlock().getType() == Material.ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final Game game = Game.getGame(player);
            if (game != null) {
                if (game.getStatus() == Game.Status.STARTED) {
                    final Team team = Team.getTeam(player);
                    assert team != null;
                    Player damager = null;
                    if (event instanceof EntityDamageByEntityEvent) {
                        final EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                        if (byEntityEvent.getDamager() instanceof ProjectileEntity) {
                            damager = ((ProjectileEntity) byEntityEvent.getDamager()).getShooter();
                        }else if (byEntityEvent.getDamager() instanceof Player) {
                            damager = (Player) byEntityEvent.getDamager();
                        }else if (byEntityEvent.getDamager() instanceof Projectile) {
                            final ProjectileSource shooter = ((Projectile) byEntityEvent.getDamager()).getShooter();
                            if (shooter instanceof Player) {
                                damager = (Player) shooter;
                            }
                        }
                    }
                    final Team damagerTeam = Team.getTeam(damager);
                    if (damager != null && team == damagerTeam) {
                        event.setCancelled(true);
                        return;
                    }
                    if (player.getHealth() - event.getFinalDamage() <= 0.0) {
                        if (damager != null && damagerTeam != null) {
                            game.getCommunicator().broadcastMessage(LangConfiguration.getString("event.damage.player_killed_by_player").replace("$a", team.getType().getPrefix()).replace("$b", player.getName()).replace("$c", damagerTeam.getType().getPrefix()).replace("$d", damager.getName()));
                            Score.getScore(damager).addKilledPlayer();
                            game.getCommunicator().sendNotification(damager, LangConfiguration.getString("notification.kill"));
                            if (!game.isFirstBloodDrawn()) {
                                Score.getScore(damager).addExtraReward(Score.RewardEntry.FIRST_BLOOD);
                                game.getCommunicator().sendNotification(damager, LangConfiguration.getString("notification.special.first_blood"));
                                game.setFirstBloodDrawn();
                            }
                        }else{
                            game.getCommunicator().broadcastMessage(LangConfiguration.getString("event.damage.player_died").replace("$a", team.getType().getPrefix()).replace("$b", player.getName()));
                        }
                        event.setCancelled(true);
                        game.killPlayer(player);
                    }
                }else{
                    event.setCancelled(true);
                }
            }
        }else if (event.getEntity() instanceof Villager) {
            final Villager villager = (Villager) event.getEntity();
            for (final Team team : Team.teams) {
                if (team.getCaptain() != null && team.getCaptain().getVillager() != villager) {
                    continue;
                }
                if (team.getGame().getStatus() != Game.Status.STARTED) {
                    event.setCancelled(true);
                    return;
                }
                Player damager = null;
                if (event instanceof EntityDamageByEntityEvent) {
                    final EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                    if (byEntityEvent.getDamager() instanceof ProjectileEntity) {
                        damager = ((ProjectileEntity) byEntityEvent.getDamager()).getShooter();
                    }else if (byEntityEvent.getDamager() instanceof Player) {
                        damager = (Player) byEntityEvent.getDamager();
                    }else if (byEntityEvent.getDamager() instanceof Projectile) {
                        final ProjectileSource shooter = ((Projectile) byEntityEvent.getDamager()).getShooter();
                        if (shooter instanceof Player) {
                            damager = (Player) shooter;
                        }
                    }
                }
                if (damager != null && team == Team.getTeam(damager)) {
                    team.getGame().getCommunicator().sendActionBar(damager, LangConfiguration.getString("event.damage.own_captain"));
                    event.setCancelled(true);
                    return;
                }
                final Game game2 = team.getGame();
                if (villager.getHealth() - event.getFinalDamage() <= 0.0) {
                    if (damager != null) {
                        game2.getCommunicator().broadcastMessage(LangConfiguration.getString("event.damage.captain_killed_by_player").replace("$a", team.getEnemyTeam().getType().getPrefix()).replace("$b", damager.getName()).replace("$c", team.getType().getPrefix()).replace("$d", team.getType().getPlayerPlural()));
                        Score.getScore(damager).addExtraReward(Score.RewardEntry.CAPTAIN_KILL);
                        game2.getCommunicator().sendNotification(damager, LangConfiguration.getString("notification.special.captain_kill"));
                    }
                    game2.getCommunicator().broadcastTitle(LangConfiguration.getString("event.damage.captain_died#title").replace("$a", team.getType().getPrefix()).replace("$b", team.getType().getPlayerPlural()), LangConfiguration.getString("event.damage.captain_died#subtitle").replace("$", team.getType().getPlayerPlural()), Communicator.TitleLength.MEDIUM);
                    game2.getArena().getWorld().playSound(villager.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 10.0f, 1.0f);
                    team.getBossBar().setProgress(0.0);
                }else if (damager != null) {
                    final int healthPercentage = (int) Math.round((villager.getHealth() - event.getFinalDamage()) / Configuration.captainHealth * 100.0);
                    game2.getCommunicator().sendActionBar(damager, Utils.getPercentageColor(healthPercentage) + healthPercentage + "%");
                    game2.getCommunicator().broadcastActionBar(LangConfiguration.getString("event.damage.captain_under_attack"), team);
                    team.getBossBar().setProgress((villager.getHealth() - event.getFinalDamage()) / Configuration.captainHealth);
                }
                team.getBossBar().setColor(BarColor.WHITE);
                Bukkit.getScheduler().runTaskLater((Plugin) Main.getMain(), () -> {
                    BarColor color = Utils.getPercentageBossBarColor(team.getCaptain().getVillager().getHealth() / Configuration.captainHealth);
                    team.getBossBar().setColor(color);
                    return;
                }, 2L);
                game2.updateScoreboard();
            }
        }
    }

    @EventHandler
    public void onHangingBreak(final HangingBreakEvent event) {
        final Game game = Utils.getGameContains(event.getEntity().getLocation());
        if (game != null && (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY || event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        final Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
        Team team = null;
        if (nmsEntity instanceof ProjectileEntity) {
            final ProjectileEntity projectile = (ProjectileEntity) nmsEntity;
            final Game game = projectile.getShooterTeam().getGame();
            if (!game.hasStarted()) {
                return;
            }
            event.setCancelled(false);
            final Player shooter = projectile.getShooter();
            Score.getScore(shooter).addDestroyedBlocks(event.blockList().size());
            game.getCommunicator().sendNotification(shooter, LangConfiguration.getString("notification.destroyed_blocks").replace("$", event.blockList().size() + ""));
            team = projectile.getShooterTeam().getEnemyTeam();
        }else{
            for (final Team t : Team.teams) {
                if (Utils.isInArea(event.getLocation(), t.getGame().getArena().getWorld(), t.getArea()[0], t.getArea()[1])) {
                    team = t;
                    break;
                }
            }
        }
        if (team != null) {
            final Iterator<Block> iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                final Material material = iterator.next().getType();
                if (Utils.isButton(material)) {
                    iterator.remove();
                }
            }
            event.setYield(0.0f);
            final Team finalTeam = team;
            final int destroyedBlocks = event.blockList().stream().filter(block -> Utils.isInArea(block.getLocation(), finalTeam.getGame().getArena().getWorld(), finalTeam.getArea()[0], finalTeam.getArea()[1])).collect(Collectors.toSet()).size();
            team.getEnemyTeam().addDestroyedBlocks(destroyedBlocks);
        }
    }

    @EventHandler
    public void onVehicleExit(final VehicleExitEvent event) {
        if (event.getVehicle().getType() == EntityType.BOAT && event.getExited() instanceof Player) {
            final Player player = (Player) event.getExited();
            final Game game = Game.getGame(player);
            if (game != null && game.hasStarted()) {
                Bukkit.getScheduler().runTask((Plugin) Main.getMain(), () -> {
                    if (game.hasStarted()) {
                        player.teleport(event.getVehicle().getLocation().add(0.0, 1.0, 0.0));
                    }
                });
            }
        }
    }

    public static void waterInteractTask() {
        for (final Team team : Team.teams) {
            final Game game = team.getGame();
            if (game.getStatus() != Game.Status.STARTED) {
                continue;
            }
            for (final Player player : team.getPlayers()) {
                if (player.getGameMode() != GameMode.SPECTATOR
                        && (player.getLocation().getBlock().getType() == Material.WATER
                        || player.getLocation().getBlock().getType() == Material.LEGACY_STATIONARY_WATER)
                        && (player.getVehicle() == null
                        || player.getVehicle().getType() != EntityType.BOAT)
                        && player.getPotionEffect(PotionEffectType.WATER_BREATHING) == null) { //added check to stop players from taking damage in water if they have the potion effect by TeddyBear_2004 on 2022.07.19

                    if (player.getHealth() <= 4.0) {
                        game.getCommunicator().broadcastMessage(LangConfiguration.getString("event.damage.player_died_toxic_water").replace("$", team.getType().getPrefix() + player.getName()));
                        if (game.killPlayer(player)) {
                            break;
                        }
                        continue;
                    }else{
                        player.damage(4.0);
                    }
                }
            }
        }
    }
}
