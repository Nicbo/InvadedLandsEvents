package ca.nicbo.invadedlandsevents.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a GUI for the player.
 *
 * @author Nicbo
 */
public interface Gui {
    /**
     * Opens the GUI.
     */
    void open();

    /**
     * Closes the GUI.
     */
    void close();

    /**
     * Returns the player who owns the GUI.
     *
     * @return the player
     */
    @NotNull
    Player getPlayer();

    /**
     * Returns the title.
     *
     * @return the title
     */
    @NotNull
    String getTitle();

    /**
     * Returns the amount of slots.
     *
     * @return the size
     */
    int getSize();

    /**
     * Returns the button at the provided slot or null if no button is using that slot.
     *
     * @param slot the slot number
     * @return the button
     */
    @Nullable
    Button getButton(int slot);

    /**
     * Sets the button at the provided slot.
     *
     * @param slot the slot number
     * @param button the button
     * @throws IllegalArgumentException if slot < 0 || slot >= size
     */
    void setButton(int slot, @Nullable Button button);

    /**
     * Returns true if the slot is not occupied by a button.
     *
     * @param slot the slot number
     * @return true if the slot is not occupied by a button
     */
    boolean isSlotEmpty(int slot);

    /**
     * Returns an unmodifiable view of the button map.
     *
     * @return an unmodifiable view of the button map
     */
    @NotNull
    Map<Integer, Button> getButtonMap();

    /**
     * Returns true if the provided inventory is equal to the internal inventory.
     *
     * @param inventory the inventory
     * @return true if the inventories are equal
     */
    boolean isInventoryEqual(@Nullable Inventory inventory);

    /**
     * Updates the GUI.
     */
    void update();
}
