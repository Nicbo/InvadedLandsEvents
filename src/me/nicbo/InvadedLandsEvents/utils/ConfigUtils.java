package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigUtils {
    private static World eventWorld;
    private ConfigUtils() {}

    public static Location locFromConfig(ConfigurationSection section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(eventWorld, x, y, z, yaw, pitch);
    }

    public static void locToConfig(Location loc, ConfigurationSection section) {
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", loc.getYaw());
        section.set("pitch", loc.getPitch());
    }

    public static World getEventWorld() {
        return eventWorld;
    }

    public static void setEventWorld(String world) {
        eventWorld = Bukkit.getWorld(world);
    }
}
