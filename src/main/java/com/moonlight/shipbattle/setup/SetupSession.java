package com.moonlight.shipbattle.setup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.moonlight.shipbattle.Main;
import org.bukkit.entity.Player;

import com.moonlight.shipbattle.Arena;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.teams.TeamType;

public class SetupSession
{
    private static final HashMap<Player, SetupSession> sessions;
    private final Player player;
    private final Arena arena;
    private final List<Step> steps;
    private final List<Step> navySteps;
    private final List<Step> piratesSteps;
    
    public SetupSession(final Player player, final String name) {
        this.steps = new LinkedList<Step>(Arrays.asList(Step.AREA, Step.LOBBY, Step.OUTSIDE));
        this.navySteps = new LinkedList<Step>(Arrays.asList(Step.TEAM_AREA, Step.TEAM_SPAWN, Step.TEAM_CAPTAIN_SPAWN));
        this.piratesSteps = new LinkedList<Step>(Arrays.asList(Step.TEAM_AREA, Step.TEAM_SPAWN, Step.TEAM_CAPTAIN_SPAWN));
        this.player = player;
        this.arena = new Arena(name);
        SetupSession.sessions.put(player, this);
    }
    
    public Arena getArena() {
        return this.arena;
    }
    
    public void next() {
        if (!this.steps.isEmpty()) {
            this.player.sendMessage(this.steps.get(0).getNextMessage());
            return;
        }
        if (!this.navySteps.isEmpty()) {
            this.player.sendMessage(this.navySteps.get(0).getNextMessage().replace("$", TeamType.NAVY.getPlayerPlural()));
            return;
        }
        if (!this.piratesSteps.isEmpty()) {
            this.player.sendMessage(this.piratesSteps.get(0).getNextMessage().replace("$", TeamType.PIRATES.getPlayerPlural()));
            return;
        }
        this.player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.done.next"));
    }
    
    public boolean hasNext() {
        return this.steps.size() > 0 || this.navySteps.size() > 0 || this.piratesSteps.size() > 0;
    }
    
    public void remove(final Step step) {
        this.steps.remove(step);
    }
    
    public void remove(final Step step, final TeamType type) {
        if (type == TeamType.NAVY) {
            this.navySteps.remove(step);
        }
        else {
            this.piratesSteps.remove(step);
        }
    }
    
    public void end() {
        SetupSession.sessions.remove(this.player);
    }
    
    public static SetupSession getSession(final Player player) {
        return SetupSession.sessions.get(player);
    }
    
    static {
        sessions = new HashMap<Player, SetupSession>();
    }
}
