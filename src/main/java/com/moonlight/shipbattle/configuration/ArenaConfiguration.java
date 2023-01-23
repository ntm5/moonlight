package com.moonlight.shipbattle.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import com.moonlight.shipbattle.Arena;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.teams.TeamType;
import org.bukkit.block.Sign;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Bukkit;

import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

public class ArenaConfiguration
{
    private static FileConfiguration config;
    private static File configFile;
    private static final List<Arena> arenas;
    public static boolean signsChanged;
    
    public static List<Arena> getArenas() {
        return ArenaConfiguration.arenas;
    }
    
    public static Arena getArena(final String name) {
        for (final Arena arena : ArenaConfiguration.arenas) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }
        return null;
    }
    
    static void load() {
        if (ArenaConfiguration.configFile == null) {
            ArenaConfiguration.configFile = new File(Main.getMain().getDataFolder(), "arenas.yml");
        }
        if (!ArenaConfiguration.configFile.exists()) {
            Main.getMain().saveResource("arenas.yml", false);
        }
        ArenaConfiguration.config = YamlConfiguration.loadConfiguration(ArenaConfiguration.configFile);
        if (ArenaConfiguration.config.getConfigurationSection("arenas") != null) {
            loadArenas();
        }
    }
    
    public static void save() {
        saveArenas();
        try {
            ArenaConfiguration.config.save(ArenaConfiguration.configFile);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void loadArenas() {
        ArenaConfiguration.arenas.clear();
        for (final String name : ArenaConfiguration.config.getConfigurationSection("arenas").getKeys(false)) {
            Logging.getLogger().info("Loading arena: " + name);
            final Arena arena = loadArena(name);
            ArenaConfiguration.arenas.add(arena);
        }
        Logging.getLogger().info("Loaded arenas: " + ArenaConfiguration.arenas);
    }
    
    private static Arena loadArena(final String name) {
        final String path = "arenas." + name;
        final Arena arena = new Arena(name);
        final World arenaWorld = Bukkit.getWorld(ArenaConfiguration.config.getString(path + ".world"));
        if (arenaWorld == null) {
            arena.setEnabled(false);
            throw new IllegalArgumentException("A vil\u00e1g nem tal\u00e1lhat\u00f3: " + ArenaConfiguration.config.getString(path + ".world"));
        }
        arena.setWorld(arenaWorld);
        arena.setLocation("lobby", getLocation(path + ".lobby"));
        arena.setLocation("outside", getLocation(path + ".outside"));
        arena.setLocation("area.min_point", getLocation(path + ".area.min_point", arenaWorld));
        arena.setLocation("area.max_point", getLocation(path + ".area.max_point", arenaWorld));
        arena.setLocation("navy.area.min_point", getLocation(path + ".navy.area.min_point", arenaWorld));
        arena.setLocation("navy.area.max_point", getLocation(path + ".navy.area.max_point", arenaWorld));
        arena.setLocation("navy.spawn", getLocation(path + ".navy.spawn", arenaWorld));
        arena.setLocation("navy.captain_spawn", getLocation(path + ".navy.captain_spawn", arenaWorld));
        arena.setLocation("pirates.area.min_point", getLocation(path + ".pirates.area.min_point", arenaWorld));
        arena.setLocation("pirates.area.max_point", getLocation(path + ".pirates.area.max_point", arenaWorld));
        arena.setLocation("pirates.spawn", getLocation(path + ".pirates.spawn", arenaWorld));
        arena.setLocation("pirates.captain_spawn", getLocation(path + ".pirates.captain_spawn", arenaWorld));
        if (ArenaConfiguration.config.getConfigurationSection(path + ".signs") != null) {
            for (final String key : ArenaConfiguration.config.getConfigurationSection(path + ".signs").getKeys(false)) {
                final Location location = getLocation(path + ".signs." + key);
                final Material material = location.getBlock().getType();
                if (Utils.isSign(material)) {
                    arena.getSigns().add(location);
                }
                else {
                    ArenaConfiguration.signsChanged = true;
                }
            }
        }
        arena.setMinPlayers(ArenaConfiguration.config.getInt(path + ".min_players"));
        arena.setMaxPlayers(ArenaConfiguration.config.getInt(path + ".max_players"));
        arena.setShipHealths(ArenaConfiguration.config.getInt(path + ".ship_healths"));
        arena.setCannonMultiplier(ArenaConfiguration.config.getDouble(path + ".cannon_multiplier"));
        return arena;
    }
    
    private static void saveArena(final Arena arena) {
        final String path = "arenas." + arena.getName();
        ArenaConfiguration.config.set(path + ".world", (Object)arena.getWorld().getName());
        setLocation(arena.getLocation("lobby"), path + ".lobby");
        setLocation(arena.getLocation("outside"), path + ".outside");
        setLocation(arena.getLocation("area.min_point"), path + ".area.min_point", false);
        setLocation(arena.getLocation("area.max_point"), path + ".area.max_point", false);
        setLocation(arena.getLocation("navy.area.min_point"), path + ".navy.area.min_point", false);
        setLocation(arena.getLocation("navy.area.max_point"), path + ".navy.area.max_point", false);
        setLocation(arena.getLocation("navy.spawn"), path + ".navy.spawn", false);
        setLocation(arena.getLocation("navy.captain_spawn"), path + ".navy.captain_spawn", false);
        setLocation(arena.getLocation("pirates.area.min_point"), path + ".pirates.area.min_point", false);
        setLocation(arena.getLocation("pirates.area.max_point"), path + ".pirates.area.max_point", false);
        setLocation(arena.getLocation("pirates.spawn"), path + ".pirates.spawn", false);
        setLocation(arena.getLocation("pirates.captain_spawn"), path + ".pirates.captain_spawn", false);
        int i = 0;
        for (final Location location : arena.getSigns()) {
            if (location.getBlock().getState() instanceof Sign) {
                setLocation(location, path + ".signs." + i);
                ++i;
            }
        }
        ArenaConfiguration.config.set(path + ".min_players", (Object)arena.getMinPlayers());
        ArenaConfiguration.config.set(path + ".max_players", (Object)arena.getMaxPlayers());
        ArenaConfiguration.config.set(path + ".cannon_multiplier", (Object)arena.getCannonMultiplier());
        ArenaConfiguration.config.set(path + ".ship_healths", (Object)arena.getShipHealths());
    }
    
    private static void saveArenas() {
        ArenaConfiguration.config.set("arenas", (Object)null);
        ArenaConfiguration.arenas.forEach(ArenaConfiguration::saveArena);
    }
    
    private static Location getLocation(final String path) {
        final World world = Bukkit.getWorld(ArenaConfiguration.config.getString(path + ".world"));
        final double x = ArenaConfiguration.config.getDouble(path + ".x");
        final double y = ArenaConfiguration.config.getDouble(path + ".y");
        final double z = ArenaConfiguration.config.getDouble(path + ".z");
        final float yaw = (float)ArenaConfiguration.config.getDouble(path + ".yaw");
        final float pitch = (float)ArenaConfiguration.config.getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    private static Location getLocation(final String path, final World arenaWorld) {
        final String world1 = ArenaConfiguration.config.getString(path + ".world");
        World world2;
        if (world1 == null) {
            world2 = arenaWorld;
        }
        else {
            world2 = Bukkit.getWorld(world1);
            assert world2 != null;
        }
        final double x = ArenaConfiguration.config.getDouble(path + ".x");
        final double y = ArenaConfiguration.config.getDouble(path + ".y");
        final double z = ArenaConfiguration.config.getDouble(path + ".z");
        final float yaw = (float)ArenaConfiguration.config.getDouble(path + ".yaw");
        final float pitch = (float)ArenaConfiguration.config.getDouble(path + ".pitch");
        return new Location(world2, x, y, z, yaw, pitch);
    }
    
    private static void setLocation(final Location location, final String path) {
        setLocation(location, path, true);
    }
    
    private static void setLocation(final Location location, final String path, final boolean setWorld) {
        if (setWorld) {
            ArenaConfiguration.config.set(path + ".world", (Object)location.getWorld().getName());
        }
        ArenaConfiguration.config.set(path + ".x", (Object)location.getX());
        ArenaConfiguration.config.set(path + ".y", (Object)location.getY());
        ArenaConfiguration.config.set(path + ".z", (Object)location.getZ());
        if (location.getYaw() != 0.0f) {
            ArenaConfiguration.config.set(path + ".yaw", (Object)(double)location.getYaw());
        }
        if (location.getPitch() != 0.0f) {
            ArenaConfiguration.config.set(path + ".pitch", (Object)(double)location.getPitch());
        }
    }
    
	public static int getShipHealths(final Arena arena) {
        final Material[] ignored = { Material.AIR, Material.WATER, Material.LEGACY_STATIONARY_WATER, Material.LAVA, Material.LEGACY_STATIONARY_LAVA };
        double all = 0.0;
        for (final TeamType teamType : TeamType.values()) {
            final Location minPoint = arena.getLocation(teamType.toString() + ".area.min_point");
            final Location maxPoint = arena.getLocation(teamType.toString() + ".area.max_point");
            final World world = minPoint.getWorld();
            final int minX = minPoint.getBlockX();
            final int minY = minPoint.getBlockY();
            final int minZ = minPoint.getBlockZ();
            final int maxX = maxPoint.getBlockX();
            final int maxY = maxPoint.getBlockY();
            final int maxZ = maxPoint.getBlockZ();
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        final Location location = new Location(world, (double)x, (double)y, (double)z);
                        if (!Arrays.asList(ignored).contains(location.getBlock().getType())) {
                            ++all;
                        }
                    }
                }
            }
        }
        return (int)Math.round(all / TeamType.values().length * 0.2);
    }
    
    static {
        arenas = new ArrayList<Arena>();
        ArenaConfiguration.signsChanged = false;
    }
}
