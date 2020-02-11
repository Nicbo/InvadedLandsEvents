package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GeneralEventListener implements Listener {
    private EventManager eventManager;

    public GeneralEventListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        if (!EventManager.isEventRunning()) return;
        eventManager.leaveEvent(event.getPlayer());
    }
}
