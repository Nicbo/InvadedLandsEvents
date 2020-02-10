package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.logging.Logger;

public abstract class InvadedEvent implements Listener {
    protected EventsMain plugin;
    protected Logger log;
    private String name;
    protected boolean started;
    protected boolean ending;

    protected ConfigurationSection eventConfig;
    protected List<Player> players;
    protected List<Player> spectators;

    public InvadedEvent(String name, EventsMain plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.name = name;
        this.eventConfig = plugin.getConfig().getConfigurationSection(name.toLowerCase().replace("\\s", "-"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        init(plugin);
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

    public boolean containsPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public void joinEvent(Player player) {
        players.add(player);
        player.teleport((Location) eventConfig.get("spec-location"));
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
        player.teleport((Location) eventConfig.get("spec-location"));
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        leaveEvent(event.getPlayer());
    }

    /*
    TODO:
        - Scoreboards/Teams
     */
}
