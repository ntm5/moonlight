package com.moonlight.shipbattle.listeners.game;

import com.moonlight.shipbattle.Arena;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.teams.Team;
import com.moonlight.shipbattle.teams.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R2.block.impl.CraftLadder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collection;
import java.util.logging.Level;

public class WorldEventListener implements Listener {
    private static WorldEventListener instance;

    public WorldEventListener() {
        Logging.getLogger().log(Level.INFO, "init listener", this);
        WorldEventListener.instance = this;
    }

    public static WorldEventListener getInstance() {
        return WorldEventListener.instance;
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        for (final Team team : Team.teams) {
            if (event.getChunk() == team.getCaptainChunk()) {
                Logging.getLogger().log(Level.INFO, "chunk unload cancelled");
                event.getChunk().setForceLoaded(false);
            }
        }
    }

    //changed methods by TeddyBear_2004 started here
    @EventHandler
    public void onPlayerBlockBreak(final BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.LADDER) {
            event.setCancelled(true);
            return;
        }
        for (final Team team : Team.teams) {
            if (!team.getGame().hasStarted() || !team.getPlayers().contains(event.getPlayer())) {
                continue;
            }

            Location minPoint = null;
            Location maxPoint = null;

            if (team.getType() == TeamType.NAVY) {
                minPoint = team.getGame().getArena().getLocation("navy.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("navy.area.max_point");
            }else if (team.getType() == TeamType.PIRATES) {
                minPoint = team.getGame().getArena().getLocation("pirates.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("pirates.area.max_point");
            }

            if (!Utils.isInArea(event.getBlock().getLocation(), team.getGame().getArena().getWorld(), minPoint, maxPoint))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!(event instanceof HangingBreakByEntityEvent))
            event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();

        if (!(remover instanceof Player)) {
            return;
        }

        for (final Team team : Team.teams) {
            if (!team.getGame().hasStarted() || !team.getPlayers().contains(remover)) {
                continue;
            }

            Location minPoint = null;
            Location maxPoint = null;

            if (team.getType() == TeamType.NAVY) {
                minPoint = team.getGame().getArena().getLocation("navy.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("navy.area.max_point");
            }else if (team.getType() == TeamType.PIRATES) {
                minPoint = team.getGame().getArena().getLocation("pirates.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("pirates.area.max_point");
            }
            event.setCancelled(!Utils.isInArea(event.getEntity().getLocation(), team.getGame().getArena().getWorld(), minPoint, maxPoint));
        }
    }


    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Material type = event.getBlock().getType();

        if (type == Material.LADDER
                /*|| type == Configuration.cannonBaseBlockMaterial
                || Utils.isButton(type)*/) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.LADDER) {
            BlockFace facing = ((CraftLadder) event.getBlock().getBlockData()).getFacing();

            Bukkit.getScheduler().runTaskLater(Main.getMain(), () -> {
                event.getBlock().setType(Material.LADDER, false);
                CraftLadder blockData = (CraftLadder) event.getBlock().getBlockData();
                blockData.setFacing(facing);
                event.getBlock().setBlockData(blockData, false);
            }, 1);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        Collection<Entity> nearbyEntities = event.getBlock().getLocation().getWorld().getNearbyEntities(event.getBlock().getLocation(), 1, 1, 1);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof ItemFrame) {
                Location location = entity.getLocation();
                System.out.println(location);
                int yaw = (int) location.getYaw();

                if (yaw == 270)
                    location.setX(Math.floor(location.getX() + 0.5));
                else location.setX(Math.floor(location.getX() - 0.5));

                if (yaw == 0)
                    location.setZ(Math.floor(location.getZ() + 0.5));
                else
                    location.setZ(Math.floor(location.getZ() - 0.5));

                location.setY(Math.floor(location.getY()));
                location.setYaw(0);
                location.setPitch(0);

                System.out.println(location);
                System.out.println(event.getBlock().getLocation());
                if (location.equals(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        for (final Team team : Team.teams) {
            if (!team.getGame().hasStarted() || !team.getPlayers().contains(event.getPlayer())) {
                continue;
            }

            Location minPoint = null;
            Location maxPoint = null;

            if (team.getType() == TeamType.NAVY) {
                minPoint = team.getGame().getArena().getLocation("navy.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("navy.area.max_point");
            }else if (team.getType() == TeamType.PIRATES) {
                minPoint = team.getGame().getArena().getLocation("pirates.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("pirates.area.max_point");
            }

            if (!Utils.isInArea(event.getBlock().getLocation(), team.getGame().getArena().getWorld(), minPoint, maxPoint))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        for (final Team team : Team.teams) {
            if (!team.getGame().hasStarted() || !team.getPlayers().contains(event.getPlayer())) {
                continue;
            }

            Location minPoint = null;
            Location maxPoint = null;

            if (team.getType() == TeamType.NAVY) {
                minPoint = team.getGame().getArena().getLocation("navy.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("navy.area.max_point");
            }else if (team.getType() == TeamType.PIRATES) {
                minPoint = team.getGame().getArena().getLocation("pirates.area.min_point");
                maxPoint = team.getGame().getArena().getLocation("pirates.area.max_point");
            }

            if (!Utils.isInArea(event.getEntity().getLocation(), team.getGame().getArena().getWorld(), minPoint, maxPoint)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(final BlockBurnEvent event) {
        final Location location = event.getBlock().getLocation();

        if (location.getBlock().getType() == Material.LADDER
                /*|| Utils.isButton(location.getBlock().getType())
                || location.getBlock().getType() == Configuration.cannonBaseBlockMaterial*/) {
            event.setCancelled(true);
            return;
        }

        for (final Team team : Team.teams) {
            if (!team.getGame().hasStarted()) {
                continue;
            }
            final Arena arena = team.getGame().getArena();
            final Location[] area = team.getArea();
            if (Utils.isInArea(location, arena.getWorld(), area[0], area[1])) {
                team.getEnemyTeam().addDestroyedBlocks(1);
            }
        }
    }
    //Method changes ending here
}
