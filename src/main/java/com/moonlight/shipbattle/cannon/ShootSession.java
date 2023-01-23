package com.moonlight.shipbattle.cannon;

import com.moonlight.shipbattle.cannon.projectile.ProjectileEntity;
import com.moonlight.shipbattle.teams.Team;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ShootSession {
    private static final Map<Player, ShootSession> map;
    private final Player player;
    private final Cannon cannon;

    public static Map<Player, ShootSession> getMap() {
        return ShootSession.map;
    }

    public ShootSession(final Player player, final Cannon cannon) {
        this.player = player;
        this.cannon = cannon;
    }

    public void fire() {
        if (!this.cannon.update()) {
            return;
        }
        final Location spawnLocation = this.cannon.getSpawnLocation();
        final Team team = Team.getTeam(this.player);
        assert team != null;
        ProjectileEntity.spawnProjectile(spawnLocation, team, this.player, this.cannon.getType(), this.getVector());
        this.cannon.getItemFrame().setItem((ItemStack) null);
        this.cannon.getBaseLocation().getWorld().playSound(this.cannon.getBaseLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        this.cannon.getBaseLocation().getWorld().playSound(this.cannon.getBaseLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
        spawnLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, spawnLocation, 10, 0.6, 0.6, 0.6);
        spawnLocation.getWorld().spawnParticle(Particle.FLAME, spawnLocation, 3, 0.3, 0.3, 0.3);
    }

    private Vector getVector() {//Commented out by TeddyBear_2004 on 2022.07.19
        final double x = /*Math.abs(this.cannon.getFace().getModX()) * */ this.player.getLocation().getDirection().getX();
        final double y = this.player.getLocation().getDirection().getY();
        final double z = /*Math.abs(this.cannon.getFace().getModZ()) * */ this.player.getLocation().getDirection().getZ();

        return new Vector(x, y, z).multiply(this.cannon.getTeam().getGame().getArena().getCannonMultiplier());
    }

    static {
        map = new HashMap<>();
    }
}
