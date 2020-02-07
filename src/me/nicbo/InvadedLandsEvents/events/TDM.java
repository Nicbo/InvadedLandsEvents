package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.configuration.file.FileConfiguration;

public class TDM extends InvadedEvent {
    public TDM(EventsMain plugin) {
        super("Team Deathmatch", plugin.getConfig());
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
