package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class EventUtils {
    private EventUtils() {}

    private static boolean isInventoryEmpty(Player player) {
        for(ItemStack item : player.getInventory().getContents()) {
            if(item != null) return false;
        }
        return true;
    }
}
