package ca.nicbo.invadedlandsevents.api.event.event;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after an event starts.
 *
 * @author Nicbo
 */
public class EventPostStartEvent extends EventEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPostStartEvent.
     *
     * @param event the event that started
     * @throws NullPointerException if the event is null
     */
    public EventPostStartEvent(@NotNull Event event) {
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
