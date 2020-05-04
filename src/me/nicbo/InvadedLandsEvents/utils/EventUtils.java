package me.nicbo.InvadedLandsEvents.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Utility class for events
 *
 * @author StarZorroww
 * @author Nicbo
 * @since 2020-03-12
 */

public final class EventUtils {
    private EventUtils() {}

    public static boolean isInventoryEmpty(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) return false;
        }
        return true;
    }

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
        player.getInventory().clear();
        player.getOpenInventory().getTopInventory().clear();
        player.getOpenInventory().getBottomInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public static void broadcastEventMessage(String msg) {
        EventsMain.getInstance().getManagerHandler().getEventManager().getCurrentEvent().getParticipants().forEach(player -> player.sendMessage(msg));
    }

    public static boolean isLocInRegion(Location loc, ProtectedRegion region) {
        return region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
