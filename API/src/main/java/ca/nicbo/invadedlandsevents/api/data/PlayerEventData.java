package ca.nicbo.invadedlandsevents.api.data;

import ca.nicbo.invadedlandsevents.api.event.EventType;
import org.jetbrains.annotations.NotNull;

/**
 * Holds all the player's data for an event type.
 *
 * @author Nicbo
 */
public interface PlayerEventData {
    /**
     * Returns the event type.
     *
     * @return the event type
     */
    @NotNull
    EventType getEventType();

    /**
     * Returns the timestamp of when the player last hosted the event or 0 if they have never hosted this event type.
     *
     * @return the timestamp
     */
    long getHostTimestamp();

    /**
     * Sets the timestamp of when the player last hosted the event.
     *
     * @param hostTimestamp the timestamp
     */
    void setHostTimestamp(long hostTimestamp);

    /**
     * Returns the number of times the player has won the event.
     *
     * @return the number of wins
     */
    int getWins();

    /**
     * Sets the number of times the player has won the event.
     *
     * @param wins the number of wins
     */
    void setWins(int wins);
}
