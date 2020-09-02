package me.nicbo.invadedlandsevents.scoreboard.line;

/**
 * A line that can be updated
 *
 * @author Nicbo
 * @see Line
 */

public class TrackLine extends Line {
    public TrackLine(String team, String prefix, String base, String suffix, int line) {
        super(team, prefix, base, suffix, line);
    }

    @Override
    public void setPrefix(String prefix) {
        super.setPrefix(prefix);
    }

    @Override
    public void setSuffix(String suffix) {
        super.setSuffix(suffix);
    }
}
