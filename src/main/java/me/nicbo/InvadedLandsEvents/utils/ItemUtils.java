package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Item utility class
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-05-07
 */

public final class ItemUtils {
    private ItemUtils() {}

    public static ItemStack addEnchant(ItemStack item, Enchantment enc, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enc, level, true);
        item.setItemMeta(meta);
        return item;
    }
}
