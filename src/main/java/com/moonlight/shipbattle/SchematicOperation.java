package com.moonlight.shipbattle;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.moonlight.shipbattle.configuration.LangConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class SchematicOperation
{
    private Outcome outcome;
    private final File folder;
    
    SchematicOperation() {
        this.outcome = Outcome.NONE;
        this.folder = new File(Main.getMain().getDataFolder(), "/schematics/");
    }
    
    Outcome getOutcome() {
        return this.outcome;
    }
    
    void load(String name, World w) {
        final WorldEditPlugin we = (WorldEditPlugin)Bukkit.getPluginManager().getPlugin("WorldEdit");
        
        File file = new File(this.folder, name + ".schem"); // prioritize this format
        if (file.exists()) {
        	loadSchem(we, name, file, w);
        	return;
        }
        
        file = new File(this.folder, name + ".schematic");
        if (file.exists()) {
        	loadSchematic(we, name, file, w);
        	return;
        }
        
        this.outcome = Outcome.FILE_NOT_FOUND;
        Main.getMain().getLogger().severe("[Haj\u00f3Csata] " + LangConfiguration.getString("schematic.error.file_not_found").replace("$a", name));
    }
    
    private void loadSchem(WorldEditPlugin we, String name, File file, World w) {
        final EditSession editSession = we.getWorldEdit().newEditSessionBuilder().world(new BukkitWorld(w)).maxBlocks(-1).build();
        editSession.getExtent().enableQueue();
        try {
            ClipboardReader reader = BuiltInClipboardFormat.FAST.getReader(new FileInputStream(file));
            Clipboard clipboard = reader.read();
            ClipboardHolder ch = new ClipboardHolder(clipboard);
            Main.getMain().getLogger().info(clipboard.getOrigin().toParserString());
            Operation operation = ch.createPaste(editSession.getSurvivalExtent()).to(clipboard.getOrigin()).ignoreAirBlocks(false).build();
            Operations.complete(operation);
            this.outcome = Outcome.SUCCESS;
            ch.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            this.outcome = Outcome.ERROR;
            Main.getMain().getLogger().severe("[Haj\u00f3Csata] " + LangConfiguration.getString("schematic.error").replace("$a", name).replace("$b", e.getMessage()));
        } finally {
        	editSession.close();
        }
    }
    
    private void loadSchematic(WorldEditPlugin we, String name, File file, World w) {
        final EditSession editSession = we.getWorldEdit().newEditSessionBuilder().world(new BukkitWorld(w)).maxBlocks(-1).build();
        editSession.getExtent().enableQueue();
        try {
            ClipboardReader reader = BuiltInClipboardFormat.MCEDIT_SCHEMATIC.getReader(new FileInputStream(file));
            Clipboard clipboard = reader.read();
            ClipboardHolder ch = new ClipboardHolder(clipboard);
            Operation operation = ch.createPaste(editSession.getSurvivalExtent()).to(clipboard.getOrigin()).ignoreAirBlocks(false).build();
            Operations.complete(operation);
            this.outcome = Outcome.SUCCESS;
            ch.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            this.outcome = Outcome.ERROR;
            Main.getMain().getLogger().severe("[Haj\u00f3Csata] " + LangConfiguration.getString("schematic.error").replace("$a", name).replace("$b", e.getMessage()));
        } finally {
        	editSession.close();
        }
    }
    
    void save(String name, Location minPoint, Location maxPoint) {
        try {
            if (!this.folder.exists() || !this.folder.isDirectory()) {
                this.folder.mkdirs();
            }
            File schematic = new File(this.folder, name + ".schem");
            WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            EditSession editSession = we.getWorldEdit().newEditSessionBuilder().world(new BukkitWorld(minPoint.getWorld())).maxBlocks(-1).build();
            CuboidRegion region = new CuboidRegion(BlockVector3.at(minPoint.getX(), minPoint.getY(), minPoint.getZ()), BlockVector3.at(maxPoint.getX(), maxPoint.getY(), maxPoint.getZ()));
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            Operations.complete(forwardExtentCopy);
            ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(schematic));
            writer.write(clipboard);
            writer.close();
            editSession.close();
            this.outcome = Outcome.SUCCESS;
        }
        catch (IOException | WorldEditException e) {
            e.printStackTrace();
            this.outcome = Outcome.ERROR;
        }
    }
    
    enum Outcome
    {
        NONE, 
        SUCCESS, 
        FILE_NOT_FOUND, 
        ERROR;
    }
}
