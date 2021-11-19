package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.api.configuration.WandLocationHolder;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;

/**
 * Implementation of {@link WandLocationHolder}.
 *
 * @author Nicbo
 */
public class InvadedWandLocationHolder implements WandLocationHolder {
    private Location locationOne;
    private Location locationTwo;

    @Override
    public Location getLocationOne() {
        return cloneOrNull(locationOne);
    }

    @Override
    public void setLocationOne(Location locationOne) {
        this.locationOne = locationOne;
    }

    @Override
    public Location getLocationTwo() {
        return cloneOrNull(locationTwo);
    }

    @Override
    public void setLocationTwo(Location locationTwo) {
        this.locationTwo = locationTwo;
    }

    @Override
    public String toString() {
        return "(" + StringUtils.locationToString(locationOne, false) + ", " + StringUtils.locationToString(locationTwo, false) + ")";
    }

    private static Location cloneOrNull(Location location) {
        return location == null ? null : location.clone();
    }
}
