package com.moonlight.shipbattle;

import com.moonlight.shipbattle.configuration.ArenaConfiguration;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.teams.Team;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.logging.Level;

public class Arena {
    private final String name;
    private boolean enabled;
    private Game game;
    private final Map<String, Location> locations;
    private final List<Location> signs;
    private World world;
    private int shipHealths;
    private double cannonMultiplier;
    private int minPlayers;
    private int maxPlayers;

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(final World world) {
        this.world = world;
    }

    public int getShipHealths() {
        return this.shipHealths;
    }

    public void setShipHealths(final int shipHealths) {
        this.shipHealths = shipHealths;
    }

    public double getCannonMultiplier() {
        return this.cannonMultiplier;
    }

    public void setCannonMultiplier(final double cannonMultiplier) {
        this.cannonMultiplier = cannonMultiplier;
    }

    public Arena(final String name) {
        this.enabled = true;
        this.locations = new HashMap<String, Location>();
        this.signs = new ArrayList<Location>();
        this.name = name;
    }

    Game getGame() {
        return this.game;
    }

    public Location getLocation(final String id) {
        return this.locations.get(id.toLowerCase());
    }

    public List<Location> getSigns() {
        return this.signs;
    }

    public void updateSigns() {
        Logging.getLogger().log(Level.INFO, "[0] {0}: updating signs", this);
        final Iterator<Location> iterator = this.signs.iterator();
        while (iterator.hasNext()) {
            final Location location = iterator.next();
            if (!(location.getBlock().getState() instanceof Sign)) {
                iterator.remove();
                return;
            }
            final Sign sign = (Sign) location.getBlock().getState();
            sign.setLine(0, LangConfiguration.getString("sign.header"));
            sign.setLine(1, LangConfiguration.getString("sign.arena_name").replace("$", this.name));
            final String players = (this.game != null) ? (this.game.getPlayers().size() + "") : "0";
            sign.setLine(2, LangConfiguration.getString("sign.players").replace("$a", players).replace("$b", this.maxPlayers * 2 + ""));
            sign.setLine(3, (this.game != null && (this.game.getStatus() != Game.Status.WAITING || this.game.getPlayers().size() >= this.maxPlayers * 2)) ? LangConfiguration.getString("sign.started") : LangConfiguration.getString("sign.join"));
            sign.update();
        }
    }

    public void setLocation(final String id, final Location location) {
        this.locations.put(id.toLowerCase(), location);
    }

    void initGame() {
        this.game = new Game(this);
        Main.games.add(this.game);
    }

    void reset() {
        //added by TeddyBear_2004 on 2022.07.18 to fix nullpointer
        if (this.game == null) {
            initGame();
        }

        Logging.getLogger().log(Level.INFO, "[0] {0}: resetting arena", this);
        Main.games.remove(this.game);
        Team.teams.remove(this.game.navy);
        Team.teams.remove(this.game.pirates);
        for (final Team team : this.game.teams) {
            if (team.getCaptain() != null) {
                team.getCaptain().remove();
            }
            team.getBossBar().setVisible(false);
            team.getBossBar().removeAll();
        }
        for (Entity e : world.getEntities()) {
            if (!(e instanceof CraftPlayer)) {
                e.remove();
            }
        }
        final SchematicOperation operation = new SchematicOperation();
        operation.load(this.name, this.world);
        if (operation.getOutcome() != SchematicOperation.Outcome.SUCCESS) {
            this.setEnabled(false);
            Logging.getLogger().log(Level.SEVERE, "FATAL: arena reset error: SchematicOperation outcome: " + operation.getOutcome(), this);
        }
        this.game = null;
        this.updateSigns();
        for (final Arena arena : ArenaConfiguration.getArenas()) {
            if (arena.game != null) {
                return;
            }
        }
        Main.getMain().suspend();
        Logging.getLogger().log(Level.INFO, "[1] {0}: arena reset", this);
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMinPlayers(final int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setMaxPlayers(final int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String toString() {
        return String.format("Arena@%d[name=%s]", this.hashCode(), this.name);
    }
}
