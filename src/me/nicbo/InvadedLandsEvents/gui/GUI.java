package me.nicbo.InvadedLandsEvents.gui;

import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI creator
 *
 * @author thehydrogen
 * @since 2020-05-06
 */

public class GUI {
    public static final Map<UUID, GUI> openInventories;

    protected final Player player;
    private final Inventory inv;
    private final Map<Integer, Action> actions;

    static {
        openInventories = new HashMap<>();
    }

    public GUI(final String title, final int size, final Player player) {
        this.player = player;
        this.inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        this.actions = new HashMap<>();
    }

    public void setItem(final int slot, final Material material, final String name, final List<String> lore, final Action action) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        this.inv.setItem(slot, item);
        this.actions.put(slot, action);
    }

    public void setItem(final int slot, final Material material, int dataID, final String name, final List<String> lore, final Action action) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        item.setDurability((short) dataID);
        this.inv.setItem(slot, item);
        this.actions.put(slot, action);
    }

    public void setBlankItem(final int slot) {
        setBlankItem(slot, 15);
    }

    public void setBlankItem(final int slot, final int dataID) {
        final ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short) dataID);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        this.inv.setItem(slot, item);
    }

    public boolean isSlotEmpty(final int slot) {
        return inv.getItem(slot) == null;
    }

    public void open() {
        player.openInventory(this.inv);
        GUI.openInventories.put(player.getUniqueId(), this);
    }

    public void close() {
        player.closeInventory();
        GUI.openInventories.remove(player.getUniqueId());
    }

    public Map<Integer, Action> getActions() {
        return this.actions;
    }

    public static Map<UUID, GUI> getOpenInventories() {
        return GUI.openInventories;
    }

    public interface Action {
        void click();
    }
}
