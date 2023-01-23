package com.moonlight.shipbattle.shop;

import org.bukkit.entity.Player;
import com.moonlight.shipbattle.configuration.ItemLoadException;
import com.moonlight.shipbattle.configuration.ItemConfiguration;
import java.util.HashMap;
import org.bukkit.inventory.Inventory;

public class Shop
{
    public static Inventory inventory;
    private static final HashMap<Integer, ShopItem> items;
    
    public static void load() throws ItemLoadException {
        Shop.inventory = ItemConfiguration.loadShopInventory(Shop.items);
    }
    
    public static void openShopInventory(final Player player) {
        player.openInventory(Shop.inventory);
    }
    
    public static ShopItem getItem(final int slot) {
        return Shop.items.get(slot);
    }
    
    static {
        items = new HashMap<>();
    }
}
