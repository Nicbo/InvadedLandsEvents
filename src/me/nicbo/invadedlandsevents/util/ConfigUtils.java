package me.nicbo.invadedlandsevents.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

/**
 * Utility class for config
 *
 * @author Nicbo
 */

public final class ConfigUtils {
    private ConfigUtils() {
    }

    /**
     * Saves event location in config
     *
     * @param section  the ConfigurationSection to save to
     * @param location the location
     */
    public static void serializeEventLocation(ConfigurationSection section, Location location) {
        section.set("value", location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch());
    }

    /**
     * Deserializes event location
     *
     * @param section    the ConfigurationSection where the location is saved
     * @return the deserialized location (null world)
     */
    public static Location deserializeEventLocation(ConfigurationSection section) {
        String[] splitLoc = section.getString("value").split(";");
        double x = Double.parseDouble(splitLoc[0]);
        double y = Double.parseDouble(splitLoc[1]);
        double z = Double.parseDouble(splitLoc[2]);
        float yaw = Float.parseFloat(splitLoc[3]);
        float pitch = Float.parseFloat(splitLoc[4]);
        return new Location(null, x, y, z, yaw, pitch);
    }

    /**
     * Saves full location in config
     * Used when world name is needed to be saved
     *
     * @param section  the ConfigurationSection to save to
     * @param location the location
     */
    public static void serializeFullLocation(ConfigurationSection section, Location location) {
        section.set("value", location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch());
    }

    /**
     * Deserializes full location
     *
     * @param section the ConfigurationSection where the location is saved
     * @return the deserialized location
     */
    public static Location deserializeFullLocation(ConfigurationSection section) {
        String[] splitLoc = section.getString("value").split(";");
        World world = Bukkit.getWorld(splitLoc[0]);
        double x = Double.parseDouble(splitLoc[1]);
        double y = Double.parseDouble(splitLoc[2]);
        double z = Double.parseDouble(splitLoc[3]);
        float yaw = Float.parseFloat(splitLoc[4]);
        float pitch = Float.parseFloat(splitLoc[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Saves block vector to config
     *
     * @param section     the ConfigurationSection to save to
     * @param blockVector the block vector
     */
    public static void serializeBlockVector(ConfigurationSection section, BlockVector blockVector) {
        section.set("value", blockVector.getBlockX() + ";" + blockVector.getBlockY() + ";" + blockVector.getBlockZ());
    }

    /**
     * Deserialize block vector
     *
     * @param section the ConfigurationSection where the block vector is saved
     * @return deserialized block vector
     */
    public static BlockVector deserializeBlockVector(ConfigurationSection section) {
        String[] splitLocBlock = section.getString("value").split(";");
        double x = Double.parseDouble(splitLocBlock[0]);
        double y = Double.parseDouble(splitLocBlock[1]);
        double z = Double.parseDouble(splitLocBlock[2]);
        return new BlockVector(x, y, z);
    }
}
