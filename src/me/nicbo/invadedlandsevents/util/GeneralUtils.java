package me.nicbo.invadedlandsevents.util;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * Utility class for general purposes
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class GeneralUtils {
    private static final Random random;

    static {
        random = new Random();
    }

    private GeneralUtils() {
    }

    /**
     * Returns a random number between two numbers (inclusive)
     *
     * @param min the minimum number
     * @param max the maximum number
     * @return the random number
     */
    public static int randomMinMax(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Returns a random boolean
     *
     * @return true or false
     */
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns a random element in a list
     *
     * @param list the list to pick the value from
     * @param <T>  the type that is returned
     * @return the random element
     * @throws IllegalArgumentException if the list is empty
     */
    public static <T> T getRandom(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Can't get random element from an empty list");
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Sends messages to the player
     *
     * @param player   the player to send the messages to
     * @param messages the messages
     */
    public static void sendMessages(Player player, Iterable<String> messages) {
        for (String message : messages) {
            player.sendMessage(message);
        }
    }
}
