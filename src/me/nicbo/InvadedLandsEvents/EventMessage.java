package me.nicbo.InvadedLandsEvents;

import org.bukkit.ChatColor;

public enum EventMessage {
    NONE(ChatColor.RED + "There currently isn't any event active right now."),
    STARTED(ChatColor.RED + "You cannot join the event as it has already started!"),
    HOST_STARTED(ChatColor.RED + "You cannot host an event as one is already in progress."),
    IN_EVENT(ChatColor.RED + "You're already in the event."),
    DOES_NOT_EXIST(ChatColor.RED + "There is no event named " + ChatColor.YELLOW + "{event}" + ChatColor.RED + "."),
    NOT_ENABLED(ChatColor.RED + "That event is not enabled!"),
    NOT_IN_EVENT(ChatColor.RED + "You aren't in an event!"),
    CRAFT_IN_EVENT(ChatColor.RED + "You're unable to craft in this state."),
    NO_PERMISSION(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command.");

    final String description;

    EventMessage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}