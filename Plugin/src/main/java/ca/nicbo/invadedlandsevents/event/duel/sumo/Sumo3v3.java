package ca.nicbo.invadedlandsevents.event.duel.sumo;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Sumo 3v3.
 *
 * @author Nicbo
 */
public class Sumo3v3 extends Sumo {
    public Sumo3v3(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.SUMO_3V3, hostName, ListMessage.SUMO3V3_DESCRIPTION.get(), 3);
    }
}
