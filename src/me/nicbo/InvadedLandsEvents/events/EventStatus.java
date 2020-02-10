package me.nicbo.InvadedLandsEvents.events;

public enum EventStatus {
    NONE("There is no event active right now"),
    STARTED("You cannot join the event as it has already started!"),
    ENDING("The event is ending"),
    IN_EVENT("You are already in the event"),
    JOIN("You joined the event"),
    DOES_NOT_EXIST("{event} does not exist!");

    final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /*
    TODO:
        - Make descriptions editable based on config
        - Colours based on Invaded's
        - Some of these invaded does not have, change later
     */
}
