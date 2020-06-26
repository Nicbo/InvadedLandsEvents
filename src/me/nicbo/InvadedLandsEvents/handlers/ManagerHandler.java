package me.nicbo.InvadedLandsEvents.handlers;

import me.nicbo.InvadedLandsEvents.managers.EventManager;

/**
 * Handles managers
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class ManagerHandler {
    private final EventManager eventManager;

    public ManagerHandler() {
        this.eventManager = new EventManager();
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }
}