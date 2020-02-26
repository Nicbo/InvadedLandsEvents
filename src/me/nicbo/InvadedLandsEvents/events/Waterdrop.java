package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.block.Block;

public class Waterdrop extends InvadedEvent {

    private Block[][] blocks;

    public Waterdrop(EventsMain plugin) {
        super("Waterdrop", "waterdrop", plugin);
    }

    @Override
    public void init(EventsMain plugin) {
        blocks = new Block[5][5];
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
