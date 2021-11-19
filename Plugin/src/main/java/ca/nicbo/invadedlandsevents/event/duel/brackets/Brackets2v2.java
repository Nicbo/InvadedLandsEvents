package ca.nicbo.invadedlandsevents.event.duel.brackets;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Brackets 2v2.
 *
 * @author Nicbo
 */
public class Brackets2v2 extends Brackets {
    public Brackets2v2(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.BRACKETS_2V2, hostName, ListMessage.BRACKETS2V2_DESCRIPTION.get(), 2);
    }
}
