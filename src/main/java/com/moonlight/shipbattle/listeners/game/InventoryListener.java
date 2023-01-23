package com.moonlight.shipbattle.listeners.game;

import com.moonlight.shipbattle.Game;
import com.moonlight.shipbattle.Lobby;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.shop.Shop;
import com.moonlight.shipbattle.shop.ShopItem;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventHandler;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;

import java.util.Objects;

public class InventoryListener implements Listener
{
    private static InventoryListener instance;
    
    public InventoryListener() {
        InventoryListener.instance = this;
    }
    
    public static InventoryListener getInstance() {
        return InventoryListener.instance;
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player)event.getWhoClicked();
        final Game game = Game.getGame(player);
        if (event.getAction() != InventoryAction.NOTHING && game != null && Shop.inventory.getViewers().contains(player)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            final ShopItem item = Shop.getItem(event.getSlot());
            if (item == null) {
                return;
            }
            if (game.getBalances().get(player) >= item.getPrice()) {
                game.getBalances().put(player, game.getBalances().get(player) - item.getPrice());
                player.getInventory().addItem(new ItemStack[] { item.getItemStack() });
                player.sendMessage(Main.prefix + LangConfiguration.getString("shop.purchase_successful"));
            }
            else {
                player.sendMessage(Main.prefix + LangConfiguration.getString("shop.not_enough_money"));
            }
            player.closeInventory();
        }
        else if (event.getAction() != InventoryAction.NOTHING && game != null && game.getStatus() == Game.Status.WAITING) {
            if (game.getLobby().getInventory().getViewers().contains(player)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                if (event.getSlot() == 4) {
                    game.getLobby().removePlayer(player);
                    player.sendMessage(Main.prefix + LangConfiguration.getString("lobby.random_team"));
                    return;
                }
                if (event.getSlot() == 2) {
                    game.getLobby().addPlayer(player, game.navy);
                    return;
                }
                if (event.getSlot() == 6) {
                    game.getLobby().addPlayer(player, game.pirates);
                    return;
                }
            }
            if (Objects.equals(event.getCurrentItem(), Lobby.teamChooserItem)) { //fixed by TeddyBear_2004 on 2022.07.18
                event.setCancelled(true);
            }
        }
    }
    
	@EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player) || event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        final Player player = (Player)event.getPlayer();
        final Game game = Game.getGame(player);
        if (game != null && game.hasStarted() && !event.getView().getTitle().equals(LangConfiguration.getString("shop.inventory_name"))) {
            player.sendMessage(Main.prefix + LangConfiguration.getString("supply.close"));
        }
    }
}
