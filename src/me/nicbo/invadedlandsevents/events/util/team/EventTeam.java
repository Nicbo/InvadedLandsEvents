package me.nicbo.invadedlandsevents.events.util.team;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Team of players in an event
 *
 * @author Nicbo
 */

public class EventTeam {
    private final String name;
    private final Set<Player> players;

    public EventTeam(String name) {
        this(name, new HashSet<>());
    }

    public EventTeam(String name, Set<Player> players) {
        this.name = name;
        this.players = players;
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

    public void clear() {
        players.clear();
    }

    /**
     * Get unmodifiable set of the internal players
     *
     * @return the players
     */
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Gets the name that is broadcasted when the team wins
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
