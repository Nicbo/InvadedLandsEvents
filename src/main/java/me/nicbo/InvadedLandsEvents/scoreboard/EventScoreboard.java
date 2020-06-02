package me.nicbo.InvadedLandsEvents.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;

/**
 * Flickerless scoreboard class
 * Inspired by knoapp
 *
 * @author Nicbo
 * @since 2020-05-04
 */

public abstract class EventScoreboard {
    protected Player player;
    private Scoreboard scoreboard;
    private Objective objective;

    private List<Line> lines;

    public static final String HEADERFOOTER;

    static {
        HEADERFOOTER = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH.toString() + "----------";
    }

    public EventScoreboard(Player player, String name) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(name, "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void init(String title, Line... lines) {
        this.objective.setDisplayName(title);
        this.lines = Arrays.asList(lines);

        for (Line line : lines) {
            if (line.getTeam() != null) {
                Team team = scoreboard.registerNewTeam(line.getTeam());
                team.addEntry(line.getBase());
                team.setPrefix(line.getPrefix());
                team.setSuffix(line.getSuffix());
            }
            objective.getScore(line.getBase()).setScore(line.getLine());
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public abstract void refresh();

    public void updateScoreboard() {
        for (Line line : lines) {
            if (line instanceof TrackRow) {
                Team team = scoreboard.getTeam(line.getTeam());
                team.setPrefix(line.getPrefix());
                team.setSuffix(line.getSuffix());
            }
        }
    }

    protected static class Row extends Line {
        public Row(String team, String prefix, String base, String suffix, int line) {
            super(team, prefix, base, suffix, line);
        }
    }

    protected static class TrackRow extends Line {
        public TrackRow(String team, String prefix, String base, String suffix, int line) {
            super(team, prefix, base, suffix, line);
        }

        public void setPrefix(String prefix) {
            super.prefix = prefix;
        }

        public void setSuffix(String suffix) {
            super.suffix = suffix;
        }
    }

    protected static class Line {
        protected String team, base;
        protected String prefix, suffix;
        protected int line;

        protected Line(String team, String prefix, String base, String suffix, int line) {
            this.team = team;
            this.base = base;
            this.prefix = prefix;
            this.suffix = suffix;
            this.line = line;
        }

        public String getTeam() {
            return team;
        }

        public String getBase() {
            return base;
        }

        public int getLine() {
            return line;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    /*
    TODO:
        - Add way to remove line
     */
}
