package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for strings.
 *
 * @author Nicbo
 */
public final class StringUtils {
    private StringUtils() {
    }

    public static String locationToString(Location location) {
        return locationToString(location, true);
    }

    public static String locationToString(Location location, boolean includeYawPitch) {
        if (location == null) {
            return "null";
        }

        World world = location.getWorld();

        StringBuilder builder = new StringBuilder()
                .append("(")
                .append(world == null ? "null" : world.getName())
                .append(", ")
                .append(String.format("%.2f", location.getX()))
                .append(", ")
                .append(String.format("%.2f", location.getY()))
                .append(", ")
                .append(String.format("%.2f", location.getZ()))
                .append(")");

        if (includeYawPitch) {
            builder.append(" [")
                    .append(String.format("%.2f", location.getYaw()))
                    .append(", ")
                    .append(String.format("%.2f", location.getPitch()))
                    .append("]");
        }

        return builder.toString();
    }

    public static String colour(String message) {
        Validate.checkArgumentNotNull(message, "message");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colour(List<String> messages) {
        Validate.checkArgumentNotNull(messages, "messages");
        List<String> translated = new ArrayList<>();
        for (String str : messages) {
            translated.add(colour(str));
        }
        return translated;
    }

    public static String formatSeconds(long totalSeconds) {
        long totalSecondsAbs = Math.abs(totalSeconds);
        long seconds = totalSecondsAbs % 60;
        long totalMinutes = totalSecondsAbs / 60;
        long minutes = totalMinutes % 60;
        long hours = totalMinutes / 60;

        StringBuilder builder = new StringBuilder();

        if (totalSeconds < 0) {
            builder.append("-");
        }
        if (hours != 0) {
            builder.append(hours).append("h ");
        }
        if (minutes != 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds != 0 || totalSecondsAbs == 0) {
            builder.append(seconds).append("s ");
        }

        // trim trailing whitespace
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    public static boolean equalsAnyIgnoreCase(String target, String... strings) {
        Validate.checkArgumentNotNull(target, "target");
        Validate.checkArgumentNotNull(strings, "strings");
        for (String string : strings) {
            if (target.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    public static String formatStringList(List<String> strings) {
        Validate.checkArgumentNotNull(strings, "strings");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            builder.append(ChatColor.YELLOW)
                    .append(strings.get(i))
                    .append(ChatColor.GOLD)
                    .append(i < strings.size() - 1 ? ", " : ".");
        }

        return builder.toString();
    }
}
