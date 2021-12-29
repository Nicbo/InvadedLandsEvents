package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Utility class for spigot.
 *
 * @author Nicbo
 * @author StarZorrow
 */
public final class SpigotUtils {
    private SpigotUtils() {
    }

    public static void clear(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        clearPotionEffects(player);
        clearInventory(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setMaximumNoDamageTicks(20);
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setFlying(false);
    }

    public static void clearPotionEffects(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        for (PotionEffect potion : player.getActivePotionEffects()) {
            player.removePotionEffect(potion.getType());
        }
    }

    public static void clearInventory(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        player.setItemOnCursor(null);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public static void fillHotbar(Player player, ItemStack item) {
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgumentNotNull(item, "item");
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, item);
        }
    }

    public static boolean isInventoryEmpty(Player player) {
        Validate.checkArgumentNotNull(player, "player");

        for (ItemStack item : player.getInventory().getContents()) {
            if (!SpigotUtils.isEmpty(item)) {
                return false;
            }
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!SpigotUtils.isEmpty(armor)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isPlayerOnGround(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        return !player.isFlying() && player.getLocation().getY() % (1 / 64d) == 0;
    }

    public static Player getPlayerFromDamager(Entity damager) {
        Validate.checkArgumentNotNull(damager, "damager");
        if (damager instanceof Player) {
            return (Player) damager;
        }

        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }

        return null;
    }

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public static void sendMessage(CommandSender sender, String message) {
        Validate.checkArgumentNotNull(sender, "sender");
        Validate.checkArgumentNotNull(message, "message");
        sender.sendMessage(StringUtils.colour(message));
    }

    public static void sendMessages(CommandSender sender, Iterable<String> messages) {
        Validate.checkArgumentNotNull(messages, "messages");
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }
}
