package ca.nicbo.invadedlandsevents.api.event.event;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after an event stops.
 *
 * @author Nicbo
 */
public class EventPostStopEvent extends EventEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPostStopEvent.
     *
     * @param event the event that stopped
     * @throws NullPointerException if the event is null
     */
    public EventPostStopEvent(@NotNull Event event) {
        super(event);
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
