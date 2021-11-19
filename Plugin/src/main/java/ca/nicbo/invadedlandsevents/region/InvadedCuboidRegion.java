package ca.nicbo.invadedlandsevents.region;

import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CuboidRegion}.
 *
 * @author Nicbo
 */
public class InvadedCuboidRegion implements CuboidRegion {
    private final World world;

    private final Location locationOne;
    private final Location locationTwo;

    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public InvadedCuboidRegion(Location locationOne, Location locationTwo) {
        Validate.checkArgumentNotNull(locationOne, "locationOne");
        Validate.checkArgumentNotNull(locationTwo, "locationTwo");
        Validate.checkArgumentNotNull(locationOne.getWorld(), "locationOne's world");
        Validate.checkArgumentNotNull(locationTwo.getWorld(), "locationTwo's world");
        Validate.checkArgument(locationOne.getWorld().equals(locationTwo.getWorld()), "locationOne and locationTwo must have the same worlds");

        this.world = locationOne.getWorld();

        this.locationOne = locationOne;
        this.locationTwo = locationTwo;

        this.minX = Math.min(locationOne.getBlockX(), locationTwo.getBlockX());
        this.maxX = Math.max(locationOne.getBlockX(), locationTwo.getBlockX());
        this.minY = Math.min(locationOne.getBlockY(), locationTwo.getBlockY());
        this.maxY = Math.max(locationOne.getBlockY(), locationTwo.getBlockY());
        this.minZ = Math.min(locationOne.getBlockZ(), locationTwo.getBlockZ());
        this.maxZ = Math.max(locationOne.getBlockZ(), locationTwo.getBlockZ());
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getLocationOne() {
        return locationOne.clone();
    }

    @Override
    public Location getLocationTwo() {
        return locationTwo.clone();
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public int getMinZ() {
        return minZ;
    }

    @Override
    public int getMaxZ() {
        return maxZ;
    }

    @Override
    public int getLengthX() {
        return maxX - minX + 1;
    }

    @Override
    public int getLengthY() {
        return maxY - minY + 1;
    }

    @Override
    public int getLengthZ() {
        return maxZ - minZ + 1;
    }

    @Override
    public int getSize() {
        return getLengthX() * getLengthY() * getLengthZ();
    }

    @Override
    public boolean contains(Entity entity) {
        Validate.checkArgumentNotNull(entity, "entity");
        return contains(entity.getLocation());
    }

    @Override
    public boolean contains(Block block) {
        Validate.checkArgumentNotNull(block, "block");
        return contains(block.getLocation());
    }

    @Override
    public boolean contains(Location location) {
        Validate.checkArgumentNotNull(location, "location");
        return this.world.equals(location.getWorld()) &&
                location.getBlockX() >= this.minX &&
                location.getBlockX() <= this.maxX &&
                location.getBlockY() >= this.minY &&
                location.getBlockY() <= this.maxY &&
                location.getBlockZ() >= this.minZ &&
                location.getBlockZ() <= this.maxZ;
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
    }

    @Override
    public String toString() {
        return "(" + StringUtils.locationToString(locationOne, false) + ", " + StringUtils.locationToString(locationTwo, false) + ")";
    }
}
