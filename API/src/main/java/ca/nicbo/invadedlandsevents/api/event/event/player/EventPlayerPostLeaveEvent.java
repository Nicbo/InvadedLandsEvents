package ca.nicbo.invadedlandsevents.api.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a player leaves an event.
 *
 * @author Nicbo
 */
public class EventPlayerPostLeaveEvent extends EventPlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPlayerPostLeaveEvent.
     *
     * @param event the event that the player left
     * @param player the player who left the event
     * @throws NullPointerException if the event or player is null
     */
    public EventPlayerPostLeaveEvent(@NotNull Event event, @NotNull Player player) {
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
