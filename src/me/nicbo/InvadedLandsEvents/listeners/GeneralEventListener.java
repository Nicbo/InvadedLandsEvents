package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GeneralEventListener implements Listener {
    private EventManager eventManager;

    public GeneralEventListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (EventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player)) {
            eventManager.leaveEvent(player);
        }
    }

    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        if (EventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactNetherStar(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        if (item.getItemMeta().getDisplayName().contains("Leave Event") && EventManager.isEventRunning() &&  eventManager.getCurrentEvent().containsPlayer(event.getPlayer())) {
            eventManager.leaveEvent(event.getPlayer());
        }
    }
}
