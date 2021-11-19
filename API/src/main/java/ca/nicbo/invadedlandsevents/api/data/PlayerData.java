package ca.nicbo.invadedlandsevents.api.data;

import ca.nicbo.invadedlandsevents.api.event.EventType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all the player's data.
 *
 * @author Nicbo
 */
public interface PlayerData {
    /**
     * Returns the player's UUID.
     *
     * @return the player's UUID
     */
    @NotNull
    UUID getPlayerUUID();

    /**
     * Returns the event data associated with the provided event type.
     *
     * @param eventType the event type
     * @return the event data
     * @throws NullPointerException if the eventType is null
     */
    @NotNull
    PlayerEventData getEventData(@NotNull EventType eventType);

    /**
     * Returns an unmodifiable view of the event data map. This map is guaranteed to have every event type.
     *
     * @return an unmodifiable view of the event data map
     */
    @NotNull
    Map<EventType, PlayerEventData> getEventDataMap();

    /**
     * Attempts to save the data to the file.
     *
     * @throws IOException if the file cannot be written to
     */
    void saveToFile() throws IOException;
}
