package me.nicbo.invadedlandsevents.util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for strings
 *
 * @author Nicbo
 */

public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Turns a location into a readable string minus the world
     *
     * @param location the location
     * @return the string
     */
    public static String locationToString(Location location) {
        return "(" + String.format("%.2f", location.getX()) + ", " +
                String.format("%.2f", location.getY()) + ", " +
                String.format("%.2f", location.getZ()) + ")" + " [" +
                String.format("%.2f", location.getYaw()) + ", " +
                String.format("%.2f", location.getPitch()) + "]";
    }

    /**
     * Turns a location into a readable string
     *
     * @param location the location
     * @return the string
     */
    public static String fullLocationToString(Location location) {
        World world = location.getWorld();
        return "(" + (world == null ? "null" : world.getName()) + ": " +
                String.format("%.2f", location.getX()) + ", " +
                String.format("%.2f", location.getY()) + ", " +
                String.format("%.2f", location.getZ()) + ")" + " [" +
                String.format("%.2f", location.getYaw()) + ", " +
                String.format("%.2f", location.getPitch()) + "]";
    }

    /**
     * Turns a block vector into a readable string
     *
     * @param blockVector the block vector
     * @return the string
     */
    public static String blockVectorToString(BlockVector blockVector) {
        return "(" + blockVector.getBlockX() + ", " + blockVector.getBlockY() + ", " + blockVector.getBlockZ() + ")";
    }

    /**
     * Colours a message
     *
     * @param message the message to be coloured
     * @return the coloured message
     */
    public static String colour(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Colours all messages in a list
     *
     * @param list the list of strings to be coloured
     * @return the coloured strings
     */
    public static List<String> colour(List<String> list) {
        List<String> translated = new ArrayList<>();
        for (String str : list) {
            translated.add(colour(str));
        }
        return translated;
    }

    /**
     * Converts seconds into a string
     * Format: (hours)h (minutes)m (seconds)s
     *
     * @param totalSeconds the amount of seconds to be converted
     * @return the formatted string
     */
    public static String formatSeconds(long totalSeconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        long seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        long totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        long minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        long hours = totalMinutes / MINUTES_IN_AN_HOUR;

        StringBuilder builder = new StringBuilder();

        if (hours != 0) {
            builder.append(hours).append("h ");
        }
        if (minutes != 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds != 0) {
            builder.append(seconds).append("s ");
        }

        // Remove last char of string if possible (trailing space)
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
