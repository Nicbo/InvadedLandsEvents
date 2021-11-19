package ca.nicbo.invadedlandsevents.api.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a region.
 *
 * @author Nicbo
 */
public interface CuboidRegion {
    /**
     * Returns the world of this region.
     *
     * @return the world
     */
    @NotNull
    World getWorld();

    /**
     * Returns the first corner of this region.
     *
     * @return the first corner
     */
    @NotNull
    Location getLocationOne();

    /**
     * Returns the second corner of this region.
     *
     * @return the second corner
     */
    @NotNull
    Location getLocationTwo();

    /**
     * Returns the lowest x value of this region.
     *
     * @return the lowest x value
     */
    int getMinX();

    /**
     * Returns the highest x value of this region.
     *
     * @return the highest x value.
     */
    int getMaxX();

    /**
     * Returns the lowest y value of this region.
     *
     * @return the lowest y value
     */
    int getMinY();

    /**
     * Returns the highest y value of this region.
     *
     * @return the highest y value
     */
    int getMaxY();

    /**
     * Returns the lowest z value of this region.
     *
     * @return the lowest z value
     */
    int getMinZ();

    /**
     * Returns the highest z value of this region.
     *
     * @return the highest z value
     */
    int getMaxZ();

    /**
     * Returns the length of this region's x-axis.
     *
     * @return the length of the x-axis
     */
    int getLengthX();

    /**
     * Returns the length of this region's y-axis.
     *
     * @return the length of the y-axis
     */
    int getLengthY();

    /**
     * Returns the length of this region's z-axis.
     *
     * @return the length of the z-axis
     */
    int getLengthZ();

    /**
     * Returns the total number of blocks in this region.
     *
     * @return the total number of blocks
     */
    int getSize();

    /**
     * Returns true if this region contains the provided entity.
     *
     * @param entity the entity
     * @return true if this region contains the provided entity
     * @throws NullPointerException if the entity is null
     */
    boolean contains(@NotNull Entity entity);

    /**
     * Returns true if this region contains the provided block.
     *
     * @param block the block
     * @return true if this region contains the provided block
     * @throws NullPointerException if the block is null
     */
    boolean contains(@NotNull Block block);

    /**
     * Returns true if this region contains the provided location.
     *
     * @param location the location
     * @return true if this region contains the provided location
     * @throws NullPointerException if the location is null
     */
    boolean contains(@NotNull Location location);

    /**
     * Returns a list of the blocks in this region.
     *
     * @return the blocks
     */
    @NotNull
    List<Block> getBlocks();
}
