package me.nicbo.invadedlandsevents.events;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Nicbo
 */

public final class EventScoreboardManager {
    private final Function<Player, EventScoreboard> countdownFactory;
    private final Function<Player, EventScoreboard> eventEndedFactory;
    private final Function<Player, EventScoreboard> eventScoreboardFactory;

    private final Map<Player, EventScoreboard> scoreboards;
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
                for (EventScoreboard scoreboard : scoreboards.values()) {
                    scoreboard.updateScoreboard();
                }
            }
        };
    }

    /**
     * Give the player a fresh scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveNewScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Gives the players a fresh scoreboard
     *
     * @param players the players to give the scoreboard to
     */
    public void giveNewScoreboard(Iterable<Player> players) {
        for (Player player : players) {
            giveNewScoreboard(player);
        }
    }

    /**
     * Gives the player the current events scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveEventScoreboard(Player player) {
        scoreboards.put(player, eventScoreboardFactory.apply(player));
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
        scoreboards.put(player, countdownFactory.apply(player));
    }

    /**
     * Gives the player an event over scoreboard
     *
     * @param player the player to give the scoreboard to
     */
    public void giveEventOverSB(Player player) {
        scoreboards.put(player, eventEndedFactory.apply(player));
    }

    /**
     * Gives the players an event over scoreboard
     *
     * @param players the players to give the scoreboard to
     */
    public void giveEventOverSB(Iterable<Player> players) {
        for (Player player : players) {
            giveEventOverSB(player);
        }
    }

    /**
     * Removes a players scoreboard and gives them a fresh one
     *
     * @param player the player to remove the scoreboard from
     */
    public void removeScoreboard(Player player) {
        giveNewScoreboard(player);
        scoreboards.remove(player);
    }

    /**
     * Removes all players scoreboards and gives them all a fresh one
     */
    public void removeAllScoreboards() {
        for (Iterator<Player> iterator = scoreboards.keySet().iterator(); iterator.hasNext(); iterator.remove()) {
            giveNewScoreboard(iterator.next());
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
