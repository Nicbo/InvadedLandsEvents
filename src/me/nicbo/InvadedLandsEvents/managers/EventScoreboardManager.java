package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

/**
 * Manages EventScoreboards
 * @author Nicbo
 * @since 2020-05-04
 */

public final class EventScoreboardManager {
    private EventManager eventManager;
    private HashMap<Player, EventScoreboard> scoreboards;

    public EventScoreboardManager(EventManager eventManager) {
        this.eventManager = eventManager;
        this.scoreboards = new HashMap<>();
    }

    public void refreshScoreboards() { //runnable will call this

    }

    public void giveSpectatorScoreboard(List<Player> spectators) {

    }

    public void giveWaterdropScoreboard(List<Player> players) { //for testing its not how its gonna work
        for (Player player : players) {
            EventScoreboard scoreboard = new EventScoreboard(player);
            scoreboard.setDisplayName("Waterdrop");
            scoreboard.addLine("Players: " + eventManager.getCurrentEvent().getPlayers().size());
            scoreboard.addLine("Spectators: " + eventManager.getCurrentEvent().getSpectators().size());
            scoreboards.put(player, scoreboard);
            player.setScoreboard(scoreboard.getScoreboard());
        }
    }
}
