package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
    private final Inventory inv;
    public static final Map<UUID, GUI> openInventories;
    private final Map<Integer, Action> actions;

    public GUI(final String title, final int size, final Player owner) {
        this.inv = Bukkit.createInventory(owner, size, ChatColor.translateAlternateColorCodes('&', title));
        this.actions = new HashMap<>();
    }

    public void setItem(final int slot, final Material material, final String name, final ArrayList<String> lore, final Action action) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        this.inv.setItem(slot, item);
        this.actions.put(slot, action);
    }

    public void setItem(final int slot, final Material material, int dataId, final String name, final ArrayList<String> lore, final Action action) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        item.setDurability((short) dataId);
        this.inv.setItem(slot, item);
        this.actions.put(slot, action);
    }

    public void setBlankItem(final int slot) {
        final ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short)15);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        this.inv.setItem(slot, item);
    }

    public void open(final Player toOpen) {
        toOpen.openInventory(this.inv);
        GUI.openInventories.put(toOpen.getUniqueId(), this);
    }

    public void close(final Player toClose) {
        toClose.closeInventory();
        GUI.openInventories.remove(toClose.getUniqueId());
    }

    public Map<Integer, Action> getActions() {
        return this.actions;
    }

    public static Map<UUID, GUI> getOpenInventories() {
        return GUI.openInventories;
    }

    static {
        openInventories = new HashMap<>();
    }

    public interface Action
    {
        void click(final Player p0);
    }

}
