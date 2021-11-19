package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import org.bukkit.entity.Player;

/**
 * The {@link HostGui} for /e host brackets.
 *
 * @author Nicbo
 */
public final class BracketsHostGui extends HostGui {
    private BracketsHostGui(Player player, InvadedLandsEventsPlugin plugin) {
        super(player, "Host Brackets", 9, plugin);
    }

    public static BracketsHostGui create(Player player, InvadedLandsEventsPlugin plugin) {
        BracketsHostGui gui = new BracketsHostGui(player, plugin);
        gui.setButton(0, gui.createHostButton(HostButtonType.BRACKETS_1V1));
        gui.setButton(1, gui.createHostButton(HostButtonType.BRACKETS_2V2));
        gui.setButton(2, gui.createHostButton(HostButtonType.BRACKETS_3V3));
        return gui;
    }
}
