package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.api.event.EventType;

/**
 * The different {@link HostButton} types.
 *
 * @author Nicbo
 */
public enum HostButtonType {
    BRACKETS(null),
    BRACKETS_1V1(EventType.BRACKETS_1V1),
    BRACKETS_2V2(EventType.BRACKETS_2V2),
    BRACKETS_3V3(EventType.BRACKETS_3V3),
    KING_OF_THE_HILL(EventType.KING_OF_THE_HILL),
    LAST_MAN_STANDING(EventType.LAST_MAN_STANDING),
    ONE_IN_THE_CHAMBER(EventType.ONE_IN_THE_CHAMBER),
    REDROVER(EventType.REDROVER),
    RACE_OF_DEATH(EventType.RACE_OF_DEATH),
    SPLEEF(EventType.SPLEEF),
    SUMO(null),
    SUMO_1V1(EventType.SUMO_1V1),
    SUMO_2V2(EventType.SUMO_2V2),
    SUMO_3V3(EventType.SUMO_3V3),
    TEAM_DEATHMATCH(EventType.TEAM_DEATHMATCH),
    TNT_TAG(EventType.TNT_TAG),
    WATERDROP(EventType.WATERDROP),
    WOOL_SHUFFLE(EventType.WOOL_SHUFFLE);

    private final EventType eventType;

    HostButtonType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
