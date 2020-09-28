package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;

import java.util.List;

/**
 * Represents sumo event with teams of 2
 *
 * @author Nicbo
 */

public final class Sumo2v2 extends Sumo {
    public Sumo2v2(InvadedLandsEvents plugin) {
        super(plugin, "2v2 Sumo", "sumo2v2", 2);
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return ListMessage.SUMO2V2_DESCRIPTION.get();
    }
}
