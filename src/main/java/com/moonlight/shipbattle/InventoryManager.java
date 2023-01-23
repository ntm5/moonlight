package com.moonlight.shipbattle;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.moonlight.shipbattle.configuration.ItemConfiguration;
import com.moonlight.shipbattle.configuration.ItemLoadException;
import com.moonlight.shipbattle.teams.TeamType;

public class InventoryManager
{
    private static final HashMap<Player, ItemStack[]> inventories;
    private static final HashMap<Player, ItemStack[]> armors;
    private static final HashMap<Player, Integer> expPoints;
    private static final HashMap<Player, Double> health;
    private static PlayerInventory navyInventory;
    private static PlayerInventory pirateInventory;
    
    public static void loadInventories() throws ItemLoadException {
        InventoryManager.navyInventory = ItemConfiguration.loadInventory("navy");
        InventoryManager.pirateInventory = ItemConfiguration.loadInventory("pirate");
    }
    
    static void saveInventory(final Player player) {
        InventoryManager.inventories.put(player, player.getInventory().getContents());
        InventoryManager.armors.put(player, player.getInventory().getArmorContents());
        InventoryManager.expPoints.put(player, player.getTotalExperience());
        InventoryManager.health.put(player, player.getHealth());
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().addItem(new ItemStack[] { Lobby.teamChooserItem });
        player.setTotalExperience(0);
        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    static void restoreInventory(final Player player) {
        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setContents((ItemStack[])InventoryManager.inventories.get(player));
        player.getInventory().setArmorContents((ItemStack[])InventoryManager.armors.get(player));
        player.setTotalExperience((int)InventoryManager.expPoints.get(player));
        player.setHealth((double)InventoryManager.health.get(player));
        InventoryManager.inventories.remove(player);
        InventoryManager.armors.remove(player);
        InventoryManager.expPoints.remove(player);
        InventoryManager.health.remove(player);
    }
    
	public static void setGameInventory(final Player player, final TeamType teamType) {
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.getInventory().clear();
        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if (teamType == TeamType.NAVY) {
            player.getInventory().setContents(InventoryManager.navyInventory.getContents());
            player.getInventory().setArmorContents(InventoryManager.navyInventory.getArmorContents());
        }
        else {
            player.getInventory().setContents(InventoryManager.pirateInventory.getContents());
            player.getInventory().setArmorContents(InventoryManager.pirateInventory.getArmorContents());
        }
        player.updateInventory();
    }
    
    static {
        inventories = new HashMap<Player, ItemStack[]>();
        armors = new HashMap<Player, ItemStack[]>();
        expPoints = new HashMap<Player, Integer>();
        health = new HashMap<Player, Double>();
    }
}
