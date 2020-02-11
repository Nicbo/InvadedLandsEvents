package me.nicbo.InvadedLandsEvents.utils;

public final class GeneralUtils {
    public static boolean containsString(String[] array, String str) {
        for (String string : array) {
            if (string.equals(str)) {
                return true;
            }
        }
        return false;
    }
}
