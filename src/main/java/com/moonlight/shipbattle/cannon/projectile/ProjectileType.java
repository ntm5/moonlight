package com.moonlight.shipbattle.cannon.projectile;

import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class ProjectileType
{
    private static final List<ProjectileType> list;
    private final Effect effect;
    private final ItemStack itemStack;
    
    public ProjectileType(final Effect effect, final ItemStack itemStack) {
        this.effect = effect;
        this.itemStack = itemStack;
    }
    
    Effect getEffect() {
        return this.effect;
    }
    
    public ItemStack getItemStack() {
        return this.itemStack;
    }
    
    public static List<ProjectileType> getList() {
        return ProjectileType.list;
    }
    
    static {
        list = new ArrayList<>();
    }
    
    public enum Effect
    {
        EXPLOSION, 
        FIRE, 
        SMOKE, 
        LIGHTNING;
    }
}
