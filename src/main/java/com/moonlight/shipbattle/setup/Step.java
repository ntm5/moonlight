package com.moonlight.shipbattle.setup;

import com.moonlight.shipbattle.configuration.LangConfiguration;

public enum Step
{
    AREA(LangConfiguration.getString("commands.setup.area"), LangConfiguration.getString("commands.setup.area.next")), 
    LOBBY(LangConfiguration.getString("commands.setup.lobby"), LangConfiguration.getString("commands.setup.lobby.next")), 
    OUTSIDE(LangConfiguration.getString("commands.setup.outside"), LangConfiguration.getString("commands.setup.outside.next")), 
    TEAM_AREA(LangConfiguration.getString("commands.setup.team.area"), LangConfiguration.getString("commands.setup.team.area.next")), 
    TEAM_SPAWN(LangConfiguration.getString("commands.setup.team.spawn"), LangConfiguration.getString("commands.setup.team.spawn.next")), 
    TEAM_CAPTAIN_SPAWN(LangConfiguration.getString("commands.setup.team.captain_spawn"), LangConfiguration.getString("commands.setup.team.captain_spawn.next"));
    
    final String nextMessage;
    final String successMessage;
    
    private Step(final String successMessage, final String nextMessage) {
        this.successMessage = successMessage;
        this.nextMessage = nextMessage;
    }
    
    public String getNextMessage() {
        return this.nextMessage;
    }
    
    public String getSuccessMessage() {
        return this.successMessage;
    }
}
