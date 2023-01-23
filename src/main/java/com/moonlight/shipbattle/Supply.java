package com.moonlight.shipbattle;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.moonlight.shipbattle.cannon.projectile.ProjectileType;

public class Supply
{
    private static final Supply instance;
    
    public static Supply getInstance() {
        return Supply.instance;
    }
    
    public void openInventory(final Player player) {
        final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST);
        final Random random = new Random();
        final int randomInt = random.nextInt(ProjectileType.getList().size());
        final ProjectileType type = ProjectileType.getList().get(randomInt);
        inventory.setItem(11, type.getItemStack());
        inventory.setItem(15, new ItemStack(Material.COOKED_COD, random.nextInt(3) + 1));
        if (Math.random() <= 0.7) {
            inventory.setItem(13, new ItemStack(Material.OAK_BOAT, 1));
        }
        if (Math.random() <= 0.5) {
            inventory.setItem(12, new ItemStack(Material.BUCKET, 1));
        }
        if (Math.random() <= 0.4) {
            inventory.setItem(12, new ItemStack(Material.MILK_BUCKET, 1));
        }
        player.openInventory(inventory);
    }
    
    static {
        instance = new Supply();
    }
}
