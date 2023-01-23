package com.moonlight.shipbattle.cannon;

import com.moonlight.shipbattle.Game;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.cannon.projectile.ProjectileType;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.teams.Team;
import org.bukkit.entity.ItemFrame;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;

public class Cannon
{
    private final Game game;
    private final Location baseLocation;
    private BlockFace face;
    private ItemFrame frame;
    private Team team;
    
    public Cannon(final Game game, final Location baseLocation) {
        this.game = game;
        this.baseLocation = baseLocation;
    }
    
    public boolean update() {
        if (!this.baseLocation.getWorld().getName().equals(this.game.getArena().getWorld().getName()) || this.baseLocation.getBlock().getType() != Configuration.cannonBaseBlockMaterial) {
            return false;
        }
        for (final Entity entity : this.baseLocation.getWorld().getNearbyEntities(this.baseLocation, 2.0, 1.0, 2.0)) {
            if (entity instanceof ItemFrame) {
                final ItemFrame frame = (ItemFrame)entity;
                if (frame.getItem().getType() != Material.AIR) {
                    this.frame = frame;
                    this.face = frame.getFacing().getOppositeFace();
                    if (Utils.isInArea(this.baseLocation, this.game.getArena().getWorld(), this.game.navy.getArea()[0], this.game.navy.getArea()[1])) {
                        this.team = this.game.navy;
                    }
                    else {
                        if (!Utils.isInArea(this.baseLocation, this.game.getArena().getWorld(), this.game.pirates.getArea()[0], this.game.pirates.getArea()[1])) {
                            return false;
                        }
                        this.team = this.game.pirates;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    BlockFace getFace() {
        return this.face;
    }
    
    ItemFrame getItemFrame() {
        return this.frame;
    }
    
    public Team getTeam() {
        return this.team;
    }
    
    Location getBaseLocation() {
        return this.baseLocation;
    }
    
    Location getSpawnLocation() {
        final Location spawnLocation = this.baseLocation;
        final Vector mod = new Vector(this.face.getModX() * Configuration.cannonLength, 0, this.face.getModZ() * Configuration.cannonLength);
        spawnLocation.add(mod);
        return spawnLocation;
    }
    
    ProjectileType getType() {
        for (final ProjectileType type : ProjectileType.getList()) {
            if (type.getItemStack().getType() == this.frame.getItem().getType()) {
                return type;
            }
        }
        return null;
    }
}
