package me.nicbo.InvadedLandsEvents.handlers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventLeaderBoardManager;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyRequestManager;

/**
 * Handles managers
 *
 * @author StarZorroww
 * @since 2020-03-12
 */

public final class ManagerHandler {
    private EventsMain plugin;
    private EventPartyManager partyManager;
    private EventManager eventManager;
    private EventPartyRequestManager eventPartyRequestManager;
    private EventLeaderBoardManager eventLeaderBoardManager;

    public ManagerHandler(EventsMain plugin) {
        this.plugin = plugin;
        this.eventManager = new EventManager(plugin);
        this.partyManager = new EventPartyManager();
        this.eventPartyRequestManager = new EventPartyRequestManager();
        this.eventLeaderBoardManager = new EventLeaderBoardManager();
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public EventPartyManager getEventPartyManager() {
        return this.partyManager;
    }

    public EventPartyRequestManager getEventPartyRequestManager() {
        return this.eventPartyRequestManager;
    }

    public EventLeaderBoardManager getEventLeaderBoardManager() {
        return this.eventLeaderBoardManager;
    }

    public void restartEventManager() {
        this.eventManager = new EventManager(plugin);
    }
}