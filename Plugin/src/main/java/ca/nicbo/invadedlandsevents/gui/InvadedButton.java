package ca.nicbo.invadedlandsevents.gui;

import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.inventory.ItemStack;

/**
 * Implementation of {@link Button}.
 *
 * @author Nicbo
 */
public class InvadedButton implements Button {
    private ItemStack itemStack;
    private Callback callback;

    public InvadedButton(ItemStack itemStack) {
        this(itemStack, null);
    }

    public InvadedButton(ItemStack itemStack, Callback callback) {
        Validate.checkArgumentNotNull(itemStack, "itemStack");
        this.itemStack = itemStack;
        this.callback = callback;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        Validate.checkArgumentNotNull(itemStack, "itemStack");
        this.itemStack = itemStack;
    }

    @Override
    public Callback getCallback() {
        return callback;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
