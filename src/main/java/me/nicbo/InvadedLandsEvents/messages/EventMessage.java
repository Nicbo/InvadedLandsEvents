package me.nicbo.InvadedLandsEvents.messages;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.ChatColor;

/**
 * All event messages
 *
 * @author Nicbo
 * @author StarZorrow
 * @author thehydrogen
 * @since 2020-05-01
 */

public final class EventMessage {
    public static String NONE;
    public static String STARTED;
    public static String ENDED;
    public static String SPECTATING;
    public static String JOINED_EVENT;
    public static String LEFT_EVENT;
    public static String EVENT_FORCE_ENDED;
    public static String HOST_STARTED;
    public static String EVENT_ENDING;
    public static String IN_EVENT;
    public static String DOES_NOT_EXIST;
    public static String NOT_ENABLED;
    public static String NOT_IN_EVENT;
    public static String EMPTY_INVENTORY;
    public static String CRAFT_IN_EVENT;
    public static String NO_PERMISSION;

    public static void reload() {
        NONE = getStringFromConfig("NONE");
        STARTED = getStringFromConfig("STARTED");
        ENDED = getStringFromConfig("ENDED");
        SPECTATING = getStringFromConfig("SPECTATING");
        JOINED_EVENT = getStringFromConfig("JOINED_EVENT");
        LEFT_EVENT = getStringFromConfig("LEFT_EVENT");
        EVENT_FORCE_ENDED = getStringFromConfig("EVENT_FORCE_ENDED");
        HOST_STARTED = getStringFromConfig("HOST_STARTED");
        EVENT_ENDING = getStringFromConfig("EVENT_ENDING");
        IN_EVENT = getStringFromConfig("IN_EVENT");
        DOES_NOT_EXIST = getStringFromConfig("404");
        NOT_ENABLED = getStringFromConfig("DISABLED");
        NOT_IN_EVENT = getStringFromConfig("NOT_IN_EVENT");
        EMPTY_INVENTORY = getStringFromConfig("EMPTY_INV");
        CRAFT_IN_EVENT = getStringFromConfig("CRAFT_IN_EVENT");
        NO_PERMISSION = getStringFromConfig("NO_PERMS");
   }

    private static String getStringFromConfig(String path) {
        return ChatColor.translateAlternateColorCodes('&', EventsMain.getMessages().getConfig().getString("event." + path));
    }
}
