package ca.nicbo.invadedlandsevents.gui;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.gui.GuiManager;
import ca.nicbo.invadedlandsevents.api.gui.event.GuiCloseEvent;
import ca.nicbo.invadedlandsevents.api.gui.event.GuiOpenEvent;
import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link GuiManager}.
 *
 * @author Nicbo
 */
public class InvadedGuiManager implements GuiManager, Listener {
    private final Map<Player, Gui> guiMap;

    public InvadedGuiManager() {
        this.guiMap = new HashMap<>();
    }

    public void startUpdating(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Gui gui : guiMap.values()) {
                gui.update();
            }
        }, 0, 10);
    }

    @Override
    public Gui getGui(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        return guiMap.get(player);
    }

    @Override
    public Map<Player, Gui> getGuiMap() {
        return Collections.unmodifiableMap(guiMap);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Gui gui = guiMap.get(event.getPlayer());
        if (gui != null) {
            gui.close();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Gui gui = guiMap.get(player);
            if (gui != null) {
                Bukkit.getPluginManager().callEvent(new GuiCloseEvent(gui));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuiOpen(GuiOpenEvent event) {
        Gui gui = event.getGui();
        guiMap.put(gui.getPlayer(), gui);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuiClose(GuiCloseEvent event) {
        guiMap.remove(event.getGui().getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Gui gui = guiMap.get(player);
            if (gui != null && gui.isInventoryEqual(event.getClickedInventory())) {
                event.setCancelled(true);
                Button button = gui.getButton(event.getSlot());
                if (button != null) {
                    Callback action = button.getCallback();
                    if (action != null) {
                        action.call();
                    }
                }
            }
        }
    }
}
