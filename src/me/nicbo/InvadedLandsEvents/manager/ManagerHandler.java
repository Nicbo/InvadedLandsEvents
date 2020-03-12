package me.nicbo.InvadedLandsEvents.manager;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import me.nicbo.InvadedLandsEvents.manager.managers.EventPartyManager;
import me.nicbo.InvadedLandsEvents.manager.managers.EventPartyRequestManager;

/**
 * Handles managers
 *
 * @author StarZorroww
 * @since 2020-03-12
 */

public class ManagerHandler
{
    private EventsMain plugin;
    private EventPartyManager partyManager;
    private EventManager eventManager;
    private EventPartyRequestManager eventPartyRequestManager;

    public ManagerHandler(EventsMain plugin) {
        this.plugin = plugin;
        this.loadManagers();
    }

    private void loadManagers() {
        this.eventManager = new EventManager(this);
        this.partyManager = new EventPartyManager(this);
        this.eventPartyRequestManager = new EventPartyRequestManager(this);
    }

    public EventsMain getPlugin() {
        return this.plugin;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public EventPartyManager getEventPartyManager() { return this.partyManager; }

    public EventPartyRequestManager getEventPartyRequestManager() { return this.eventPartyRequestManager; }


    public void restartEventManager() {
        this.eventManager = new EventManager(this);
    }
}