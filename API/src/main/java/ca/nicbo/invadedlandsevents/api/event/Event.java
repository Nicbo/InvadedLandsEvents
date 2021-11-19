package ca.nicbo.invadedlandsevents.api.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an event.
 *
 * @author Nicbo
 */
public interface Event {
    /**
     * Returns the number of seconds left in this event's countdown.
     *
     * @return the number of seconds
     */
    int getCountdown();

    /**
     * Returns the name of the sender who hosted this event.
     *
     * @return the host name
     */
    @NotNull
    String getHostName();

    /**
     * Returns the type of this event.
     *
     * @return the event type
     */
    @NotNull
    EventType getEventType();

    /**
     * Returns an unmodifiable view of the players.
     *
     * @return an unmodifiable view of the players
     */
    @NotNull
    List<Player> getPlayers();

    /**
     * Returns an unmodifiable view of the spectators.
     *
     * @return an unmodifiable view of the spectators
     */
    @NotNull
    List<Player> getSpectators();

    /**
     * Adds the player to this event's players.
     *
     * @param player the player
     * @throws IllegalArgumentException if player is already participating in the event
     * @throws IllegalStateException if the event is not in the {@link EventState#COUNTDOWN} state
     * @throws NullPointerException if the player is null
     */
    void join(@NotNull Player player);

    /**
     * Removes the player from this event's players or spectators.
     *
     * @param player the player
     * @throws IllegalArgumentException if player is not participating in the event
     * @throws IllegalStateException if the event is not in the {@link EventState#COUNTDOWN}, {@link
     *         EventState#STARTED} or {@link EventState#ENDED} state
     * @throws NullPointerException if the player is null
     */
    void leave(@NotNull Player player);

    /**
     * Adds the player to this event's spectators.
     *
     * @param player the player
     * @throws IllegalArgumentException if player is already participating in the event
     * @throws IllegalStateException if the event is not in the {@link EventState#COUNTDOWN} or {@link
     *         EventState#STARTED} state
     * @throws NullPointerException if the player is null
     */
    void spectate(@NotNull Player player);

    /**
     * Force ends this event.
     *
     * @param silent true if a message should not be sent out
     * @throws IllegalStateException if the event is not in the {@link EventState#COUNTDOWN} or {@link
     *         EventState#STARTED} state
     */
    void forceEnd(boolean silent);

    /**
     * Sends a message to every participant of this event.
     * <p>
     * The message is not altered before being broadcasted, any formatting must be done beforehand.
     *
     * @param message the message to send
     * @throws NullPointerException if the message is null
     */
    void broadcastMessage(@NotNull String message);

    /**
     * Returns the state of this event
     *
     * @return the state
     */
    @NotNull
    EventState getState();
}
