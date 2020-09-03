package me.nicbo.invadedlandsevents.scoreboard;

import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Flickerless scoreboard for events
 * Inspired by knoapp on spigot
 *
 * @author Nicbo
 */

public abstract class EventScoreboard {
    private final String name;
    private final String title;
    private final Player player;

    private final Scoreboard scoreboard;
    private Objective objective;
    private Line[] lines;

    /**
     * Creates instance of EventScoreboard
     * Uses the message name to get the title from Message
     *
     * @param messageName the scoreboards message name
     * @param player the owner of the scoreboard
     * @see Message
     */
    protected EventScoreboard(String messageName, Player player) {
        this(messageName, Message.valueOf("TITLE_" + messageName.toUpperCase()).get(), player);
    }

    /**
     * Creates instance of EventScoreboard
     *
     * @param name the name of the objective
     * @param title the title of the scoreboard
     * @param player the owner of the scoreboard
     */
    protected EventScoreboard(String name, String title, Player player) {
        this.name = name;
        this.title = title;
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.player.setScoreboard(scoreboard);
        this.lines = new Line[0];

        Objective healthIndicator = scoreboard.registerNewObjective("health", "health");
        healthIndicator.setDisplayName(ChatColor.RED + "❤");
        healthIndicator.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    /**
     * Registers the objective
     */
    private void registerObjective() {
        this.objective = scoreboard.registerNewObjective(this.name, "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
    }

    /**
     * Used to initialize lines on the scoreboard
     *
     * @param lines the lines
     */
    protected void initLines(Line... lines) {
        registerObjective();
        this.lines = lines;

        final String LINE = "&7&m----------";
        Line header = new Line("header", LINE, "&7&7&7&7", LINE, lines.length + 2);
        Line footer = new Line("footer", LINE, "&8&8&8&8", LINE, 1);

        registerTeam(header);
        registerTeam(footer);

        for (Line line : lines) {
            if (line != null) {
                registerTeam(line);
            }
        }

        // Update so there is no delay
        updateScoreboard();
    }

    /**
     * Registers a team to the internal scoreboard using line properties
     *
     * @param line the line that the team is based on
     */
    private void registerTeam(Line line) {
        Team team = scoreboard.registerNewTeam(line.getTeam());
        team.addEntry(line.getBase());
        team.setPrefix(line.getPrefix());
        team.setSuffix(line.getSuffix());
        objective.getScore(line.getBase()).setScore(line.getLine());
    }


    /**
     * Unregisters and clears all the lines on the scoreboard
     */
    protected void clearLines() {
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        this.objective.unregister();
    }

    /**
     * What to do before updating the scoreboard
     */
    protected abstract void refresh();

    /**
     * Refreshes scoreboard and updates the lines
     */
    public void updateScoreboard() {
        refresh();
        for (Line line : lines) {
            if (line instanceof TrackLine) {
                Team team = scoreboard.getTeam(line.getTeam());
                team.setPrefix(line.getPrefix());
                team.setSuffix(line.getSuffix());
            }
        }
    }

    public Player getPlayer() {
        return player;
    }
}
