package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

import java.util.*;

/**
 * Utility class for config files
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public final class ConfigUtils {
    private ConfigUtils() {}

    public static void serializeLoc(ConfigurationSection section, Location loc, boolean includeWorld) {
        if (includeWorld)
            section.set("world", loc.getWorld().getName());
        section.set("x", loc.getBlockX());
        section.set("y", loc.getBlockY());
        section.set("z", loc.getBlockZ());
        section.set("yaw", loc.getYaw());
        section.set("pitch", loc.getPitch());
    }

    public static Location deserializeLoc(ConfigurationSection section) {
        return deserializeLoc(section, Bukkit.getWorld(section.getString("world")));
    }

    public static Location deserializeLoc(ConfigurationSection section, World world) {
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void serializeBlockVector(ConfigurationSection section, BlockVector blockVector) {
        section.set("x", blockVector.getX());
        section.set("y", blockVector.getY());
        section.set("z", blockVector.getZ());
    }

    public static BlockVector deserializeBlockVector(ConfigurationSection section) {
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        return new BlockVector(x, y, z);
    }

    public static String[] getConfigMessage(ConfigurationSection section) {
        List<String> message = new ArrayList<>();
        Map<String, Object> keyValues = section.getValues(false);

        for (String key : keyValues.keySet()) {
            if (key.equals("events"))
                continue;

            Object value = keyValues.get(key);
            String val = value.toString();

            if (value instanceof ConfigurationSection) {
                ConfigurationSection locSection = (ConfigurationSection) value;

                val = "(" + locSection.getInt("x") + ", "
                        + locSection.getInt("y") + ", "
                        + locSection.getInt("z") + ") ["
                        + String.format("%.2f", locSection.getDouble("yaw")) + ", "
                        + String.format("%.2f", locSection.getDouble("pitch")) + "]";
            }

            message.add(ChatColor.GOLD + key + ": " + ChatColor.YELLOW + val);
        }

        return message.toArray(new String[0]);
    }
}
