package ca.nicbo.invadedlandsevents.event.duel.brackets;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;

/**
 * Brackets 3v3.
 *
 * @author Nicbo
 */
public class Brackets3v3 extends Brackets {
    public Brackets3v3(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.BRACKETS_3V3, hostName, ListMessage.BRACKETS3V3_DESCRIPTION.get(), 3);
    }
}
