package ca.nicbo.invadedlandsevents.api.event.event;

import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Event} related event.
 *
 * @author Nicbo
 */
public abstract class EventEvent extends org.bukkit.event.Event {
    private final Event event;

    /**
     * Creates an EventEvent.
     *
     * @param event the event involved with the event
     * @throws NullPointerException if the event is null
     */
    protected EventEvent(@NotNull Event event) {
        Validate.checkArgumentNotNull(event, "event");
        this.event = event;
    }

    /**
     * Returns the event involved with the event.
     *
     * @return the event
     */
    @NotNull
    public Event getEvent() {
        return event;
    }
}
