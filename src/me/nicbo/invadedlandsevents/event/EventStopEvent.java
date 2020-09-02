package me.nicbo.invadedlandsevents.event;

import me.nicbo.invadedlandsevents.events.type.InvadedEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an event is stopped
 * Created so I could avoid cyclic dependency
 * between EventManager and InvadedEvent
 *
 * @author Nicbo
 */

public class EventStopEvent extends Event {
    private static final HandlerList HANDLERS;

    private final InvadedEvent event;

    static {
        HANDLERS = new HandlerList();
    }

    public EventStopEvent(InvadedEvent event) {
        this.event = event;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the event that was stopped
     * @return the event
     */
    public InvadedEvent getEvent() {
        return event;
    }
}
