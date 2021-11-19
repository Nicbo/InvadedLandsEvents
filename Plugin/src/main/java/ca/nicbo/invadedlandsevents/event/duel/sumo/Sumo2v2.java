package ca.nicbo.invadedlandsevents.event.duel.sumo;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Sumo 2v2.
 *
 * @author Nicbo
 */
public class Sumo2v2 extends Sumo {
    public Sumo2v2(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.SUMO_2V2, hostName, ListMessage.SUMO2V2_DESCRIPTION.get(), 2);
    }
}
