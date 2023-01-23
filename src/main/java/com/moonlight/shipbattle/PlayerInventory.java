package com.moonlight.shipbattle;

import org.bukkit.inventory.ItemStack;

public class PlayerInventory
{
    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    
    public PlayerInventory() {
        this.contents = new ItemStack[36];
        this.armorContents = new ItemStack[4];
        for (int i = 0; i < 36; ++i) {
            this.contents[i] = null;
            if (i < 4) {
                this.armorContents[i] = null;
            }
        }
    }
    
    ItemStack[] getContents() {
        return this.contents;
    }
    
    ItemStack[] getArmorContents() {
        return this.armorContents;
    }
    
    public void setItemStack(final int slot, final ItemStack itemStack) {
        this.contents[slot] = itemStack;
    }
    
    public void setHelmet(final ItemStack itemStack) {
        this.armorContents[3] = itemStack;
    }
    
    public void setChestplate(final ItemStack itemStack) {
        this.armorContents[2] = itemStack;
    }
    
    public void setLeggings(final ItemStack itemStack) {
        this.armorContents[1] = itemStack;
    }
    
    public void setBoots(final ItemStack itemStack) {
        this.armorContents[0] = itemStack;
    }
}
