package com.moonlight.shipbattle;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.SessionManager;
import com.moonlight.shipbattle.configuration.ArenaConfiguration;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.ItemConfiguration;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import com.moonlight.shipbattle.database.EconomyDatabase;
import com.moonlight.shipbattle.logging.Logging;
import com.moonlight.shipbattle.setup.SetupSession;
import com.moonlight.shipbattle.setup.Step;
import com.moonlight.shipbattle.teams.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;
import java.util.logging.Level;

class CommandManager implements CommandExecutor
{
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (label.equalsIgnoreCase("shipbattle") || label.equalsIgnoreCase("sb")) {
            if (args.length <= 0) {
                sender.sendMessage(LangConfiguration.getString("commands.info").replace("$", Main.getMain().getDescription().getVersion()));
                return true;
            }
            if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.help.label"))) {
                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.help.player"));
                return true;
            }
            if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.join.label"))) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.only_players"));
                    return true;
                }
                final Player player = (Player)sender;
                if (args.length <= 1) {
                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.usage").replace("$", LangConfiguration.getString("commands.join.usage")));
                    return true;
                }
                Game.joinPlayer(player, args[1], null);
                return true;
            }
            else if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.leave.label"))) {
                if (!(sender instanceof final Player player)) {
                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.only_players"));
                    return true;
                }
                final Game game = Game.getGame(player);
                if (game == null) {
                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.leave.error.not_in_game"));
                    return true;
                }
                Bukkit.getLogger().severe(player.getName() + " is trying to leave game: " + game);
                game.removePlayer(player, true);
                return true;
            }
            else if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.balance.label"))) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.only_players"));
                    return true;
                }
                final Player player = (Player)sender;
                final Game game = Game.getGame(player);
                if (game == null) {
                    EconomyDatabase.getDatabase().getBalance(player.getName(), balance -> player.sendMessage(Main.prefix + LangConfiguration.getString("commands.balance").replace("$", balance + "")));
                }
                else {
                    final int balance2 = game.getBalances().getOrDefault(player, 0); //fixed by TeddyBear_2004 on 2022.07.18
                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.balance").replace("$", balance2 + ""));
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("exchange")) {
                if (!(sender instanceof Player))
                    return false;
                new GuiInventory().openInventory((Player) sender);
                return true;
            }
            else {
                if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.admin.label"))) {
                    if (!sender.hasPermission("shipbattle.admin")) {
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.no_permission"));
                        return true;
                    }
                    if (args.length <= 1) {
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.help.label"))) {
                        sender.sendMessage(LangConfiguration.getString("commands.admin.help"));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.get_balance.label"))) {
                        if (args.length < 3) {
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                            return true;
                        }
                        final String target = args[2];
                        EconomyDatabase.getDatabase().getBalance(target, balance -> sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.get_balance").replace("$a", target).replace("$b", balance + "")));
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.set_balance.label"))) {
                        if (args.length < 4) {
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                            return true;
                        }
                        final String target = args[2];
                        int newBalance;
                        try {
                            newBalance = Integer.parseInt(args[3]);
                        }
                        catch (NumberFormatException exception) {
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                            return true;
                        }
                        EconomyDatabase.getDatabase().setBalance(target, newBalance);
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.set_balance").replace("$a", target + "").replace("$b", newBalance + ""));
                        return true;
                    }
                    else {
                        if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.enable.label"))) {
                            if (args.length > 2) {
                                final Arena arena = ArenaConfiguration.getArena(args[2]);
                                if (arena == null) {
                                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                                    return true;
                                }
                                arena.setEnabled(true);
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.enable.arena").replace("$", arena.getName()));
                            }
                            else {
                                for (final Arena arena2 : ArenaConfiguration.getArenas()) {
                                    arena2.setEnabled(true);
                                }
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.enable"));
                            }
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.disable.label"))) {
                            if (args.length > 2) {
                                final Arena arena = ArenaConfiguration.getArena(args[2]);
                                if (arena == null) {
                                    sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                                    return true;
                                }
                                arena.setEnabled(false);
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.disable.arena").replace("$", arena.getName()));
                            }
                            else {
                                for (final Arena arena2 : ArenaConfiguration.getArenas()) {
                                    arena2.setEnabled(false);
                                }
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.disable"));
                            }
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.force_stop.label"))) {
                            Main.getMain().forceStopGames();
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.force_stop"));
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.save_item.label"))) {
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.only_players"));
                                return true;
                            }
                            if (args.length < 3) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                                return true;
                            }
                            final Player player = (Player)sender;
                            final String name = args[2];
                            final ItemStack itemStack = player.getInventory().getItemInMainHand();
                            if (args.length >= 4) {
                                final ItemMeta meta = itemStack.getItemMeta();
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[3].replace("_", " ")));
                                itemStack.setItemMeta(meta);
                            }
                            ItemConfiguration.saveItemStack(itemStack, name);
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.save_item").replace("$", name));
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.reload_config.label"))) {
                            if (!Main.games.isEmpty()) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.reload_config.error.games_running"));
                                return true;
                            }
                            try {
                                Configuration.reload();
                            }
                            catch (Exception e) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.reload_config.error.fatal"));
                                Logging.getLogger().log(Level.SEVERE, "RELOADING(reload) FATAL: config error", e);
                                Bukkit.getPluginManager().disablePlugin(Main.getMain());
                                return true;
                            }
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.reload_config"));
                            return true;
                        }
                        else {
                            if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.admin.save_arenas.label"))) {
                                ArenaConfiguration.save();
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.save_arenas"));
                                return true;
                            }
                            sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.error.bad_args"));
                        }
                    }
                }

                if (args[0].equalsIgnoreCase(LangConfiguration.getString("commands.setup.label"))) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.only_players"));
                        return true;
                    }
                    if (!sender.hasPermission("shipbattle.setup")) {
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.no_permission"));
                        return true;
                    }
                    if (args.length <= 1) {
                        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.help.label"))) {
                        sender.sendMessage(LangConfiguration.getString("commands.setup.help"));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.new.label"))) {
                        if (!Main.getMain().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.we_not_found"));
                            return true;
                        }
                        if (SetupSession.getSession(player) != null) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new.error.already_started"));
                            return true;
                        }
                        if (args.length < 5) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                            return true;
                        }
                        final String name = args[2];
                        for (final Arena arena3 : ArenaConfiguration.getArenas()) {
                            if (arena3.getName().equalsIgnoreCase(name)) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new.error.already_exists"));
                                return true;
                            }
                        }
                        int minPlayers;
                        int maxPlayers;
                        try {
                            minPlayers = Integer.parseInt(args[3]);
                            maxPlayers = Integer.parseInt(args[4]);
                        }
                        catch (NumberFormatException e3) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                            return true;
                        }
                        player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.new").replace("$", name));
                        final SetupSession session = new SetupSession(player, name);
                        session.getArena().setMinPlayers(minPlayers);
                        session.getArena().setMaxPlayers(maxPlayers);
                        session.next();
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.area.label"))) {
                        if (!Main.getMain().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.we_not_found"));
                            return true;
                        }
                        final SetupSession session2 = SetupSession.getSession(player);
                        if (session2 == null) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                            return true;
                        }
                        final SessionManager manager = WorldEdit.getInstance().getSessionManager();
                        final LocalSession session = manager.findByName(player.getName());
                        if (session.getSelection() == null) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.no_selection"));
                            return true;
                        }
                        if (!(session.getSelection() instanceof CuboidRegion)) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.selection_not_cuboid"));
                            return true;
                        }
                        final Arena arena3 = session2.getArena();
                        final CuboidRegion selection = (CuboidRegion)session.getSelection();
                        World w = Bukkit.getWorld(selection.getWorld().getName());
                        BlockVector3 maxv = selection.getMaximumPoint();
                        BlockVector3 minv = selection.getMinimumPoint();
                        Location max = new Location(w, maxv.getBlockX(), maxv.getBlockY(), maxv.getBlockZ());
                        Location min = new Location(w, minv.getBlockX(), minv.getBlockY(), minv.getBlockZ());
                        arena3.setWorld(w);
                        arena3.setLocation("area.max_point", max);
                        arena3.setLocation("area.min_point", min);
                        final SchematicOperation operation = new SchematicOperation();
                        operation.save(arena3.getName(), min, max);
                        if (operation.getOutcome() == SchematicOperation.Outcome.ERROR) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.schematic"));
                            return true;
                        }
                        player.sendMessage(Main.prefix + Step.AREA.getSuccessMessage().replace("$", arena3.getName()));
                        session2.remove(Step.AREA);
                        session2.next();
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.lobby.label"))) {
                        final SetupSession session2 = SetupSession.getSession(player);
                        if (session2 == null) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                            return true;
                        }
                        final Arena arena4 = session2.getArena();
                        arena4.setLocation("lobby", player.getLocation());
                        arena4.setLocation("lobby", player.getLocation());
                        player.sendMessage(Main.prefix + Step.LOBBY.getSuccessMessage().replace("$", arena4.getName()));
                        session2.remove(Step.LOBBY);
                        session2.next();
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.outside.label"))) {
                        final SetupSession session2 = SetupSession.getSession(player);
                        if (session2 == null) {
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                            return true;
                        }
                        final Arena arena4 = session2.getArena();
                        arena4.setLocation("outside", player.getLocation());
                        player.sendMessage(Main.prefix + Step.OUTSIDE.getSuccessMessage().replace("$", arena4.getName()));
                        session2.remove(Step.OUTSIDE);
                        session2.next();
                        return true;
                    }
                    else {
                        if (args[1].equalsIgnoreCase(TeamType.NAVY.getPlayerPlural()) || args[1].equalsIgnoreCase(TeamType.PIRATES.getPlayerPlural())) {
                            if (!Main.getMain().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.we_not_found"));
                                return true;
                            }
                            final SetupSession session2 = SetupSession.getSession(player);
                            if (session2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                                return true;
                            }
                            final Arena arena4 = session2.getArena();
                            TeamType teamType = TeamType.NAVY;
                            for (final TeamType type : TeamType.values()) {
                                if (type.getPlayerPlural().equalsIgnoreCase(args[1])) {
                                    teamType = type;
                                }
                            }
                            if (args[2].equalsIgnoreCase(LangConfiguration.getString("commands.setup.team.area.label"))) {
                                final SessionManager manager = WorldEdit.getInstance().getSessionManager();
                                final LocalSession session = manager.findByName(player.getName());
                                if (session.getSelection() == null) {
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.no_selection"));
                                    return true;
                                }
                                if (!(session.getSelection() instanceof CuboidRegion)) {
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.selection_not_cuboid"));
                                    return true;
                                }
                                final CuboidRegion selection2 = (CuboidRegion)session.getSelection();
                                if (!selection2.getWorld().getName().equals(arena4.getWorld().getName())) {
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.wrong_world").replace("$", arena4.getWorld().getName()));
                                    return true;
                                }
                                World w = Bukkit.getWorld(selection2.getWorld().getName());
                                BlockVector3 maxv = selection2.getMaximumPoint();
                                BlockVector3 minv = selection2.getMinimumPoint();
                                Location max = new Location(w, maxv.getBlockX(), maxv.getBlockY(), maxv.getBlockZ());
                                Location min = new Location(w, minv.getBlockX(), minv.getBlockY(), minv.getBlockZ());
                                arena4.setLocation(teamType + ".area.max_point", max);
                                arena4.setLocation(teamType + ".area.min_point", min);
                                player.sendMessage(Main.prefix + Step.TEAM_AREA.getSuccessMessage().replace("$a", arena4.getName()).replace("$b", teamType.getPlayerPlural()));
                                session2.remove(Step.TEAM_AREA, teamType);
                                session2.next();
                                return true;
                            }
                            else if (args[2].equalsIgnoreCase(LangConfiguration.getString("commands.setup.team.spawn.label"))) {
                                if (!player.getLocation().getWorld().getName().equals(arena4.getWorld().getName())) {
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.wrong_world").replace("$", arena4.getWorld().getName()));
                                    return true;
                                }
                                arena4.setLocation(teamType + ".spawn", player.getLocation());
                                player.sendMessage(Main.prefix + Step.TEAM_SPAWN.getSuccessMessage().replace("$a", arena4.getName()).replace("$b", teamType.getPlayerPlural()));
                                session2.remove(Step.TEAM_SPAWN, teamType);
                                session2.next();
                                return true;
                            }
                            else if (args[2].equalsIgnoreCase(LangConfiguration.getString("commands.setup.team.captain_spawn.label"))) {
                                if (!player.getLocation().getWorld().getName().equals(arena4.getWorld().getName())) {
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.wrong_world").replace("$", arena4.getWorld().getName()));
                                    return true;
                                }
                                arena4.setLocation(teamType + ".captain_spawn", player.getLocation());
                                player.sendMessage(Main.prefix + Step.TEAM_CAPTAIN_SPAWN.getSuccessMessage().replace("$a", arena4.getName()).replace("$b", teamType.getPlayerPlural()));
                                session2.remove(Step.TEAM_CAPTAIN_SPAWN, teamType);
                                session2.next();
                                return true;
                            }
                        }
                        if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.done.label"))) {
                            final SetupSession session2 = SetupSession.getSession(player);
                            if (session2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                                return true;
                            }
                            if (session2.hasNext()) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.done.error.not_ready"));
                                session2.next();
                                return true;
                            }
                            final Arena arena4 = session2.getArena();
                            arena4.setCannonMultiplier(1.0);
                            arena4.setShipHealths(ArenaConfiguration.getShipHealths(arena4));
                            ArenaConfiguration.getArenas().add(arena4);
                            ArenaConfiguration.save();
                            final BukkitScheduler scheduler = Bukkit.getScheduler();
                            final Main main = Main.getMain();
                            final Arena obj = arena4;
                            Objects.requireNonNull(obj);
                            scheduler.runTask(main, obj::updateSigns);
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.done").replace("$", arena4.getName()));
                            session2.end();
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.cancel.label"))) {
                            final SetupSession session2 = SetupSession.getSession(player);
                            if (session2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.not_started"));
                                return true;
                            }
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.cancel").replace("$", session2.getArena().getName()));
                            session2.end();
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.delete.label"))) {
                            if (args.length < 3) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                return true;
                            }
                            if (!Main.games.isEmpty()) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.reload_config.error.games_running"));
                                return true;
                            }
                            try {
                                Configuration.reload();
                            }
                            catch (Exception e2) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.admin.reload_config.error.fatal"));
                                Logging.getLogger().log(Level.SEVERE, "RELOADING(delete) FATAL: config error", e2);
                                e2.printStackTrace();
                                Bukkit.getPluginManager().disablePlugin(Main.getMain());
                                return true;
                            }
                            final Arena arena2 = ArenaConfiguration.getArena(args[2]);
                            if (arena2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                                return true;
                            }
                            ArenaConfiguration.getArenas().remove(arena2);
                            ArenaConfiguration.save();
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.delete").replace("$", arena2.getName()));
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.recalculate.label"))) {
                            if (args.length < 3) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                return true;
                            }
                            final Arena arena2 = ArenaConfiguration.getArena(args[2]);
                            if (arena2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                                return true;
                            }
                            final int shipHealths = ArenaConfiguration.getShipHealths(arena2);
                            arena2.setShipHealths(shipHealths);
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.recalculate").replace("$a", arena2.getName()).replace("$b", shipHealths + ""));
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.update_area.label"))) {
                            if (!Main.getMain().getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.we_not_found"));
                                return true;
                            }
                            if (args.length < 3) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                return true;
                            }
                            final Arena arena2 = ArenaConfiguration.getArena(args[2]);
                            if (arena2 == null) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                                return true;
                            }
                            final SchematicOperation operation2 = new SchematicOperation();
                            operation2.save(arena2.getName(), arena2.getLocation("area.min_point"), arena2.getLocation("area.max_point"));
                            if (operation2.getOutcome() == SchematicOperation.Outcome.ERROR) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.area.error.schematic"));
                                return true;
                            }
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.update_area").replace("$", arena2.getName()));
                            return true;
                        }
                        else {
                            if (!args[1].equalsIgnoreCase(LangConfiguration.getString("commands.setup.cannon_multiplier.label"))) {
                                sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                return true;
                            }
                            if (args.length < 4) {
                                player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                return true;
                            }
                            final String name = args[2];
                            for (final Arena arena3 : ArenaConfiguration.getArenas()) {
                                if (arena3.getName().equalsIgnoreCase(name)) {
                                    double multiplier;
                                    try {
                                        multiplier = Double.parseDouble(args[3]);
                                    }
                                    catch (NumberFormatException e4) {
                                        player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.bad_args"));
                                        return true;
                                    }
                                    arena3.setCannonMultiplier(multiplier);
                                    player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.cannon_multiplier").replace("$a", arena3.getName()).replace("$b", multiplier + ""));
                                    return true;
                                }
                            }
                            player.sendMessage(Main.prefix + LangConfiguration.getString("commands.setup.error.arena_not_found"));
                            return true;
                        }
                    }
                }
            }
        }
        sender.sendMessage(Main.prefix + LangConfiguration.getString("commands.error.bad_args"));
        return true;
    }
}
