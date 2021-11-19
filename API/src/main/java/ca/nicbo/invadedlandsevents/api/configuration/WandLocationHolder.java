package ca.nicbo.invadedlandsevents.api.configuration;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Holds both locations for a player's wand.
 *
 * @author Nicbo
 */
public interface WandLocationHolder {
    /**
     * Returns a copy of the location for left click.
     *
     * @return the location
     */
    @Nullable
    Location getLocationOne();

    /**
     * Sets the location for left click.
     *
     * @param locationOne the location
     */
    void setLocationOne(@Nullable Location locationOne);

    /**
     * Returns a copy of the location for right click.
     *
     * @return the location
     */
    @Nullable
    Location getLocationTwo();

    /**
     * Sets the location for right click.
     *
     * @param locationTwo the location
     */
    void setLocationTwo(@Nullable Location locationTwo);
}
