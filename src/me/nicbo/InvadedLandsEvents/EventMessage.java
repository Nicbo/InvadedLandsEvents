package me.nicbo.InvadedLandsEvents;

import org.bukkit.ChatColor;

/**
 * All event messages
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public enum EventMessage {
    NONE(ChatColor.RED + "There currently isn't any event active right now."),
    STARTED(ChatColor.RED + "You cannot join the event as it has already started!"),
    ENDED(ChatColor.GREEN + "You stopped the active event."),
    SPECTATING(ChatColor.YELLOW + "You are now " + ChatColor.GOLD + "spectating" + ChatColor.YELLOW + " the event."),
    JOINED_EVENT(ChatColor.GREEN + "" + ChatColor.BOLD + "{player} has joined the event"),
    LEFT_EVENT(ChatColor.RED + "" + ChatColor.BOLD + "{player} has left the event"),
    EVENT_FORCE_ENDED(ChatColor.YELLOW + "The " + ChatColor.GOLD + "{event}" + ChatColor.YELLOW + " event has been stopped manually."),
    HOST_STARTED(ChatColor.RED + "You cannot host an event as one is already in progress."),
    EVENT_ENDING(ChatColor.RED + "The event currently active is ending, please wait..."),
    IN_EVENT(ChatColor.RED + "You're already in the event."),
    DOES_NOT_EXIST(ChatColor.RED + "There is no event named " + ChatColor.YELLOW + "{event}" + ChatColor.RED + "."),
    NOT_ENABLED(ChatColor.RED + "That event is not enabled!"),
    NOT_IN_EVENT(ChatColor.RED + "You aren't in an event!"),
    EMPTY_INVENTORY(ChatColor.RED + "Your inventory must be empty to join an event!"),
    CRAFT_IN_EVENT(ChatColor.RED + "You're unable to craft in this state."),
    NO_PERMISSION(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command.");

    final String message;

    EventMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    /*
    TODO:
        - Make these all configurable in config file
     */
}