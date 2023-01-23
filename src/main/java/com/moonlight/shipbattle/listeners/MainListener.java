package com.moonlight.shipbattle.listeners;

import com.moonlight.shipbattle.Game;
import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.setup.SetupSession;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.moonlight.shipbattle.Arena;
import com.moonlight.shipbattle.configuration.ArenaConfiguration;
import com.moonlight.shipbattle.configuration.LangConfiguration;

import static com.moonlight.shipbattle.Game.integerMap;

public class MainListener implements Listener
{
	@EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Utils.isSign(event.getClickedBlock().getType())) {
            final Player player = event.getPlayer();
            final Sign sign = (Sign)event.getClickedBlock().getState();
            if (sign.getLine(0).equalsIgnoreCase(LangConfiguration.getString("sign.header")) && Game.joinPlayer(player, sign.getLine(1), sign.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
    
	@EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("shipbattle.setup") && event.getLine(0).equalsIgnoreCase(LangConfiguration.getString("sign.header_raw"))) {
            if (event.getLine(1).equalsIgnoreCase(LangConfiguration.getString("sign.setup_label"))) {
                if (!Main.getMain().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.we_not_found"));
                    return;
                }
                if (SetupSession.getSession(player) != null) {
                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new.error.already_started"));
                    return;
                }
                final String name = event.getLine(2);
                final String[] players = event.getLine(3).split("/");
                int minPlayers;
                int maxPlayers;
                try {
                    minPlayers = Integer.parseInt(players[0]);
                    maxPlayers = Integer.parseInt(players[1]);
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException ex2) {
                    player.sendMessage(Main.prefix + LangConfiguration.getString("sign.setup.error.incorrect_sign"));
                    return;
                }
                for (final Arena arena : ArenaConfiguration.getArenas()) {
                    if (arena.getName().equalsIgnoreCase(name)) {
                        player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new.error.already_exists"));
                        return;
                    }
                }
                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new").replace("$", name));
                final SetupSession session = new SetupSession(player, name);
                session.getArena().setMaxPlayers(maxPlayers);
                session.getArena().setMinPlayers(minPlayers);
                session.getArena().getSigns().add(event.getBlock().getLocation());
                event.setLine(0, LangConfiguration.getString("sign.header"));
                event.setLine(1, name);
                event.setLine(2, LangConfiguration.getString("sign.setup_in_progress"));
                event.setLine(3, "");
                session.next();
            }
            else if (event.getLine(1).equalsIgnoreCase(LangConfiguration.getString("sign.join_label"))) {
                for (final Arena arena2 : ArenaConfiguration.getArenas()) {
                    if (event.getLine(2).equalsIgnoreCase(arena2.getName())) {
                        arena2.getSigns().add(event.getBlock().getLocation());
                        player.sendMessage(Main.prefix + LangConfiguration.getString("sign.created").replace("$", arena2.getName()));
                        event.setCancelled(ArenaConfiguration.signsChanged = true);
                        arena2.updateSigns();
                        return;
                    }
                }
                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.join.error.arena_not_found"));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (integerMap.containsKey(event.getEntity().getUniqueId()))
            event.setCancelled(true);
    }
}
