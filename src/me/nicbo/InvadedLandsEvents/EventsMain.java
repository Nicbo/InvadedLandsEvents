package me.nicbo.InvadedLandsEvents;

import org.bukkit.plugin.java.JavaPlugin;

public class EventsMain extends JavaPlugin {
    private final String name = "InvadedLandsEvents";

    @Override
    public void onEnable() {
        System.out.println(name + " has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println(name + " has been disabled!");
    }
}
