package me.nicbo.invadedlandsevents.gui.host;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.gui.button.Button;
import me.nicbo.invadedlandsevents.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Host GUI for /e host sumo
 *
 * @author Nicbo
 */

public final class SumoHostGUI extends HostGUI {
    public SumoHostGUI(InvadedLandsEvents plugin, Player player) {
        super(plugin, player, "Host Sumo", 9);
        this.setButton(0, new Button(new ItemBuilder(Material.WOOL)
                .setName("&e1 vs. 1")
                .setLore(createLore())
                .setHideAttributes(true)
                .build(), () -> tryHost("sumo1v1"), "sumo1v1"));
        this.setButton(1, new Button(new ItemBuilder(Material.WOOL)
                .setName("&e2 vs. 2")
                .setLore(createLore())
                .setHideAttributes(true)
                .build(), () -> tryHost("sumo2v2"), "sumo2v2"));
        this.setButton(2, new Button(new ItemBuilder(Material.WOOL)
                .setName("&e3 vs. 3")
                .setLore(createLore())
                .setHideAttributes(true)
                .build(), () -> tryHost("sumo3v3"), "sumo3v3"));
        this.update();
    }
}