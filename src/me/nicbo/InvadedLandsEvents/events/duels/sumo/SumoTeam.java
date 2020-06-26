package me.nicbo.InvadedLandsEvents.events.duels.sumo;

import me.nicbo.InvadedLandsEvents.events.utils.EventTeam;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SumoTeam extends EventTeam {
    private final List<Player> initialPlayers;

    private static final String TEAMOFONE;
    private static final String TEAMOFTWO;
    private static final String TEAMOFTHREE;

    static {
        TEAMOFONE = EventUtils.getEventMessage("TEAMOFONE");
        TEAMOFTWO = EventUtils.getEventMessage("TEAMOFTWO");
        TEAMOFTHREE = EventUtils.getEventMessage("TEAMOFTHREE");
    }

    public SumoTeam() {
        this.initialPlayers = new ArrayList<>(players);
    }

    public void preparePlayers(Location location) {
        super.players.forEach(player -> player.teleport(location));
    }

    @Override
    public String toString() {
        switch (initialPlayers.size()) {
            case 1:
                return TEAMOFONE.replace("{player1}", initialPlayers.get(0).getName());
            case 2:
                return TEAMOFTWO
                        .replace("{player1}", initialPlayers.get(0).getName())
                        .replace("{player2}", initialPlayers.get(1).getName());
            case 3:
                return TEAMOFTHREE
                        .replace("{player1}", initialPlayers.get(0).getName())
                        .replace("{player2}", initialPlayers.get(1).getName())
                        .replace("{player3}", initialPlayers.get(2).getName());
            default:
                return null;
        }
    }
}