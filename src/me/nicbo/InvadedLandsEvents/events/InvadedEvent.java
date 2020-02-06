package me.nicbo.InvadedLandsEvents.events;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class InvadedEvent {
    private String name;
    protected ArrayList<Player> players;
    protected ArrayList<Player> spectators;

    public InvadedEvent(String name) {
        this.name = name;
    }

    public abstract void start();
    public abstract void stop();

    public String getName() {
        return name;
    }
    /*
    TODO:
        - Add what every event is using Arraylist of players, countdown for starting event, itemstack for event preview?
     */
}
