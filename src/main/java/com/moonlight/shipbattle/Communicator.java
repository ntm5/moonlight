package com.moonlight.shipbattle;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.moonlight.shipbattle.teams.Team;


public class Communicator
{
    private final Game game;
    
    Communicator(final Game game) {
        this.game = game;
    }
    
    public void broadcastMessage(final String message) {
        for (final Player player : this.game.getPlayers()) {
            player.sendMessage(Main.prefix + message);
        }
    }
    
    void broadcastMessage(final String message, final Team team) {
        for (final Player player : team.getAllPlayers()) {
            player.sendMessage(Main.prefix + message);
        }
    }
    
    public void broadcastTitle(final String title, final String subtitle, final TitleLength length) {
        for (final Player player : this.game.getPlayers()) {
            this.sendTitle(player, title, subtitle, length);
        }
    }
    
    void sendTitle(final Player player, final String title, final String subtitle, final TitleLength length) {
        player.sendTitle(title, subtitle, length.fadeIn, length.stay, length.fadeOut);
    }
    
    void broadcastActionBar(final String message) {
        for (final Player player : this.game.getPlayers()) {
            this.sendActionBar(player, message);
        }
    }
    
    public void broadcastActionBar(final String message, final Team team) {
        for (final Player player : team.getPlayers()) {
            this.sendActionBar(player, message);
        }
    }
    
    public void sendActionBar(final Player player, final String message) {
        final ClientboundChatPacket packet = new ClientboundChatPacket(Component.Serializer.fromJson("{\"text\": \"" + message + "\"}"), ChatType.GAME_INFO, player.getUniqueId());
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
    
    public void sendNotification(final Player player, final String message) {
        this.sendActionBar(player, message);
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
    }
    
    public enum TitleLength
    {
        MEDIUM(10, 80, 10), 
        LONG(10, 160, 10), 
        ONLY_FADE_IN(5, 20, 0), 
        ONLY_STAY(0, 25, 0), 
        ONLY_FADE_OUT(0, 20, 5);
        
        final int fadeIn;
        final int stay;
        final int fadeOut;
        
        private TitleLength(final int fadeIn, final int stay, final int fadeOut) {
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }
    }
}
