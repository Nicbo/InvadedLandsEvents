package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.GUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * General event listener
 * This listener is active on all events
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-02-11
 */

public final class GeneralEventListener implements Listener {
    private EventManager eventManager;

    public GeneralEventListener(EventsMain plugin) {
        this.eventManager = plugin.getManagerHandler().getEventManager();
    }

    private boolean runEvent(Player player) {
        return eventManager.isEventRunning() && eventManager.getCurrentEvent().containsPlayer(player);
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent event) {
        if (runEvent(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (runEvent(player)) {
            eventManager.leaveEvent(player);
        }
        final GUI gui = GUI.getOpenInventories().get(player.getUniqueId());
        if (gui != null) {
            GUI.getOpenInventories().remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        if (runEvent(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactNetherStar(PlayerInteractEvent event) {
        if (runEvent(event.getPlayer())) {
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
        if (runEvent((Player) event.getWhoClicked())) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR)
                return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(EventMessage.CRAFT_IN_EVENT);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (runEvent(event.getEntity())) {
            event.setDeathMessage("");
        }
    }

    /*
    TODO:
        - CommandPreProcessEvent make sure command is in config (enabled commands in event) (auto allow event cmds and add perm to bypass)
     */
}
