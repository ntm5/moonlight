package com.moonlight.shipbattle;

import java.util.logging.Level;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.teams.TeamType;

public class Utils
{
    public static Game getGameContains(final Location location) {
        for (final Game game : Main.games) {
            final Arena arena = game.getArena();
            if (location.getWorld() != arena.getWorld()) {
                continue;
            }
            final Location minPoint = arena.getLocation("area.min_point");
            final Location maxPoint = arena.getLocation("area.max_point");
            if (location.getBlockX() >= minPoint.getBlockX() && location.getBlockX() <= maxPoint.getBlockX() && location.getBlockY() >= minPoint.getBlockY() && location.getBlockY() <= maxPoint.getBlockY() && location.getBlockZ() >= minPoint.getBlockZ() && location.getBlockZ() <= maxPoint.getBlockZ()) {
                return game;
            }
        }
        return null;
    }
    
	public static boolean isSign(Material m) {
    	return m == Material.ACACIA_SIGN || m == Material.ACACIA_WALL_SIGN || m == Material.BIRCH_SIGN || m == Material.BIRCH_WALL_SIGN || m == Material.CRIMSON_SIGN || 
    			m == Material.CRIMSON_WALL_SIGN || m == Material.DARK_OAK_SIGN || m == Material.DARK_OAK_WALL_SIGN || m == Material.JUNGLE_SIGN || m == Material.JUNGLE_WALL_SIGN || 
    			m == Material.LEGACY_SIGN || m == Material.LEGACY_SIGN_POST || m == Material.LEGACY_WALL_SIGN || m == Material.OAK_SIGN || m == Material.OAK_WALL_SIGN || m == Material.SPRUCE_SIGN || 
    			m == Material.SPRUCE_WALL_SIGN || m == Material.WARPED_SIGN || m == Material.WARPED_WALL_SIGN;
    }
    
	public static boolean isButton(Material m) {
		return m == Material.ACACIA_BUTTON || m == Material.BIRCH_BUTTON || m == Material.CRIMSON_BUTTON || m == Material.DARK_OAK_BUTTON || m == Material.JUNGLE_BUTTON || 
				m == Material.LEGACY_STONE_BUTTON || m == Material.LEGACY_WOOD_BUTTON || m == Material.OAK_BUTTON || m == Material.POLISHED_BLACKSTONE_BUTTON || m == Material.SPRUCE_BUTTON || 
				m == Material.STONE_BUTTON || m == Material.WARPED_BUTTON;
	}
	
    public static boolean isInArea(final Location location, final World world, final Location minPoint, final Location maxPoint) {
        return location.getWorld().getName().equals(world.getName()) && location.getBlockX() >= minPoint.getBlockX() && location.getBlockX() <= maxPoint.getBlockX() && location.getBlockY() >= minPoint.getBlockY() && location.getBlockY() <= maxPoint.getBlockY() && location.getBlockZ() >= minPoint.getBlockZ() && location.getBlockZ() <= maxPoint.getBlockZ();
    }
    
    static boolean isInAreaHorizontal(final Location location, final World world, final Location minPoint, final Location maxPoint) {
        return location.getWorld().getName().equals(world.getName()) && location.getBlockX() >= minPoint.getBlockX() && location.getBlockX() <= maxPoint.getBlockX() && location.getBlockZ() >= minPoint.getBlockZ() && location.getBlockZ() <= maxPoint.getBlockZ();
    }
    
    public static String getPercentageColor(final int percentage) {
        char code;
        if (percentage >= 70) {
            code = 'a';
        }
        else if (percentage >= 50) {
            code = '2';
        }
        else if (percentage >= 30) {
            code = 'e';
        }
        else if (percentage >= 15) {
            code = '6';
        }
        else {
            code = 'c';
        }
        return "§" + code;
    }
    
    public static BarColor getPercentageBossBarColor(final double percentage) {
        BarColor color;
        if (percentage >= 0.6) {
            color = BarColor.GREEN;
        }
        else if (percentage >= 0.3) {
            color = BarColor.YELLOW;
        }
        else {
            color = BarColor.RED;
        }
        return color;
    }
    
    public static FireworkEffect getFirework(final TeamType teamType) {
        final Color color = (teamType == TeamType.NAVY) ? Color.fromRGB(5308234) : Color.fromRGB(16732234);
        final Color[] fadeColors = { Color.fromRGB(4259807), Color.fromRGB(16777215) };
        return FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).withFade(fadeColors).trail(true).flicker(false).build();
    }
    
    static void fixCaptains(final Game game) {
        Logging.getLogger().log(Level.INFO, "fixCaptains called");
        final World world = game.getArena().getWorld();
        final Location minPoint = game.getArena().getLocation("area.min_point");
        final Location maxPoint = game.getArena().getLocation("area.max_point");
        for (final Entity entity : world.getEntities()) {
            if (entity instanceof Villager && !((Villager)entity).getRemoveWhenFarAway() && isInAreaHorizontal(entity.getLocation(), world, minPoint, maxPoint) && entity != game.navy.getCaptain().getVillager() && entity != game.pirates.getCaptain().getVillager()) {
                entity.remove();
                Logging.getLogger().log(Level.INFO, "FIX_CAPTAINS captain entity removed");
            }
        }
    }
}
