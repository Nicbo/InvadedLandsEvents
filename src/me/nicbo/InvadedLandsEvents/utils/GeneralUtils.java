package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Utility class for general purposes
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-02-11
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
        return list.isEmpty() ? null : list.get(random.nextInt(list.size()));
    }

    public static boolean contains(Object[] arr, Object o) {
        for (Object obj : arr) {
            if (obj.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public static String formatSeconds(int sec) {
        int minutes = sec / 60;
        int seconds = sec - minutes * 60;
        return (minutes == 0 ? "" : minutes + "m ") + (seconds == 0 ? "" : seconds + "s");
    }

    public static List<String> translateAlternateColorCodes(List<String> list) {
        return list.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
    }
}
