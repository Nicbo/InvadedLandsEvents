package ca.nicbo.invadedlandsevents.gui.config;

import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.gui.InvadedButton;
import ca.nicbo.invadedlandsevents.gui.InvadedGui;
import ca.nicbo.invadedlandsevents.kit.InvadedKit;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Displays an {@link InvadedKit} to the player.
 *
 * @author Nicbo
 */
public final class KitViewGui extends InvadedGui {
    private static final ItemStack EMPTY = new ItemStackBuilder(CompatibleMaterial.RED_GLASS_PANE)
            .setName(ChatColor.GRAY.toString())
            .build();
    private static final ItemStack BLANK = new ItemStackBuilder(CompatibleMaterial.GREY_GLASS_PANE)
            .setName(ChatColor.GRAY.toString())
            .build();

    public KitViewGui(Player player, String title) {
        super(player, title, 54);
    }

    private static ItemStack emptyIfNull(ItemStack item) {
        return item == null ? EMPTY : item;
    }

    public static KitViewGui create(Player player, String title, Kit kit) {
        Validate.checkArgumentNotNull(kit, "kit");

        KitViewGui gui = new KitViewGui(player, title);
        List<ItemStack> items = kit.getItems();
        List<ItemStack> armour = CollectionUtils.reversedCopy(kit.getArmour()); // armour is backwards in this list

        // Top 3 rows
        for (int i = 0; i < 27; i++) {
            ItemStack kitItem = items.get(i + 9);
            gui.setButton(i, new InvadedButton(emptyIfNull(kitItem)));
        }

        // Hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack kitItem = items.get(i);
            gui.setButton(i + 27, new InvadedButton(emptyIfNull(kitItem)));
        }

        // Blanks
        for (int i = 36; i < 49; i++) {
            gui.setButton(i, new InvadedButton(BLANK));
        }

        // Armour
        for (int i = 0; i < 4; i++) {
            ItemStack kitItem = armour.get(i);
            gui.setButton(i + 49, new InvadedButton(emptyIfNull(kitItem)));
        }

        // Offhand slot (if it exists)
        ItemStack offhand = kit.getOffhand();
        gui.setButton(53, new InvadedButton(emptyIfNull(offhand)));

        return gui;
    }
}
