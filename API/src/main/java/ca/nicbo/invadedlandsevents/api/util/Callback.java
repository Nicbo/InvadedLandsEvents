package ca.nicbo.invadedlandsevents.api.util;

/**
 * Used for callback functions.
 *
 * @author Nicbo
 */
@FunctionalInterface
public interface Callback {
    /**
     * Invokes the callback.
     */
    void call();
}
