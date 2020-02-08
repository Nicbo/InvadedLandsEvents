package me.nicbo.InvadedLandsEvents;

import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class EventsMain extends JavaPlugin {
    private final String name = "InvadedLandsEvents";
    private EventManager eventManager;

    @Override
    public void onEnable() {
        eventManager = new EventManager(this);
        ConfigUtils.setEventWorld(getConfig().getString("event-world"));
        registerCommands();
        System.out.println(name + " has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println(name + " has been disabled!");
    }

    private void registerCommands() {

    }
}
