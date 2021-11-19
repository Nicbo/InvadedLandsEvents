package ca.nicbo.invadedlandsevents.api;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Allows static access of the InvadedLandsEvents API.
 * <p>
 * Using the bukkit services manager is preferred.
 *
 * @author Nicbo
 */
public final class InvadedLandsEventsProvider {
    private static InvadedLandsEvents instance; // this is set using reflection

    private InvadedLandsEventsProvider() {
    }

    /**
     * Returns the instance of InvadedLandsEvents.
     *
     * @return the instance
     * @throws IllegalStateException if the instance has not been set
     */
    @NotNull
    public static InvadedLandsEvents getInstance() {
        Validate.checkState(instance != null, "instance has not been set");
        return instance;
    }
}
