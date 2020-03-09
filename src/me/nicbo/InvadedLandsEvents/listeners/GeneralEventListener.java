package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GeneralEventListener implements Listener {
    private EventManager eventManager;

    public GeneralEventListener(EventsMain plugin) {
        this.eventManager = plugin.getManagerHandler().getEventManager();
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player)) {
            eventManager.leaveEvent(player);
        }
    }

    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        if (eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactNetherStar(PlayerInteractEvent event) {
        if (eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(event.getPlayer())) {
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
                return;

            Player player = event.getPlayer();
            if (item.getItemMeta().getDisplayName().contains("Leave Event") && eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player)) {
                eventManager.leaveEvent(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void itemCraft(CraftItemEvent event) {
        if (eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer((Player) event.getWhoClicked())) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(EventMessage.CRAFT_IN_EVENT.toString());
        }
    }
}
