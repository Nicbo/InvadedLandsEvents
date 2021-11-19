package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.gui.InvadedButton;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * The {@link HostGui} for /e host.
 *
 * @author Nicbo
 */
public final class MainHostGui extends HostGui {
    private static final List<CompatibleMaterial> GLASS_PANES = CollectionUtils.unmodifiableList(
            CompatibleMaterial.WHITE_GLASS_PANE, CompatibleMaterial.ORANGE_GLASS_PANE,
            CompatibleMaterial.MAGENTA_GLASS_PANE, CompatibleMaterial.LIGHT_BLUE_GLASS_PANE,
            CompatibleMaterial.YELLOW_GLASS_PANE, CompatibleMaterial.LIME_GLASS_PANE,
            CompatibleMaterial.PINK_GLASS_PANE, CompatibleMaterial.GREY_GLASS_PANE,
            CompatibleMaterial.LIGHT_GREY_GLASS_PANE, CompatibleMaterial.CYAN_GLASS_PANE,
            CompatibleMaterial.PURPLE_GLASS_PANE, CompatibleMaterial.BLUE_GLASS_PANE,
            CompatibleMaterial.BROWN_GLASS_PANE, CompatibleMaterial.GREEN_GLASS_PANE,
            CompatibleMaterial.RED_GLASS_PANE, CompatibleMaterial.BLACK_GLASS_PANE
    );

    private static final List<CompatibleMaterial> WOOLS = CollectionUtils.unmodifiableList(
            CompatibleMaterial.ORANGE_WOOL,
            CompatibleMaterial.YELLOW_WOOL,
            CompatibleMaterial.LIME_WOOL,
            CompatibleMaterial.PINK_WOOL,
            CompatibleMaterial.CYAN_WOOL,
            CompatibleMaterial.PURPLE_WOOL,
            CompatibleMaterial.BLUE_WOOL,
            CompatibleMaterial.RED_WOOL
    );

    // GUI is updated every 1/2 second, wool is updated every second
    private boolean updateWool;
    private int woolIndex;

    private MainHostGui(Player player, InvadedLandsEventsPlugin plugin) {
        super(player, "Host an Event", 36, plugin);
    }

    @Override
    public void update() {
        super.update();
        if (updateWool && ++woolIndex == WOOLS.size()) {
            woolIndex = 0;
        }

        updateWool = !updateWool;
    }

    @Override
    protected void updateHostButtonItemStack(HostButton button) {
        HostButtonType type = button.getType();
        if (updateWool && type == HostButtonType.WOOL_SHUFFLE) {
            createHostButtonItemStack(type);

            ItemMeta meta = createHostButtonItemStack(type).getItemMeta();
            ItemStack item = WOOLS.get(woolIndex).createItemStack();
            item.setItemMeta(meta);

            button.setItemStack(item);
        }

        // Update the new item stack
        super.updateHostButtonItemStack(button);
    }

    public static MainHostGui create(Player player, InvadedLandsEventsPlugin plugin) {
        MainHostGui gui = new MainHostGui(player, plugin);
        gui.setButton(10, gui.createHostButton(HostButtonType.BRACKETS));
        gui.setButton(11, gui.createHostButton(HostButtonType.KING_OF_THE_HILL));
        gui.setButton(12, gui.createHostButton(HostButtonType.LAST_MAN_STANDING));
        gui.setButton(13, gui.createHostButton(HostButtonType.ONE_IN_THE_CHAMBER));
        gui.setButton(14, gui.createHostButton(HostButtonType.RACE_OF_DEATH));
        gui.setButton(15, gui.createHostButton(HostButtonType.REDROVER));
        gui.setButton(16, gui.createHostButton(HostButtonType.SPLEEF));
        gui.setButton(20, gui.createHostButton(HostButtonType.SUMO));
        gui.setButton(21, gui.createHostButton(HostButtonType.TEAM_DEATHMATCH));
        gui.setButton(22, gui.createHostButton(HostButtonType.TNT_TAG));
        gui.setButton(23, gui.createHostButton(HostButtonType.WATERDROP));
        gui.setButton(24, gui.createHostButton(HostButtonType.WOOL_SHUFFLE));

        // Rainbow background
        int startIndex = RandomUtils.randomMinMax(0, GLASS_PANES.size() - 1);
        int index = startIndex;
        int slot = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 9; j++) {
                if (gui.isSlotEmpty(slot)) {
                    Button button = new InvadedButton(new ItemStackBuilder(GLASS_PANES.get(index))
                            .setName("&a")
                            .build());
                    gui.setButton(slot, button);
                }

                if (++index == GLASS_PANES.size()) {
                    index = 0;
                }

                slot++;
            }
            index = startIndex;
        }
        return gui;
    }
}
