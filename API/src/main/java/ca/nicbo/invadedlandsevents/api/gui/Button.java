package ca.nicbo.invadedlandsevents.api.gui;

import ca.nicbo.invadedlandsevents.api.util.Callback;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a button on the {@link Gui}.
 *
 * @author Nicbo
 */
public interface Button {
    /**
     * Returns a copy of the item stack.
     *
     * @return the item stack
     */
    @NotNull
    ItemStack getItemStack();

    /**
     * Sets the item stack.
     *
     * @param itemStack the item stack
     * @throws NullPointerException if the itemStack is null
     */
    void setItemStack(@NotNull ItemStack itemStack);

    /**
     * Returns the callback that is called when the button is clicked.
     *
     * @return the callback
     */
    @Nullable
    Callback getCallback();

    /**
     * Sets the callback that is called when the button is clicked.
     *
     * @param callback the callback
     */
    void setCallback(@Nullable Callback callback);
}
