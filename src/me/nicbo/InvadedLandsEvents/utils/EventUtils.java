package me.nicbo.InvadedLandsEvents.utils;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.GameMode;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

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
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setMaximumNoDamageTicks(20);
        player.setFoodLevel(20);
        Damageable dam = (Damageable) player;
        player.setHealth(dam.getMaxHealth());
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.getInventory().clear();
        player.getOpenInventory().getTopInventory().clear();
        player.getOpenInventory().getBottomInventory().clear();
        player.getInventory().setArmorContents(null);
    }
}
