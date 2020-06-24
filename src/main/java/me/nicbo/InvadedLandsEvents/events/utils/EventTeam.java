package me.nicbo.InvadedLandsEvents.events.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Team class
 * Used by team sumos and tdm
 *
 * @author Nicbo
 * @since 2020-05-18
 */

public class EventTeam {
    protected List<Player> players;

    protected EventTeam() {
        this.players = new ArrayList<>();
    }

    public boolean contains(Player player) {
        return players.contains(player);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public int getSize() {
        return players.size();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void clear() {
        players.clear();
    }
}
