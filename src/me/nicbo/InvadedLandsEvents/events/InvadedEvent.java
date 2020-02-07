package me.nicbo.InvadedLandsEvents.events;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class InvadedEvent {
    private String name;
    protected FileConfiguration config;
    protected ArrayList<Player> players;
    protected ArrayList<Player> spectators;

    public InvadedEvent(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
    }

    public abstract void start();
    public abstract void stop();

    public String getName() {
        return name;
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
