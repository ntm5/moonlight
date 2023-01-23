package com.moonlight.shipbattle.configuration;

import com.moonlight.shipbattle.InventoryManager;
import com.moonlight.shipbattle.Lobby;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.PlayerInventory;
import com.moonlight.shipbattle.cannon.projectile.ProjectileType;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.shop.Shop;
import com.moonlight.shipbattle.shop.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class ItemConfiguration {
    private static FileConfiguration config;
    private static File configFile;

    static void load() throws ItemLoadException {
        if (ItemConfiguration.configFile == null) {
            ItemConfiguration.configFile = new File(Main.getMain().getDataFolder(), "items.yml");
        }
        if (!ItemConfiguration.configFile.exists()) {
            Main.getMain().saveResource("items.yml", false);
        }
        ItemConfiguration.config = YamlConfiguration.loadConfiguration(ItemConfiguration.configFile);
        InventoryManager.loadInventories();
        Shop.load();
        loadProjectiles();
        loadLobbyItems();
    }

    private static ItemStack loadItemStack(final String name) throws ItemLoadException {
        final String path = "items." + name;
        if (ItemConfiguration.config.get(path) != null) {
            ItemStack itemStack;
            try{
                itemStack = ItemConfiguration.config.getItemStack(path);
            }catch(Exception e){
                Main.getMain().getLogger().severe("Hiba t\u00f6rt\u00e9nt a " + name + " itemstack bet\u00f6lt\u00e9se k\u00f6zben.");
                Logging.getLogger().log(Level.SEVERE, "error while loading item: " + name, e);
                throw new ItemLoadException(name);
            }
            return itemStack;
        }
        throw new NullPointerException("Nem tal\u00e1lhat\u00f3 t\u00e1rgy ezzel a n\u00e9vvel:" + name);
    }

    public static void saveItemStack(final ItemStack itemStack, final String name) {
        final String path = "items." + name;
        ItemConfiguration.config.set(path, itemStack);
        try{
            ItemConfiguration.config.save(ItemConfiguration.configFile);
        }catch(IOException e){
            e.printStackTrace();
            Logging.getLogger().log(Level.SEVERE, "item config save error", e);
        }
    }

    public static PlayerInventory loadInventory(final String name) throws ItemLoadException {
        final String path = "inventories." + name;
        final PlayerInventory inventory = new PlayerInventory();
        for (final String key : ItemConfiguration.config.getConfigurationSection(path).getKeys(false)) {
            final String lowerCase = key.toLowerCase();
            switch(lowerCase){
                case "helmet":{
                    inventory.setHelmet(loadItemStack(ItemConfiguration.config.getString(path + ".helmet")));
                    continue;
                }
                case "chestplate":{
                    inventory.setChestplate(loadItemStack(ItemConfiguration.config.getString(path + ".chestplate")));
                    continue;
                }
                case "leggings":{
                    inventory.setLeggings(loadItemStack(ItemConfiguration.config.getString(path + ".leggings")));
                    continue;
                }
                case "boots":{
                    inventory.setBoots(loadItemStack(ItemConfiguration.config.getString(path + ".boots")));
                    continue;
                }
                default:{
                    inventory.setItemStack(Integer.parseInt(key), loadItemStack(ItemConfiguration.config.getString(path + "." + key)));
                    continue;
                }
            }
        }
        return inventory;
    }

    public static Inventory loadShopInventory(final HashMap<Integer, ShopItem> map) throws ItemLoadException {
        final String path = "shop";
        final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, LangConfiguration.getString("shop.inventory_name"));
        for (final String key : ItemConfiguration.config.getConfigurationSection(path).getKeys(false)) {

            final int slot = Integer.parseInt(key);
            final ItemStack itemStack = loadItemStack(ItemConfiguration.config.getString(path + "." + key + ".item"));
            final int price = ItemConfiguration.config.getInt(path + "." + key + ".price");
            final ShopItem shopItem = new ShopItem(price, itemStack);
            map.put(slot, shopItem);
            ItemStack stack = shopItem.toItemStack();
            if (stack == null)
                continue;
            inventory.setItem(slot, stack);
        }
        return inventory;
    }

    private static ProjectileType loadProjectile(final String name) throws ItemLoadException {
        final String path = "projectiles." + name;
        final ItemStack itemStack = loadItemStack(ItemConfiguration.config.getString(path + ".item"));
        final ProjectileType.Effect effect = ProjectileType.Effect.valueOf(ItemConfiguration.config.getString(path + ".effect"));
        return new ProjectileType(effect, itemStack);
    }

    private static void loadProjectiles() throws ItemLoadException {
        for (final String name : ItemConfiguration.config.getConfigurationSection("projectiles").getKeys(false)) {
            ProjectileType.getList().add(loadProjectile(name));
        }
    }

    private static void loadLobbyItems() throws ItemLoadException {
        Lobby.navyItemStack = loadItemStack(ItemConfiguration.config.getString("lobby.navy_item"));
        Lobby.piratesItemStack = loadItemStack(ItemConfiguration.config.getString("lobby.pirates_item"));
        Lobby.random = loadItemStack(ItemConfiguration.config.getString("lobby.random_item"));
        Lobby.teamChooserItem = loadItemStack(ItemConfiguration.config.getString("lobby.team_chooser_item"));
    }
}
