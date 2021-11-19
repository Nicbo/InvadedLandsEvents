package ca.nicbo.invadedlandsevents.api;

import ca.nicbo.invadedlandsevents.api.configuration.ConfigurationManager;
import ca.nicbo.invadedlandsevents.api.data.PlayerDataManager;
import ca.nicbo.invadedlandsevents.api.event.EventManager;
import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.gui.GuiManager;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Callback;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The InvadedLandsEvents API.
 *
 * @author Nicbo
 */
public interface InvadedLandsEvents {
    /**
     * Returns the configuration manager.
     *
     * @return the configuration manager
     * @throws IllegalStateException if the configuration manager is not ready
     */
    @NotNull
    ConfigurationManager getConfigurationManager();

    /**
     * Returns the player data manager.
     *
     * @return the player data manager
     * @throws IllegalStateException if the player data manager is not ready
     */
    @NotNull
    PlayerDataManager getPlayerDataManager();

    /**
     * Returns the event manager.
     *
     * @return the event manager
     * @throws IllegalStateException if the event manager is not ready
     */
    @NotNull
    EventManager getEventManager();

    /**
     * Returns the GUI manager.
     *
     * @return the GUI manager
     * @throws IllegalStateException if the GUI manager is not ready
     */
    @NotNull
    GuiManager getGuiManager();

    // Factory methods

    /**
     * Returns a new instance of a GUI with the provided components.
     *
     * @param player the player who it belongs to
     * @param title the title
     * @param size the size
     * @return a new GUI
     * @throws IllegalArgumentException if the size is not a multiple of 9 and between 9 and 54 slots
     * @throws NullPointerException if the player or title is null
     */
    @NotNull
    Gui createGui(@NotNull Player player, @NotNull String title, int size);

    /**
     * Returns a new instance of a button with the provided components.
     *
     * @param itemStack the item stack to use
     * @param callback the function to call when the button is clicked
     * @return a new button
     * @throws NullPointerException if the itemStack is null
     */
    @NotNull
    Button createButton(@NotNull ItemStack itemStack, @Nullable Callback callback);

    /**
     * Returns a new instance of a kit with the provided components.
     *
     * @param items the items
     * @param armour the armour
     * @param offhand the offhand
     * @return a new kit
     * @throws NullPointerException if the items or armour is null
     */
    @NotNull
    Kit createKit(@NotNull List<ItemStack> items, @NotNull List<ItemStack> armour, @Nullable ItemStack offhand);

    /**
     * Returns a new instance of a region with the provided components.
     *
     * @param locationOne the first corner
     * @param locationTwo the second corner
     * @return a new region
     * @throws IllegalArgumentException if the location worlds are not the same
     * @throws NullPointerException if either location is null or has a null world
     */
    @NotNull
    CuboidRegion createRegion(@NotNull Location locationOne, @NotNull Location locationTwo);
}
