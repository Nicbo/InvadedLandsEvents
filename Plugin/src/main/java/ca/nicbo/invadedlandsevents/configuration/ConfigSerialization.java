package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.kit.InvadedKit;
import ca.nicbo.invadedlandsevents.region.InvadedCuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Used for serializing and deserializing objects in config.yml.
 *
 * @author Nicbo
 */
public final class ConfigSerialization {
    private ConfigSerialization() {
    }

    public static String serializeLocation(Location location) {
        Validate.checkArgumentNotNull(location, "location");
        Validate.checkArgumentNotNull(location.getWorld(), "location's world");

        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getYaw() + ";" +
                location.getPitch();
    }

    public static Location deserializeLocation(String serializedLocation) {
        Validate.checkArgumentNotNull(serializedLocation, "serializedLocation");
        String[] splitLoc = serializedLocation.split(";");
        Validate.checkArgument(splitLoc.length >= 6, "serializedLocation does not contain enough data: %s", serializedLocation);

        World world = Bukkit.getWorld(splitLoc[0]);

        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }

        double x = Double.parseDouble(splitLoc[1]);
        double y = Double.parseDouble(splitLoc[2]);
        double z = Double.parseDouble(splitLoc[3]);
        float yaw = Float.parseFloat(splitLoc[4]);
        float pitch = Float.parseFloat(splitLoc[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String serializeRegion(CuboidRegion region) {
        Validate.checkArgumentNotNull(region, "region");
        Validate.checkArgumentNotNull(region.getLocationOne(), "region's first location");
        Validate.checkArgumentNotNull(region.getLocationTwo(), "region's second location");
        return serializeLocation(region.getLocationOne()) + "|" + serializeLocation(region.getLocationTwo());
    }

    public static CuboidRegion deserializeRegion(String serializedRegion) {
        Validate.checkArgumentNotNull(serializedRegion, "serializedRegion");
        String[] serializedLocations = serializedRegion.split("\\|");
        Validate.checkArgument(serializedLocations.length >= 2, "serializedRegion does not contain enough data: %s", serializedRegion);
        return new InvadedCuboidRegion(deserializeLocation(serializedLocations[0]), deserializeLocation(serializedLocations[1]));
    }

    public static String serializeKit(Kit kit) {
        Validate.checkArgumentNotNull(kit, "kit");
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", kit.getItems());
        config.set("armour", kit.getArmour());
        config.set("offhand", kit.getOffhand());
        return config.saveToString();
    }

    @SuppressWarnings("unchecked")
    public static Kit deserializeKit(String serializedKit) {
        Validate.checkArgumentNotNull(serializedKit, "serializedKit");

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(serializedKit);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException("serializedKit is invalid", e);
        }

        List<ItemStack> items = (List<ItemStack>) configuration.getList("items");
        List<ItemStack> armour = (List<ItemStack>) configuration.getList("armour");
        ItemStack offhand = configuration.getItemStack("offhand");
        return new InvadedKit(items, armour, offhand);
    }
}
