package me.nicbo.InvadedLandsEvents.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigUtils {
    private static World eventWorld;
    private static Location spawnLoc;
    private ConfigUtils() {}

    public static void locToConfig(Location loc, ConfigurationSection section) {
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", loc.getYaw());
        section.set("pitch", loc.getPitch());
    }

    public static Location locFromConfig(ConfigurationSection section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(eventWorld, x, y, z, yaw, pitch);
    }

    public static void blockVectorToConfig(BlockVector blockVector, ConfigurationSection section) {
        section.set("x", blockVector.getX());
        section.set("y", blockVector.getY());
        section.set("z", blockVector.getZ());
    }

    public static BlockVector blockVectorFromConfig(ConfigurationSection section) {
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        return new BlockVector(x, y, z);
    }

    public static World getEventWorld() {
        return eventWorld;
    }

    public static void setEventWorld(String world) {
        eventWorld = Bukkit.getWorld(world);
    }

    public static Location getSpawnLoc() {
        return spawnLoc;
    }

    public static void setSpawnLoc(ConfigurationSection loc) {
        spawnLoc = (Location) loc;
    }

    public static String[] configSectionToMsgs(ConfigurationSection section) { // bad code but it works ¯\_(ツ)_/¯
        List<String> msgs = new ArrayList<>();
        Map<String, Object> keyValues = section.getValues(false);
        for (String key : keyValues.keySet()) {
            try {
                ConfigurationSection subSection = section.getConfigurationSection(key);
                Map<String, Object> keyValuesSub = subSection.getValues(false);
                StringBuilder coords = new StringBuilder();
                for (String subKey : keyValuesSub.keySet()) {
                    coords.append("\n    ").append(ChatColor.GOLD + subKey).append(": ").append(ChatColor.YELLOW + keyValuesSub.get(subKey).toString());
                }
                msgs.add(ChatColor.GOLD + key + ": " + coords.toString());
            } catch (NullPointerException npe) {
                msgs.add(ChatColor.GOLD + key + ": " + ChatColor.YELLOW + keyValues.get(key));
            }
        }
        return msgs.toArray(new String[0]);
    }
}
