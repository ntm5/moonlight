package com.moonlight.shipbattle.teams;

import com.moonlight.shipbattle.configuration.LangConfiguration;

public enum TeamType
{
    NAVY(LangConfiguration.getString("teams.navy_prefix"), LangConfiguration.getString("teams.navy_name"), LangConfiguration.getString("teams.soldiers"), LangConfiguration.getString("teams.soldier")), 
    PIRATES(LangConfiguration.getString("teams.pirates_prefix"), LangConfiguration.getString("teams.pirates_name"), LangConfiguration.getString("teams.pirates"), LangConfiguration.getString("teams.pirate"));
    
    private final String prefix;
    private final String name;
    private final String playerPlural;
    private final String playerSingular;
    
    private TeamType(final String prefix, final String name, final String playerPlural, final String playerSingular) {
        this.name = name;
        this.playerSingular = playerSingular;
        this.playerPlural = playerPlural;
        this.prefix = prefix;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getPlayerPlural() {
        return this.playerPlural;
    }
    
    public String getPlayerSingular() {
        return this.playerSingular;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
