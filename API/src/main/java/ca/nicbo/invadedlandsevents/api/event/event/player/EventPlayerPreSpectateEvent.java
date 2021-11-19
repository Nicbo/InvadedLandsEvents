package ca.nicbo.invadedlandsevents.api.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player spectates an event.
 *
 * @author Nicbo
 */
public class EventPlayerPreSpectateEvent extends EventPlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPlayerPreSpectateEvent.
     *
     * @param event the event that the player is going to spectate
     * @param player the player who is going to spectate the event
     * @throws NullPointerException if the event or player is null
     */
    public EventPlayerPreSpectateEvent(@NotNull Event event, @NotNull Player player) {
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
