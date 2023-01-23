package com.moonlight.shipbattle.shop;

import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;
import com.moonlight.shipbattle.configuration.LangConfiguration;

import org.bukkit.inventory.ItemStack;

public class ShopItem
{
    private final int price;
    private final ItemStack itemStack;
    
    public ShopItem(final int price, final ItemStack itemStack) {
        this.price = price;
        this.itemStack = itemStack;
    }
    
    public int getPrice() {
        return this.price;
    }
    
    public ItemStack getItemStack() {
        return this.itemStack;
    }
    
	public ItemStack toItemStack() {
        final ItemStack itemStack = new ItemStack(this.itemStack);
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta.getDisplayName() != null) {
            meta.setDisplayName(LangConfiguration.getString("shop.item_display_name").replace("$", meta.getDisplayName()));
        } else {
            meta.setDisplayName(LangConfiguration.getString("shop.item_display_name").replace("$", itemStack.getType().name()));
        }
        List<String> lore;
        if (meta.getLore() != null) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        lore.add(LangConfiguration.getString("shop.item_lore").replace("$", this.price + ""));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
