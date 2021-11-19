package ca.nicbo.invadedlandsevents.event.duel.sumo;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Sumo 1v1.
 *
 * @author Nicbo
 */
public class Sumo1v1 extends Sumo {
    public Sumo1v1(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.SUMO_1V1, hostName, ListMessage.SUMO1V1_DESCRIPTION.get(), 1);
    }
}
