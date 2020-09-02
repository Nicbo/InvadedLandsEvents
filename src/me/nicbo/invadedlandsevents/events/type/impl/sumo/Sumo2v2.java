package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;

/**
 * Represents sumo event with teams of 2
 *
 * @author Nicbo
 */

public final class Sumo2v2 extends Sumo {
    public Sumo2v2(InvadedLandsEvents plugin) {
        super(plugin, "2v2 Sumo", "sumo2v2", 2);
    }
}
