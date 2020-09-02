package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;

/**
 * Represents sumo event with teams of 1
 *
 * @author Nicbo
 */

public final class Sumo1v1 extends Sumo {
    public Sumo1v1(InvadedLandsEvents plugin) {
        super(plugin, "1v1 Sumo", "sumo1v1", 1);
    }
}
