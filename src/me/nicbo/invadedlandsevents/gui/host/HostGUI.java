package me.nicbo.invadedlandsevents.gui.host;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.EventManager;
import me.nicbo.invadedlandsevents.gui.GUI;
import me.nicbo.invadedlandsevents.gui.button.Button;
import me.nicbo.invadedlandsevents.permission.EventPermission;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for guis made for hosting events
 *
 * @author Nicbo
 */

public abstract class HostGUI extends GUI {
    private final InvadedLandsEvents plugin;

    HostGUI(InvadedLandsEvents plugin, Player player, String title, int size) {
        super(player, title, size);
        this.plugin = plugin;
    }

    /**
     * Creates lore for item desc
     *
     * @param lines the description of event
     * @return lore the lore
     */
    List<String> createLore(String... lines) {
        List<String> lore = new ArrayList<>(Arrays.asList(lines));
        lore.add("");
        lore.add(""); // Will be changed by updateLoreInfo()
        return lore;
    }

    /**
     * Updates the last line of the lore based on the player
     *
     * @param event the events config name
     * @param lore  the existing lore
     */
    private void updateLoreInfo(String event, List<String> lore) {
        String message = "&aLeft click to host.";
        if (event.equals("sumo")) {
            if (!player.hasPermission(EventPermission.HOST_SUMO1v1) &&
                    !player.hasPermission(EventPermission.HOST_SUMO2v2) &&
                    !player.hasPermission(EventPermission.HOST_SUMO3v3)) {
                message = "&c&lYou don't have permission to host this event.";
            }
        } else {
            long secondsLeft = plugin.getPlayerDataManager().getData(player.getUniqueId()).getSecondsUntilHost(event);
            if (secondsLeft > 0) {
                message = "&cYou must wait &e" + StringUtils.formatSeconds(secondsLeft) + "&c to host this event.";
            } else if (!player.hasPermission(EventPermission.HOST_PREFIX + event)) {
                message = "&c&lYou don't have permission to host this event.";
            } else if (!plugin.getEventManager().isEventEnabled(event)) {
                message = "&cEvent is currently disabled.";
            }
        }

        lore.set(lore.size() - 1, StringUtils.colour(message));
    }

    /**
     * Attempts to host event, if event is hosted the gui is closed
     * If the event could not host it sends the player the error message
     *
     * @param event the event to host
     */
    void tryHost(String event) {
        String hostMessage = plugin.getEventManager().hostEvent(player, event);
        if (hostMessage == null) {
            this.close();
        } else {
            player.sendMessage(hostMessage);
        }
    }

    /**
     * Updates all buttons lore on the host gui
     */
    public void update() {
        for (Integer slot : buttons.keySet()) {
            Button button = buttons.get(slot);
            String val = button.getValue();

            // If buttons value is an event name, it is an event button
            if (EventManager.isEvent(val) || "sumo".equals(val)) {
                ItemStack item = button.getItem();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                updateLoreInfo(val, lore);
                meta.setLore(lore);
                item.setItemMeta(meta);
                button.setItem(item);
                updateButton(slot);
            }
        }
    }
}
