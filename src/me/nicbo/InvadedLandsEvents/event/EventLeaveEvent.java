package me.nicbo.InvadedLandsEvents.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Gets called when player leaves the event
 *
 * @author Nicbo
 * @since 2020-05-17
 */

public final class EventLeaveEvent extends Event {
    private static final HandlerList HANDLERS;

    private final Player player;

    static {
        HANDLERS = new HandlerList();
    }

    public EventLeaveEvent(Player player) {
        this.player = player;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }
}
