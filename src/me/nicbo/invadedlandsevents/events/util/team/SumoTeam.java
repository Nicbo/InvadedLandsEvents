package me.nicbo.invadedlandsevents.events.util.team;

import me.nicbo.invadedlandsevents.messages.impl.Message;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;

/**
 * Sumo team
 *
 * @author Nicbo
 */

public class SumoTeam extends EventTeam {
    private final Set<Player> initialPlayers;

    /**
     * Creates an instance of SumoTeam
     *
     * @param initialPlayers the initial players of the team
     */
    public SumoTeam(Set<Player> initialPlayers) {
        super(prepareName(initialPlayers), initialPlayers);
        this.initialPlayers = initialPlayers;
    }

    public Set<Player> getInitialPlayers() {
        return initialPlayers;
    }

    /**
     * Creates sumo team name from the initial players
     *
     * @param initialPlayers the initial players
     * @return the name
     * @throws IllegalArgumentException if the size of initial players is not between 1 and 3 (inclusive)
     */
    private static String prepareName(Set<Player> initialPlayers) {
        String name;
        int size = initialPlayers.size();
        switch (size) {
            case 1:
                name = Message.SUMO_TEAM_OF_ONE.get();
                break;
            case 2:
                name = Message.SUMO_TEAM_OF_TWO.get();
                break;
            case 3:
                name = Message.SUMO_TEAM_OF_THREE.get();
                break;
            default:
                throw new IllegalArgumentException("Sumo team size must be between 1 and 3 (inclusive). Given size: " + size);
        }

        Iterator<Player> iterator = initialPlayers.iterator();

        for (int i = 1; i < size + 1; i++) {
            name = name.replace("{player" + i + "}", iterator.next().getName());
        }

        return name;
    }
}
