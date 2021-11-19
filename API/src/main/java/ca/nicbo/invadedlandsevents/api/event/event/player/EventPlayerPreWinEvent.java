package ca.nicbo.invadedlandsevents.api.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player wins an event.
 *
 * @author Nicbo
 */
public class EventPlayerPreWinEvent extends EventPlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPlayerPreWinEvent.
     *
     * @param event the event that the player is going to win
     * @param player the player who is going to win the event
     * @throws NullPointerException if the event or player is null
     */
    public EventPlayerPreWinEvent(@NotNull Event event, @NotNull Player player) {
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
