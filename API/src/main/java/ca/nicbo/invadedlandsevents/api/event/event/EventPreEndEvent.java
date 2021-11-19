package ca.nicbo.invadedlandsevents.api.event.event;

import ca.nicbo.invadedlandsevents.api.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before an event ends.
 *
 * @author Nicbo
 */
public class EventPreEndEvent extends EventEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an EventPreEndEvent.
     *
     * @param event the event that is going to end
     * @throws NullPointerException if the event is null
     */
    public EventPreEndEvent(@NotNull Event event) {
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
