package ca.nicbo.invadedlandsevents.scoreboard;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;

/**
 * Represents a line on the {@link EventScoreboard}.
 *
 * @author Nicbo
 */
public class EventScoreboardLine {
    private static final int MAX_SECTION_LENGTH = 16; // TODO: Get max length based on NMSVersion

    private static int count;

    private final String team;
    private final String base;
    private final int lineNumber;

    private String prefix;
    private String suffix;

    // Empty line
    public EventScoreboardLine(int lineNumber) {
        this.team = "ile_" + count++; // Team name will never have a collision

        // Create base, assume that line numbers will never be the same
        StringBuilder builder = new StringBuilder();
        for (char c : String.valueOf(lineNumber).toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }

        this.base = builder.toString();
        this.lineNumber = lineNumber;
        this.prefix = "";
        this.suffix = "";
    }

    public EventScoreboardLine(int lineNumber, String text) {
        this(lineNumber);
        Validate.checkArgumentNotNull(text, "text");
        internalSetText(text);
    }

    private void internalSetText(String text) {
        // Colour the input text
        final String colouredText = StringUtils.colour(text);

        // Text is 16 or fewer characters, don't worry about splitting
        if (colouredText.length() <= 16) {
            this.prefix = colouredText;
            this.suffix = "";
            return;
        }

        // Create prefix and suffix
        String prefix = colouredText.substring(0, MAX_SECTION_LENGTH);
        String suffix = colouredText.substring(MAX_SECTION_LENGTH);

        // Prefix ends with colour char, this means that a colour code was split down the middle
        if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            // Remove trailing symbol and add it to the suffix, this will fix the issue
            prefix = prefix.substring(0, prefix.length() - 1);
            suffix = ChatColor.COLOR_CHAR + suffix;
        } else {
            // Just add the prefix ending colour code to the suffix
            suffix = ChatColor.getLastColors(prefix) + suffix;
        }

        // Handle overflow of suffix
        if (suffix.length() > MAX_SECTION_LENGTH) {
            suffix = suffix.substring(0, MAX_SECTION_LENGTH - 1) + "-";
        }

        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getTeam() {
        return team;
    }

    public String getBase() {
        return base;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setText(String text) {
        Validate.checkArgumentNotNull(text, "text");
        internalSetText(text);
    }
}
