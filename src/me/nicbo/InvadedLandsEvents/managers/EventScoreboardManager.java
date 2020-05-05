package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import org.bukkit.entity.Player;

import java.util.HashMap;

public final class EventScoreboardManager {
    private EventManager eventManager;
    private HashMap<Player, EventScoreboard> scoreboards;

    public EventScoreboardManager() {
        this.scoreboards = new HashMap<>();
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void refreshScoreboards() {

    }

    public void giveSpectatorScoreboard(Player player) { // take in list?

    }

    public void giveWaterdropScoreboard(Player player) {
        EventScoreboard scoreboard = new EventScoreboard(player);
        scoreboard.setDisplayName("Waterdrop");
        scoreboard.addLine("Players: " + eventManager.getCurrentEvent().getPlayers().size());
        scoreboard.addLine("Spectators: " + eventManager.getCurrentEvent().getSpectators().size());
        scoreboards.put(player, scoreboard);
        player.setScoreboard(scoreboard.getScoreboard());
    }
}
