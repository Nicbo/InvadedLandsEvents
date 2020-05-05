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

    public static <T> T getRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public static List<Player> getPlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static String formatSeconds(int secondsInt) { // CHANGE LATER format should be like 1:03
        long seconds = secondsInt;
        long days = seconds / (24 * 60 * 60);
        seconds %= 24 * 60 * 60;
        long hh = seconds / (60 * 60);
        seconds %= 60 * 60;
        long mm = seconds / 60;
        seconds %= 60;
        long ss = seconds;

        if (days > 0) {
            return days + ":" + hh + ":" + mm + ":" + ss;
        }

        if (hh > 0) {
            return hh + ":" + mm + ":" + ss;
        }

        if (mm > 0) {
            return mm + ":" + ss;
        }

        return "00:" + ss;
    }
}
