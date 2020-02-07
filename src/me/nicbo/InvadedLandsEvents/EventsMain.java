package me.nicbo.InvadedLandsEvents;

import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EventsMain extends JavaPlugin {
    private final String name = "InvadedLandsEvents";

    @Override
    public void onEnable() {
        EventManager eventManager = new EventManager(this);
        System.out.println(name + " has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println(name + " has been disabled!");
    }
}
