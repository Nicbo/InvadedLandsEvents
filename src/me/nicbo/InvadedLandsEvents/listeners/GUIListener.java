package me.nicbo.InvadedLandsEvents.listeners;

import me.nicbo.InvadedLandsEvents.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Listener for GUIs
 *
 * @author thehydrogen
 * @since 2020-05-05
 */

public class GUIListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void invClose(final InventoryCloseEvent e) {
        final Player player = (Player)e.getPlayer();
        final GUI gui = GUI.getOpenInventories().get(player.getUniqueId());
        if (gui != null) {
            GUI.getOpenInventories().remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventoryClick(final InventoryClickEvent e) {
        final Player player = (Player)e.getWhoClicked();
        final GUI gui = GUI.getOpenInventories().get(player.getUniqueId());
        if (gui != null) {
            e.setCancelled(true);
            final GUI.Action action = gui.getActions().get(e.getSlot());
            if (action != null) {
                action.click();
            }
        }
    }

}
