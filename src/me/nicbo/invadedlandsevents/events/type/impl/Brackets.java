package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.DuelEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.misc.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * Two players are teleported each round
 * Whoever wins the duel moves on, the other player is eliminated
 * Last player standing wins
 *
 * @author Nicbo
 */

public final class Brackets extends DuelEvent {
    private final ItemStack[] armour;
    private final ItemStack[] kit;

    private Pair<Player, Player> fighters;

    public Brackets(InvadedLandsEvents plugin) {
        super(plugin, "1v1 Brackets", "brackets", "BRACKETS");

        this.armour = new ItemStack[]{
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET)
        };

        this.kit = new ItemStack[]{
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };
    }

    private void playerWonRound(Player winner, Player loser) {
        broadcastEventMessage(Message.BRACKETS_ELIMINATED.get()
                .replace("{winner}", winner.getName())
                .replace("{loser}", loser.getName())
                .replace("{remaining}", String.valueOf(getPlayersSize() - 1)));

        endRound();

        SpigotUtils.clear(winner);
        givePlayerStar(winner);
        winner.teleport(getSpecLoc());

        loseEvent(loser);
    }

    private void loseRound(Player loser) {
        if (loser.equals(fighters.getRight())) {
            playerWonRound(fighters.getLeft(), fighters.getRight());
        } else {
            playerWonRound(fighters.getRight(), fighters.getLeft());
        }
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);

        if (isRunning() && fightingPlayers.contains(player)) {
            loseEvent(player);
        }
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return BracketsSB::new;
    }

    @Override
    protected Collection<Player> prepareRound() {
        // Just in case, if the list size is one than the while loop while crash the server
        checkPlayerCount();
        if (!isRunning()) {
            return null;
        }

        Player player1 = GeneralUtils.getRandom(getPlayersView());

        Player player2;
        do {
            player2 = GeneralUtils.getRandom(getPlayersView());
        } while (player1.equals(player2));

        this.fighters = new Pair<>(player1, player2);

        broadcastEventMessage(Message.BRACKETS_MATCH_STARTING.get()
                .replace("{player1}", player1.getName())
                .replace("{player2}", player2.getName()));

        player1.teleport(startLoc1);
        player1.getInventory().setContents(kit);
        player1.getInventory().setArmorContents(armour);

        player2.teleport(startLoc2);
        player2.getInventory().setContents(kit);
        player2.getInventory().setArmorContents(armour);

        return Arrays.asList(player1, player2);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitB(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && !ignoreEvent((Player) event.getEntity())) {
            Player player = (Player) event.getEntity();
            if (player.getHealth() - event.getFinalDamage() <= 0) { // Damage will kill player
                SpigotUtils.clearInventory(player);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();

                    loseRound(player);
                }, 1);
            }
        }
    }

    private final class BracketsSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private final Line pickingPlayers;
        private final Line blank;
        private final Line vs;

        private boolean showingFighters;

        private BracketsSB(Player player) {
            super(getConfigName(), player);

            this.pickingPlayers = new Line("ppBrackets", "&6Picking new ", "players...", "", 5);
            this.blank = new Line("bBrackets", "", "&b&b", "", 4);
            this.playerCountTrack = new TrackLine("pctBrackets", "&ePlayers: ", "&a&6", "", 3);
            this.specCountTrack = new TrackLine("sctBrackets", "&eSpectators: ", "&d&6", "", 2);

            this.vs = new Line("vsBrackets", "", "&evs.", "", 6);

            this.initLines(pickingPlayers, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            if (!showingFighters && isFighting()) {
                clearLines();

                Line player1 = new Line("p1Sumo", "", "&b&6", fighters.getLeft().getName(), 7);
                Line player2 = new Line("p2Sumo", "", "&c&6", fighters.getRight().getName(), 5);
                this.showingFighters = true;

                initLines(player1, vs, player2, blank, playerCountTrack, specCountTrack);
            } else if (showingFighters && !isFighting()) {
                clearLines();
                this.showingFighters = false;
                initLines(pickingPlayers, blank, playerCountTrack, specCountTrack);
            }

            this.playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            this.specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }
}
