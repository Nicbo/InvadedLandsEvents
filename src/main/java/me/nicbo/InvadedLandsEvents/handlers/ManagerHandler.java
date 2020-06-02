package me.nicbo.InvadedLandsEvents.handlers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyRequestManager;

/**
 * Handles managers
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class ManagerHandler {
    private EventPartyManager partyManager;
    private EventManager eventManager;
    private EventPartyRequestManager eventPartyRequestManager;

    public ManagerHandler(EventsMain plugin) {
        this.eventManager = new EventManager(plugin);
        this.eventPartyRequestManager = new EventPartyRequestManager();
        this.partyManager = new EventPartyManager(plugin, eventPartyRequestManager);
    }

    public EventManager getEventManager() { return this.eventManager; }

    public EventPartyManager getEventPartyManager() {
        return this.partyManager;
    }

    public EventPartyRequestManager getEventPartyRequestManager() { return this.eventPartyRequestManager; }
}