package ca.nicbo.invadedlandsevents.api.event.event;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before an event starts its countdown.
 *
 * @author Nicbo
 */
public class EventPreCountdownEvent extends EventEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPreCountdownEvent.
     *
     * @param event the event that is going to start its countdown
     * @throws NullPointerException if the event is null
     */
    public EventPreCountdownEvent(@NotNull Event event) {
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
