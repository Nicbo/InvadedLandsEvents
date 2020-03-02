package me.nicbo.InvadedLandsEvents.utils;

import java.util.Random;

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
}
