package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.gui.InvadedButton;
import org.bukkit.inventory.ItemStack;

/**
 * A button that hosts an event when clicked.
 *
 * @author Nicbo
 */
public class HostButton extends InvadedButton {
    private final HostButtonType type;

    public HostButton(ItemStack itemStack, Callback callback, HostButtonType type) {
        super(itemStack, callback);
        Validate.checkArgumentNotNull(type, "type");
        this.type = type;
    }

    public HostButtonType getType() {
        return type;
    }
}
