package com.moonlight.shipbattle.teams;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;

public class Captain
{
    private final Villager villager;
    
    Captain(final Location location, final Team team) {
        (this.villager = (Villager)location.getWorld().spawnEntity(location, EntityType.VILLAGER)).teleport(location);
        this.villager.setCanPickupItems(false);
        this.villager.setBreed(false);
        this.villager.setProfession(Villager.Profession.LIBRARIAN);
        this.villager.setAdult();
        this.villager.setRemoveWhenFarAway(false);
        this.villager.setCustomName(LangConfiguration.getString("captain.name").replace("$", team.getType().getName()));
        this.villager.setCustomNameVisible(true);
        this.villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Configuration.captainHealth);
        this.villager.setHealth(Configuration.captainHealth);
        this.villager.setAI(false);
        this.villager.setGravity(false);
    }
    
    public Villager getVillager() {
        return this.villager;
    }
    
    public void remove() {
        this.villager.getEquipment().clear();
        this.villager.remove();
    }
}
