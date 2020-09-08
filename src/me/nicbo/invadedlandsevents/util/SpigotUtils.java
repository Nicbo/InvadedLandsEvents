package me.nicbo.invadedlandsevents.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Utility class for spigot
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class SpigotUtils {
    private SpigotUtils() {
    }

    /**
     * Check if a players inventory is empty
     *
     * @param player the player whose inventory should be checked
     * @return true if the inventory is empty
     */
    public static boolean isInventoryEmpty(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears players data (effects, health, inventory, etc.)
     *
     * @param player the player to be cleared
     */
    public static void clear(Player player) {
        for (PotionEffect potion : player.getActivePotionEffects()) {
            player.removePotionEffect(potion.getType());
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setMaximumNoDamageTicks(20);
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setAllowFlight(false);
        player.setFlying(false);
        clearInventory(player);
    }

    /**
     * Clears the players inventory (armour and items)
     *
     * @param player the player whose inventory is being cleared
     */
    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    /**
     * Check if a location is in a region
     *
     * @param loc    the location
     * @param region the region
     * @return true if the location is in the region
     */
    public static boolean isLocInRegion(Location loc, ProtectedRegion region) {
        return region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Fills the players hotbar with an item
     *
     * @param player the player
     * @param item   the item
     */
    public static void fillPlayerHotbar(Player player, ItemStack item) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, item);
        }
    }
}
