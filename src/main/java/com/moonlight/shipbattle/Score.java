package com.moonlight.shipbattle;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.configuration.LangConfiguration;

public class Score
{
    private static final HashMap<Player, Score> scores;
    private final HashSet<RewardEntry> specialRewards;
    private int destroyedBlocks;
    private int killedPlayers;
    
    public Score() {
        this.specialRewards = new HashSet<RewardEntry>();
    }
    
    private int getDestroyedBlocks() {
        return this.destroyedBlocks;
    }
    
    private int getKilledPlayers() {
        return this.killedPlayers;
    }
    
    public void addDestroyedBlocks(final int destroyedBlocks) {
        this.destroyedBlocks += destroyedBlocks;
    }
    
    public void addKilledPlayer() {
        ++this.killedPlayers;
    }
    
    public void addExtraReward(final RewardEntry rewardEntry) {
        this.specialRewards.add(rewardEntry);
    }
    
    static void setupScore(final Player player) {
        Score.scores.put(player, new Score());
    }
    
    static void reward(final Player player, final boolean winner, final Game game) {
        int reward = Configuration.participation;
        player.sendMessage(LangConfiguration.getString("rewards.header").replace("$", Configuration.participation + ""));
        final Score score = getScore(player);
        if (score.getKilledPlayers() > 0) {
            final int killedPlayers = (int)Math.round(score.getKilledPlayers() * Configuration.kill);
            player.sendMessage(LangConfiguration.getString("rewards.kills").replace("$a", score.getKilledPlayers() + "").replace("$b", killedPlayers + ""));
            reward += killedPlayers;
        }
        if (score.getDestroyedBlocks() > 5) {
            final int destroyedBlocks = (int)Math.round(score.getDestroyedBlocks() * Configuration.blockDestroy);
            player.sendMessage(LangConfiguration.getString("rewards.destroyed_blocks").replace("$a", score.getDestroyedBlocks() + "").replace("$b", destroyedBlocks + ""));
            reward += destroyedBlocks;
        }
        if (winner) {
            player.sendMessage(LangConfiguration.getString("rewards.win").replace("$", Configuration.win + ""));
            reward += Configuration.win;
        }
        if (score.specialRewards.contains(RewardEntry.FIRST_BLOOD)) {
            player.sendMessage(LangConfiguration.getString("rewards.first_blood").replace("$", Configuration.firstBlood + ""));
            reward += Configuration.firstBlood;
        }
        if (score.specialRewards.contains(RewardEntry.CAPTAIN_KILL)) {
            player.sendMessage(LangConfiguration.getString("rewards.captain_kill").replace("$", Configuration.captainKill + ""));
            reward += Configuration.captainKill;
        }
        player.sendMessage(LangConfiguration.getString("rewards.in_all").replace("$", reward + ""));
        game.getBalances().put(player, game.getBalances().get(player) + reward);
    }
    
    public static Score getScore(final Player player) {
        return Score.scores.get(player);
    }
    
    static {
        scores = new HashMap<Player, Score>();
    }
    
    public enum RewardEntry
    {
        CAPTAIN_KILL, 
        FIRST_BLOOD;
    }
}
