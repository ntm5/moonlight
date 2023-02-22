package com.moonlight.shipbattle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.moonlight.shipbattle.database.EmeraldEconomy;
import com.moonlight.shipbattle.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.moonlight.shipbattle.configuration.ArenaConfiguration;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.ItemLoadException;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.database.EconomyDatabase;
import com.moonlight.shipbattle.listeners.MainListener;
import com.moonlight.shipbattle.listeners.game.EntityListener;
import com.moonlight.shipbattle.listeners.game.InventoryListener;
import com.moonlight.shipbattle.listeners.game.PlayerListener;
import com.moonlight.shipbattle.listeners.game.WorldEventListener;
import com.moonlight.shipbattle.logging.Logging;

public class Main extends JavaPlugin
{
    public static String prefix;
    static final List<Game> games;
    private final Logger log;
    private static Main main;
    private final String logPrefix = "[ShipBattle] ";
    private boolean awake;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private EmeraldEconomy economy;

    public Map<UUID, PlayerData> getPlayerData() {
        return this.playerDataMap;
    }

    public EmeraldEconomy economy() {
        return this.economy;
    }


    public Main() {
        this.log = Logger.getLogger("Minecraft");
        this.awake = false;
    }
    
    public void onEnable() {
        Main.main = this;
        try {
            new Logging().init();
        }
        catch (IOException e) {
            this.log.severe("[ShipBattle] Hiba t\u00f6rt\u00e9nt a logger inicializ\u00e1l\u00e1sa k\u00f6zben.");
            e.printStackTrace();
        }

        Logging.getLogger().info("starting version: " + this.getDescription().getVersion());
        try {
            Configuration.load();
        }
        catch (ItemLoadException e2) {
            this.log.severe("[ShipBattle] Hiba t\u00f6rt\u00e9nt a konfigur\u00e1ci\u00f3s f\u00e1jl bet\u00f6lt\u00e9se k\u00f6zben.");
            Logging.getLogger().log(Level.SEVERE, "FATAL: config item error", e2);
            this.setEnabled(false);
            return;
        }
        catch (Exception e3) {
            this.log.severe("[ShipBattle] Hiba t\u00f6rt\u00e9nt a konfigur\u00e1ci\u00f3s f\u00e1jl bet\u00f6lt\u00e9se k\u00f6zben.");
            e3.printStackTrace();
            Logging.getLogger().log(Level.SEVERE, "FATAL: config error", e3);
            this.setEnabled(false);
            return;
        }
        Main.prefix = LangConfiguration.getString("main.prefix");
        this.getCommand("sb").setExecutor(new CommandManager());
        this.getServer().getPluginManager().registerEvents(new MainListener(), this);
        if (!this.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            this.log.severe("[ShipBattle] " + LangConfiguration.getString("main.worldedit_not_found"));
            Logging.getLogger().severe("FATAL: WorldEdit not found");
            this.setEnabled(false);
            return;
        }
        try {
            EconomyDatabase.setup();
            new EconomyDatabase();
        }
        catch (SQLException e4) {
            this.log.severe("[ShipBattle] " + LangConfiguration.getString("config.economy_error"));
            e4.printStackTrace();
            Logging.getLogger().log(Level.SEVERE, "FATAL: ", e4);
            this.setEnabled(false);
            return;
        }
        this.log.info("[ShipBattle] Sikeresen elind\u00edtva.");
        Logging.getLogger().info("started successfully");
        getServer().getPluginManager().registerEvents(new GuiInventory(), this);

        try {
            this.economy = new EmeraldEconomy();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        new ScoreboardManager().register();
    }
    
    public void onDisable() {
        Logging.getLogger().info("disabling");
        if (!Main.games.isEmpty()) {
            this.log.warning("[ShipBattle] " + LangConfiguration.getString("main.force_stop"));
            this.forceStopGames();
        }
        if (ArenaConfiguration.signsChanged) {
            ArenaConfiguration.save();
        }
        this.log.info("[ShipBattle] Serenes le\u00e1ll\u00edtva.");
        Logging.getLogger().info("disabled successfully");
        Logging.getInstance().out();
    }
    
    void forceStopGames() {
        Logging.getLogger().info("force stopping games");
        Bukkit.getScheduler().cancelTasks(getMain());
        while (Main.games.iterator().hasNext()) {
            Main.games.get(0).forceStop();
        }
    }
    
    boolean isAwake() {
        return this.awake;
    }
    
    void suspend() {
        Logging.getLogger().log(Level.INFO, "suspending");
        this.awake = false;
        Bukkit.getScheduler().cancelTasks(getMain());
        HandlerList.unregisterAll(PlayerListener.getInstance());
        HandlerList.unregisterAll(EntityListener.getInstance());
        HandlerList.unregisterAll(InventoryListener.getInstance());
        HandlerList.unregisterAll(WorldEventListener.getInstance());
    }
    
    void awake() {
        Logging.getLogger().log(Level.INFO, "waking up");
        this.awake = true;
        Bukkit.getScheduler().runTaskTimer(this, EntityListener::waterInteractTask, 0L, (long)this.getConfig().getInt("water_interaction_listener_frequency"));
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new EntityListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new WorldEventListener(), this);
    }
    
    public static Main getMain() {
        return Main.main;
    }
    
    static {
        games = new ArrayList<>();
    }
}
