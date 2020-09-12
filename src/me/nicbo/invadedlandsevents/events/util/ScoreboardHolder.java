package me.nicbo.invadedlandsevents.events.util;

import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Used to hold the scoreboard previously used by the player
 * so that when they leave the event they can be given back
 * their scoreboard
 *
 * @author Nicbo
 */

public final class ScoreboardHolder {
    private Scoreboard previousScoreboard;
    private EventScoreboard currentScoreboard;

    /**
     * Constructor for ScoreboardHolder
     *
     * @param previousScoreboard the scoreboard they had before the event
     * @param currentScoreboard the event scoreboard they are using
     */
    public ScoreboardHolder(Scoreboard previousScoreboard, EventScoreboard currentScoreboard) {
        this.previousScoreboard = previousScoreboard;
        this.currentScoreboard = currentScoreboard;
    }

    public Scoreboard getPreviousScoreboard() {
        return previousScoreboard;
    }

    public void setPreviousScoreboard(Scoreboard previousScoreboard) {
        this.previousScoreboard = previousScoreboard;
    }

    public EventScoreboard getCurrentScoreboard() {
        return currentScoreboard;
    }

    public void setCurrentScoreboard(EventScoreboard currentScoreboard) {
        this.currentScoreboard = currentScoreboard;
    }
}