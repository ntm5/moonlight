package com.moonlight.shipbattle.configuration;

import java.io.IOException;

import com.moonlight.shipbattle.Main;
import org.bukkit.Material;

public class Configuration
{
    public static int defaultBalance;
    public static double captainHealth;
    public static float cannonballExplosionStrength;
    public static int participation;
    public static int win;
    public static double kill;
    public static double blockDestroy;
    public static int firstBlood;
    public static int captainKill;
    public static Material cannonBaseBlockMaterial;
    public static int cannonLength;
    public static int supplyCooldown;
    public static int gameStartCooldown;
    public static int respawnCooldown;
    public static int timer;
    public static String database_host;
    public static String database_name;
    public static String database_table;
    public static String database_port;
    public static String database_username;
    public static String database_password;
    public static int top_1;
    public static int top_2;
    public static int top_3;
    public static int coin_per_emerald;
    
    public static void load() throws IOException, ItemLoadException {
        Main.getMain().saveDefaultConfig();
        LangConfiguration.load();
        loadOptions();
        ArenaConfiguration.load();
        ItemConfiguration.load();
        ScoreboardConfiguration.load();
    }
    
    public static void reload() throws IOException, ItemLoadException {
        Main.getMain().reloadConfig();
        if (ArenaConfiguration.signsChanged) {
            ArenaConfiguration.save();
        }
        load();
    }
    
    private static void loadOptions() {
        Configuration.defaultBalance = Main.getMain().getConfig().getInt("default_balance");
        Configuration.top_1 = Main.getMain().getConfig().getInt("rewards.top1");
        Configuration.top_2 = Main.getMain().getConfig().getInt("rewards.top2");
        Configuration.top_3 = Main.getMain().getConfig().getInt("rewards.top3");
        Configuration.coin_per_emerald = Main.getMain().getConfig().getInt("coins_per_emerald");
        Configuration.captainHealth = Main.getMain().getConfig().getDouble("captain_health");
        Configuration.cannonballExplosionStrength = (float)Main.getMain().getConfig().getDouble("cannonball_explosion_strength");
        Configuration.participation = Main.getMain().getConfig().getInt("rewards.participation");
        Configuration.win = Main.getMain().getConfig().getInt("rewards.win");
        Configuration.kill = Main.getMain().getConfig().getDouble("rewards.kill");
        Configuration.firstBlood = Main.getMain().getConfig().getInt("rewards.first_blood");
        Configuration.blockDestroy = Main.getMain().getConfig().getDouble("rewards.block_destroy");
        Configuration.captainKill = Main.getMain().getConfig().getInt("rewards.captain_kill");
        Configuration.cannonBaseBlockMaterial = Material.getMaterial(Main.getMain().getConfig().getString("cannon.base_block_material"));
        Configuration.cannonLength = Main.getMain().getConfig().getInt("cannon.length");
        Configuration.supplyCooldown = Main.getMain().getConfig().getInt("cooldowns.supply") * 1000;
        Configuration.gameStartCooldown = Main.getMain().getConfig().getInt("cooldowns.game_start");
        Configuration.respawnCooldown = Main.getMain().getConfig().getInt("cooldowns.respawn");
        Configuration.timer = Main.getMain().getConfig().getInt("cooldowns.timer");
        Configuration.database_host = Main.getMain().getConfig().getString("database.host");
        Configuration.database_port = Main.getMain().getConfig().getString("database.port");
        Configuration.database_name = Main.getMain().getConfig().getString("database.name");
        Configuration.database_table = Main.getMain().getConfig().getString("database.table");
        Configuration.database_username = Main.getMain().getConfig().getString("database.username");
        Configuration.database_password = Main.getMain().getConfig().getString("database.password");
    }
}
