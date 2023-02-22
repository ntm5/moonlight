package com.moonlight.shipbattle;

import com.moonlight.shipbattle.configuration.Configuration;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiInventory implements Listener {
    private final Inventory inventory;

    public GuiInventory() {
        this.inventory = Bukkit.createInventory(null, 54, "Emeralds");
        ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("");
        itemStack.setItemMeta(meta);
        for (int i = 0; i < 54; i++) {
            this.inventory.setItem(i, itemStack);
        }
        ItemStack itemStack1 = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta1 = itemStack1.getItemMeta();
        meta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eClick me to exchange emeralds."));
        meta1.setLore(List.of(ChatColor.translateAlternateColorCodes('&', "&cOne emerald is worth " + Configuration.coin_per_emerald + " coin(s).")));
        itemStack1.setItemMeta(meta1);
        this.inventory.setItem(54 / 2, itemStack1);
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryInteractEvent(InventoryClickEvent event) throws SQLException {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
            if (event.getSlot() == (54 / 2)) {
                int bal = Main.getMain().getPlayerData().get(event.getWhoClicked().getUniqueId()).emeralds();
                PlayerPointsAPI api = new PlayerPointsAPI(PlayerPoints.getInstance());
                api.give(event.getWhoClicked().getUniqueId(), bal * Configuration.coin_per_emerald);
                Main.getMain().getPlayerData().get(event.getWhoClicked().getUniqueId()).setEmeralds(-bal * Configuration.coin_per_emerald);
            }
        }
    }



}
