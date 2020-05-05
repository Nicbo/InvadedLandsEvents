package me.nicbo.InvadedLandsEvents.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Handles teams and scoreboard
 *
 * @author Nicbo
 * @since 2020-05-04
 */

public final class EventScoreboard {
    private Scoreboard scoreboard;
    private Objective objective;

    private int lines;

    public EventScoreboard(Player player) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("event", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.lines = 0;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setDisplayName(String name) {
        this.objective.setDisplayName(name);
    }

    public void addLine(String line) {
        Score score = objective.getScore(line);
        score.setScore(lines++);
    }

    /*
    TODO:
        - Make configurable in config.yml
     */
}
