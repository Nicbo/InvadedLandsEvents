package ca.nicbo.invadedlandsevents.api.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.event.event.EventEvent;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player related {@link EventEvent}.
 *
 * @author Nicbo
 */
public abstract class EventPlayerEvent extends EventEvent {
    private final Player player;

    /**
     * Creates an EventPlayerEvent.
     *
     * @param event the event that the player is interacting with
     * @param player the player involved with the event
     * @throws NullPointerException if the event or player is null
     */
    protected EventPlayerEvent(@NotNull Event event, @NotNull Player player) {
        super(event);
        Validate.checkArgumentNotNull(player, "player");
        this.player = player;
    }

    /**
     * Returns the player involved with the event.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
