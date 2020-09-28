package me.nicbo.invadedlandsevents.scoreboard.line;

import me.nicbo.invadedlandsevents.util.StringUtils;

/**
 * A line on the EventScoreboard
 *
 * @author Nicbo
 * @see me.nicbo.invadedlandsevents.scoreboard.EventScoreboard
 */

public class Line {
    private final String team, base;
    private String prefix, suffix;
    private final int line;

    /**
     * Creates instance of a line
     *
     * @param team   the name of the team to register with the line
     * @param prefix the first part of the line
     * @param base   the middle of the line (can not be the same twice on one scoreboard)
     * @param suffix the last part of the line
     * @param line   the line number
     */
    public Line(String team, String prefix, String base, String suffix, int line) {
        this.team = team;
        this.base = StringUtils.colour(base);
        this.prefix = StringUtils.colour(prefix);
        this.suffix = StringUtils.colour(suffix);
        this.line = line;
    }

    public String getTeam() {
        return team;
    }

    public String getBase() {
        return base;
    }

    public String getPrefix() {
        return prefix;
    }

    protected void setPrefix(String prefix) {
        this.prefix = StringUtils.colour(prefix);
    }

    public String getSuffix() {
        return suffix;
    }

    protected void setSuffix(String suffix) {
        this.suffix = StringUtils.colour(suffix);
    }

    public int getLine() {
        return line;
    }
}