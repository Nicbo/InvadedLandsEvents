package me.nicbo.InvadedLandsEvents.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
