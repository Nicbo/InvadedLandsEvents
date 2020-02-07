package me.nicbo.InvadedLandsEvents.events;

public enum EventStatus {
    NONE("d1"),
    STARTED("d3"),
    ENDING("d4"),
    INEVENT("d2");

    final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /*
    TODO:
        - Descriptions for attempt to join event or in event etc.
     */
}
