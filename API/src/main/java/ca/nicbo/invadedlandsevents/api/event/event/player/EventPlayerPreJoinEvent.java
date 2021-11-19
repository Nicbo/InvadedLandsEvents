package ca.nicbo.invadedlandsevents.api.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player joins an event.
 *
 * @author Nicbo
 */
public class EventPlayerPreJoinEvent extends EventPlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPlayerPreJoinEvent.
     *
     * @param event the event that the player is joining
     * @param player the player who is joining the event
     * @throws NullPointerException if the event or player is null
     */
    public EventPlayerPreJoinEvent(@NotNull Event event, @NotNull Player player) {
        super(event, player);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
