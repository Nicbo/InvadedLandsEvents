package me.nicbo.invadedlandsevents.gui.button;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a button on the GUI
 *
 * @author Nicbo
 * @see me.nicbo.invadedlandsevents.gui.GUI
 */

public class Button {
    private ItemStack item;
    private final Runnable action;
    private final String value;

    public Button(ItemStack item) {
        this(item, null);
    }

    public Button(ItemStack item, Runnable action) {
        this(item, action, "");
    }

    /**
     * Constructor for Button
     *
     * @param item   the item that is shown
     * @param action the action to perform when clicked
     * @param value  a hidden value associated with the button (used in updates)
     */
    public Button(ItemStack item, Runnable action, String value) {
        this.item = item;
        this.action = action;
        this.value = value;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    /**
     * Returns the runnable that runs when the button is pressed
     *
     * @return the action
     */
    public Runnable getAction() {
        return action;
    }

    /**
     * Get the value associated with the button
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
