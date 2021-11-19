package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import org.bukkit.entity.Player;

/**
 * The {@link HostGui} for /e host sumo.
 *
 * @author Nicbo
 */
public final class SumoHostGui extends HostGui {
    private SumoHostGui(Player player, InvadedLandsEventsPlugin plugin) {
        super(player, "Host Sumo", 9, plugin);
    }

    public static SumoHostGui create(Player player, InvadedLandsEventsPlugin plugin) {
        SumoHostGui gui = new SumoHostGui(player, plugin);
        gui.setButton(0, gui.createHostButton(HostButtonType.SUMO_1V1));
        gui.setButton(1, gui.createHostButton(HostButtonType.SUMO_2V2));
        gui.setButton(2, gui.createHostButton(HostButtonType.SUMO_3V3));
        return gui;
    }
}
