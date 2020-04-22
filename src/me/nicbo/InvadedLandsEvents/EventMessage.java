package me.nicbo.InvadedLandsEvents;

import org.bukkit.ChatColor;

/**
 * All event messages
 *
 * @author Nicbo
 * @author StarZorroww
 * @author thehydrogen
 * @since 2020-03-12
 */

public enum EventMessage {
    NONE(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("NONE"))),
    STARTED(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("STARTED"))),
    ENDED(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("ENDED"))),
    SPECTATING(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("SPECTATING"))),
    JOINED_EVENT(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("JOINED_EVENT"))),
    LEFT_EVENT(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("LEFT_EVENT"))),
    EVENT_FORCE_ENDED(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("LEFT_EVENT"))),
    HOST_STARTED(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("HOST_STARTED"))),
    EVENT_ENDING(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("EVENT_ENDING"))),
    IN_EVENT(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("IN_EVENT"))),
    DOES_NOT_EXIST(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("404"))),
    NOT_ENABLED(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("DISABLED"))),
    NOT_IN_EVENT(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("NOT_IN_EVENT"))),
    EMPTY_INVENTORY(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("EMPTY_INV"))),
    CRAFT_IN_EVENT(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("CRAFT_IN_EVENT"))),
    NO_PERMISSION(ChatColor.translateAlternateColorCodes('&', EventsMain.messages.getConfig().getString("NO_PERMS")));

    final String message;

    EventMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}