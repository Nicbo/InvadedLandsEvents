package me.nicbo.invadedlandsevents.listeners;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.gui.GUI;
import me.nicbo.invadedlandsevents.gui.button.Button;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener that is always active
 *
 * @author Nicbo
 * @author thehydrogen
 */

public final class GeneralListener implements Listener {
    private final InvadedLandsEvents plugin;
    private final UUID nicboUUID;
    private final UUID starUUID;

    /**
     * Constructor for GeneralListener
     *
     * @param plugin the main class instance
     */
    public GeneralListener(InvadedLandsEvents plugin) {
        this.plugin = plugin;
        this.nicboUUID = UUID.fromString("05b3c28a-a532-41bd-8b5e-f3ca452d3876");
        this.starUUID = UUID.fromString("742103ed-2145-4ada-b8ea-785e036a8898");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getUniqueId().equals(nicboUUID) || player.getUniqueId().equals(starUUID)) {
            player.sendMessage(ChatColor.AQUA + "This server is running InvadedLandsEvents v" + plugin.getDescription().getVersion() + ".");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GUI gui = GUI.getOpenInventories().get(player);
        if (gui != null) {
            GUI.getOpenInventories().remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void invClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            GUI gui = GUI.getOpenInventories().get(player);
            if (gui != null) {
                GUI.getOpenInventories().remove(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            GUI gui = GUI.getOpenInventories().get((Player) event.getWhoClicked());
            if (gui != null && event.getClickedInventory() != null && event.getClickedInventory().equals(gui.getInventory())) {
                event.setCancelled(true);
                Button button = gui.getButton(event.getSlot());
                if (button != null) {
                    Runnable action = button.getAction();
                    if (action != null) {
                        action.run();
                    }
                }
            }
        }
    }
}
