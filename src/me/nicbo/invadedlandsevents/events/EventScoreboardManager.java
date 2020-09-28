package me.nicbo.invadedlandsevents.events;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.util.ScoreboardHolder;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages the current event's scoreboards
 *
 * @author Nicbo
 */

public final class EventScoreboardManager {
    private final Function<Player, EventScoreboard> countdownFactory;
    private final Function<Player, EventScoreboard> eventEndedFactory;
    private final Function<Player, EventScoreboard> eventScoreboardFactory;

    private final Map<Player, ScoreboardHolder> scoreboards;

    private final BukkitRunnable refresher;

    /**
     * Creates instance of the event scoreboard manager
     *
     * @param countdownFactory the function to create countdown scoreboards
     * @param eventEndedFactory the function to create event ended scoreboards
     * @param eventScoreboardFactory the function to create the current event's scoreboards
     */
    public EventScoreboardManager(Function<Player, EventScoreboard> countdownFactory,
                                   Function<Player, EventScoreboard> eventEndedFactory,
                                   Function<Player, EventScoreboard> eventScoreboardFactory) {
        this.countdownFactory = countdownFactory;
        this.eventEndedFactory = eventEndedFactory;
        this.eventScoreboardFactory = eventScoreboardFactory;
        this.scoreboards = new HashMap<>();
        this.refresher = new BukkitRunnable() {
            @Override
            public void run() {
                for (ScoreboardHolder holder : scoreboards.values()) {
                    holder.getCurrentScoreboard().updateScoreboard();
                }
            }
        };
    }

    /**
     * Give the player their old scoreboard back
     *
     * @param player the player to give the scoreboard to
     */
    private void givePreviousScoreboard(Player player) {
        ScoreboardHolder holder = scoreboards.get(player);
        Scoreboard scoreboard = holder == null ? Bukkit.getScoreboardManager().getNewScoreboard() : holder.getPreviousScoreboard();
        player.setScoreboard(scoreboard);
    }

    /**
     * Gives the players their old scoreboards back
     *
     * @param players the players to give the scoreboard to
     */
    private void givePreviousScoreboard(Iterable<Player> players) {
        for (Player player : players) {
            givePreviousScoreboard(player);
        }
    }

    private void giveScoreboard(Player player, Function<Player, EventScoreboard> scoreboardFactory) {
        ScoreboardHolder holder = scoreboards.get(player);

        if (holder == null) {
            holder = new ScoreboardHolder(player.getScoreboard(), scoreboardFactory.apply(player));
            scoreboards.put(player, holder);
        } else {
            holder.setCurrentScoreboard(scoreboardFactory.apply(player));
        }
    }

    /**
     * Gives the player the current events scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveEventScoreboard(Player player) {
        giveScoreboard(player, eventScoreboardFactory);
    }

    /**
     * Gives the players the current events scoreboard
     *
     * @param players the players to give the scoreboard to
     */
    public void giveEventScoreboard(Iterable<Player> players) {
        for (Player player : players) {
            giveEventScoreboard(player);
        }
    }

    /**
     * Gives the player a countdown scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveCountdownSB(Player player) {
        giveScoreboard(player, countdownFactory);
    }

    /**
     * Gives the player an event ended scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveEventEndedSB(Player player) {
        giveScoreboard(player, eventEndedFactory);
    }

    /**
     * Gives the players an event ended scoreboard
     *
     * @param players the players to give the scoreboard to
     */
    public void giveEventEndedSB(Iterable<Player> players) {
        for (Player player : players) {
            giveEventEndedSB(player);
        }
    }

    /**
     * Removes a players scoreboard and gives them their previous one
     *
     * @param player the player to remove the scoreboard from
     */
    public void removeScoreboard(Player player) {
        givePreviousScoreboard(player);
        scoreboards.remove(player);
    }

    /**
     * Removes all players scoreboards and gives them all their previous ones
     */
    public void removeAllScoreboards() {
        for (Iterator<Player> iterator = scoreboards.keySet().iterator(); iterator.hasNext(); iterator.remove()) {
            givePreviousScoreboard(iterator.next());
        }
    }

    /**
     * Start refreshing the scoreboards
     *
     * @param plugin the instance of the main class
     */
    public void startRefreshing(InvadedLandsEvents plugin) {
        refresher.runTaskTimer(plugin, 0, 5);
    }

    /**
     * Stop refreshing the scoreboards
     */
    public void stopRefreshing() {
        refresher.cancel();
    }
}
