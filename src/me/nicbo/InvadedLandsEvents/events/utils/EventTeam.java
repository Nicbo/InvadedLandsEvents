package me.nicbo.InvadedLandsEvents.events.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class EventTeam {
    private List<Player> players;

    public EventTeam() {
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }
}
