package me.nicbo.invadedlandsevents.gui;

import me.nicbo.invadedlandsevents.gui.button.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nicbo
 * @author thehydrogen
 */

public class GUI {
    private static final Map<Player, GUI> openInventories;

    private final Player player;
    private final Inventory inventory;
    protected final Map<Integer, Button> buttons;

    static {
        openInventories = new HashMap<>();
    }

    public GUI(Player player, String title, int size) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        this.buttons = new HashMap<>();
    }

    /**
     * Get the player that owns the gui
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Open the gui
     */
    public void open() {
        openInventories.put(player, this);
        player.openInventory(inventory);
    }

    /**
     * Close the gui
     */
    public void close() {
        player.closeInventory();

        // Will remove from openInventories in GeneralListener
    }

    /**
     * Add button to the gui
     *
     * @param slot   the slot number
     * @param button the button
     */
    public void setButton(int slot, Button button) {
        inventory.setItem(slot, button.getItem());
        buttons.put(slot, button);
    }

    /**
     * Get the button in the specified slot
     *
     * @param slot the slot number
     * @return the button (null if there is none)
     */
    public Button getButton(int slot) {
        return buttons.get(slot);
    }

    /**
     * Updates a button by setting the inventory item to the
     * buttons item
     *
     * @param slot the slot number
     */
    public void updateButton(int slot) {
        inventory.setItem(slot, buttons.get(slot).getItem());
    }

    /**
     * Add a blank slot to the gui
     *
     * @param slot the slot number
     */
    public void addBlankSlot(int slot) {
        addBlankSlot(slot, (short) 7);
    }

    /**
     * Add a blank slot to the gui
     *
     * @param slot the slot number
     * @param id the glass panes id
     */
    public void addBlankSlot(int slot, short id) {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability(id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        this.inventory.setItem(slot, item);
    }

    /**
     * Check if an inventory slot is empty
     *
     * @param slot the slot number
     * @return true if the slot is empty
     */
    public boolean isSlotEmpty(int slot) {
        return inventory.getItem(slot) == null;
    }

    /**
     * Get the guis internal inventory
     *
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the open inventories
     *
     * @return the open inventories
     */
    public static Map<Player, GUI> getOpenInventories() {
        return openInventories;
    }
}
