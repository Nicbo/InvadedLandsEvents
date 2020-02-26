package me.nicbo.InvadedLandsEvents.scoreboards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class InvadedScoreBoard {
    private Scoreboard scoreboard;
    private Objective objective;
    private Team spectators;

    public InvadedScoreBoard(String name) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(name, "dummy");
        this.objective.setDisplayName(ChatColor.GOLD + name);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.spectators = scoreboard.registerNewTeam("spectators");
    }

    /*
    TODO:
        - Go on invaded and screenshot event scoreboards / record all events
     */
}
