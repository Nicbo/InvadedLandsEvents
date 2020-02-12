package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class InvadedEvent implements Listener {
    protected EventsMain plugin;
    protected Logger log;
    private String name;
    protected boolean started;
    protected boolean ending;
    private boolean enabled;

    protected ConfigurationSection eventConfig;
    protected List<Player> players;
    protected List<Player> spectators;

    public InvadedEvent(String name, EventsMain plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.name = name;
        this.eventConfig = plugin.getConfig().getConfigurationSection("events." + name.toLowerCase().replace(" ", ""));
        this.enabled = eventConfig.getBoolean("enabled");
        if (enabled) {
            this.players = new ArrayList<>();
            this.spectators = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(this, plugin);
            init(plugin);
        } else {
            log.info(name + " not enabled!");
        }
    }

    protected abstract void init(EventsMain plugin);
    public abstract void start();
    public abstract void stop();

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isEnding() {
        return ending;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public void joinEvent(Player player) {
        players.add(player);
        player.teleport(ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("spec-location")));
        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        players.remove(player);
        spectators.remove(player);
        player.teleport(ConfigUtils.getSpawnLoc());
        //remove from team and scoreboard
    }

    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("spec-location")));
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
    }

    /*
    TODO:
        - Scoreboards/Teams
     */
}