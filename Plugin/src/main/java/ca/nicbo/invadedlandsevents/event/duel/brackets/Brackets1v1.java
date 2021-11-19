package ca.nicbo.invadedlandsevents.event.duel.brackets;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Brackets 1v1.
 *
 * @author Nicbo
 */
public class Brackets1v1 extends Brackets {
    public Brackets1v1(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.BRACKETS_1V1, hostName, ListMessage.BRACKETS1V1_DESCRIPTION.get(), 1);
    }
}
