package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;

import java.util.List;

/**
 * Represents sumo event with teams of 1
 *
 * @author Nicbo
 */

public final class Sumo1v1 extends Sumo {
    public Sumo1v1(InvadedLandsEvents plugin) {
        super(plugin, "1v1 Sumo", "sumo1v1", 1);
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return ListMessage.SUMO1V1_DESCRIPTION.get();
    }
}
