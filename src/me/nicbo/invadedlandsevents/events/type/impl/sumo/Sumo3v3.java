package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;

/**
 * Represents sumo event with teams of 3
 *
 * @author Nicbo
 */

public final class Sumo3v3 extends Sumo {
    public Sumo3v3(InvadedLandsEvents plugin) {
        super(plugin, "3v3 Sumo", "sumo3v3", 3);
    }
}
