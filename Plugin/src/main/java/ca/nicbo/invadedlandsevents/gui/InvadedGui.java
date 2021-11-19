package ca.nicbo.invadedlandsevents.gui;

import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.gui.event.GuiOpenEvent;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link Gui}.
 *
 * @author Nicbo
 */
public class InvadedGui implements Gui {
    private final Player player;
    private final String title;
    private final int size;
    private final Inventory inventory;
    private final Map<Integer, Button> buttonMap;

    public InvadedGui(Player player, String title, int size) {
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgumentNotNull(title, "title");
        Validate.checkArgument(size % 9 == 0 && size >= 9 && size <= 54, "size must be a multiple of 9 between 9 and 54 slots, size: %d", size);
        this.player = player;
        this.title = StringUtils.colour(title);
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, this.title);
        this.buttonMap = new HashMap<>();
    }

    @Override
    public void open() {
        player.openInventory(inventory);
        Bukkit.getPluginManager().callEvent(new GuiOpenEvent(this));
    }

    @Override
    public void close() {
        player.closeInventory();
        // No need to call GuiCloseEvent - InventoryCloseEvent handles it in GuiManager
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Button getButton(int slot) {
        return buttonMap.get(slot);
    }

    @Override
    public void setButton(int slot, Button button) {
        Validate.checkArgument(slot >= 0 && slot < size, "slot must be >=0 && <%d, provided: %d", size, slot);
        inventory.setItem(slot, button == null ? null : button.getItemStack());
        buttonMap.put(slot, button);
    }

    @Override
    public boolean isSlotEmpty(int slot) {
        return !buttonMap.containsKey(slot);
    }

    @Override
    public Map<Integer, Button> getButtonMap() {
        return Collections.unmodifiableMap(buttonMap);
    }

    @Override
    public boolean isInventoryEqual(Inventory inventory) {
        return this.inventory.equals(inventory);
    }

    @Override
    public void update() {
        for (Map.Entry<Integer, Button> entry : buttonMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }
    }
}
