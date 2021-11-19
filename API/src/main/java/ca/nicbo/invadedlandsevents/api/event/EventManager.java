package ca.nicbo.invadedlandsevents.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the state of the current {@link Event}.
 * <p>
 * This is what most of the user commands interact with. If you want to bypass certain checks, it might be a good idea
 * to interact with the event directly through {@link #getCurrentEvent()}.
 *
 * @author Nicbo
 */
public interface EventManager {
    /**
     * Attempts to host the provided event type with the provided sender.
     *
     * @param eventType the event type
     * @param sender the sender
     * @return true if it was successful
     * @throws NullPointerException if the eventType or sender is null
     */
    boolean hostEvent(@NotNull EventType eventType, @NotNull CommandSender sender);

    /**
     * Attempts to add the provided player to the current event's players.
     *
     * @param player the player
     * @return true if it was successful
     * @throws NullPointerException if the player is null
     */
    boolean joinCurrentEvent(@NotNull Player player);

    /**
     * Attempts to remove the provided player from the current event.
     *
     * @param player the player
     * @return true if it was successful
     * @throws NullPointerException if the player is null
     */
    boolean leaveCurrentEvent(@NotNull Player player);

    /**
     * Attempts to add the provided player to the current event's spectators.
     *
     * @param player the player
     * @return true if it was successful
     * @throws NullPointerException if the player is null
     */
    boolean spectateCurrentEvent(@NotNull Player player);

    /**
     * Attempts to force end the current event.
     *
     * @param sender the sender that initiated the force end
     * @return true if it was successful
     * @throws NullPointerException if the sender is null
     */
    boolean forceEndCurrentEvent(@NotNull CommandSender sender);

    /**
     * Attempts to send the provided sender the current event's information.
     *
     * @param sender the sender
     * @return true if it was successful
     * @throws NullPointerException if the sender is null
     */
    boolean sendCurrentEventInfo(@NotNull CommandSender sender);

    /**
     * Attempts to send the provided sender the provided player's statistics.
     *
     * @param sender the sender
     * @param player the player
     * @return true if it was successful
     * @throws NullPointerException if the sender or player is null
     */
    boolean sendEventStats(@NotNull CommandSender sender, @NotNull Player player);

    /**
     * Returns the current event or null if an event is not active.
     *
     * @return the current event
     */
    @Nullable
    Event getCurrentEvent();

    /**
     * Returns true if the provided event type is enabled.
     *
     * @param eventType the event type
     * @return true if the provided event type is enabled
     * @throws NullPointerException if the eventType is null
     */
    boolean isEventEnabled(@NotNull EventType eventType);
}
