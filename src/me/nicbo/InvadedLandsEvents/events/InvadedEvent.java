package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class InvadedEvent implements Listener {
    private String name;
    protected boolean started;

    protected ConfigurationSection eventConfig;
    protected List<Player> players;
    protected List<Player> spectators;

    public InvadedEvent(String name, EventsMain plugin) {
        this.name = name;
        this.eventConfig = plugin.getConfig().getConfigurationSection(name.toLowerCase().replace("\\s", "-"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        init();
    }

    protected abstract void init();
    public abstract void start();
    public abstract void stop();

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return started;
    }

    public void joinEvent(Player player) {
        players.add(player);
    }

    public void leaveEvent(Player player) {
        players.remove(player);
    }

    public void joinSpectators(Player player) {
        spectators.add(player);
    }

    public void leaveSpectators(Player player) {
        spectators.remove(player);
    }

    /*
    TODO:
        - Scoreboards/Teams
     */
}
