package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
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
        if (EventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(event.getPlayer())) {
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta()) return;
            System.out.println("1st check"); // Remove when plugin is stable

            Player player = event.getPlayer();
            if (item.getItemMeta().getDisplayName().contains("Leave Event") && EventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player)) {
                System.out.println("hhhh"); // Remove when plugin is stable
                eventManager.leaveEvent(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void itemCraft(CraftItemEvent event) {
        if (EventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer((Player)event.getWhoClicked())) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(EventMessage.CRAFT_IN_EVENT.getDescription());
        }
    }
}
