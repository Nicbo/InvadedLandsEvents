package ca.nicbo.invadedlandsevents.data;

import ca.nicbo.invadedlandsevents.api.data.PlayerEventData;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.util.Validate;

/**
 * Implementation of {@link PlayerEventData}.
 *
 * @author Nicbo
 */
public class InvadedPlayerEventData implements PlayerEventData {
    private final EventType eventType;
    private long hostTimestamp;
    private int wins;

    public InvadedPlayerEventData(EventType eventType, long hostTimestamp, int wins) {
        Validate.checkArgumentNotNull(eventType, "eventType");
        this.eventType = eventType;
        this.hostTimestamp = hostTimestamp;
        this.wins = wins;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public long getHostTimestamp() {
        return hostTimestamp;
    }

    @Override
    public void setHostTimestamp(long hostTimestamp) {
        this.hostTimestamp = hostTimestamp;
    }

    @Override
    public int getWins() {
        return wins;
    }

    @Override
    public void setWins(int wins) {
        this.wins = wins;
    }
}
