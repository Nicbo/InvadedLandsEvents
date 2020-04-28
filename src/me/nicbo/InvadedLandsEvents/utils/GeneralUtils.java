package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for events
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public final class GeneralUtils {
    private static Random random;

    static {
        random = new Random();
    }

    private GeneralUtils() {}

    public static int randomMinMax(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static boolean isInt(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static List<Player> getPlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }
}
