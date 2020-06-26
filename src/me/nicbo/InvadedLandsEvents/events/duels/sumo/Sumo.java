package me.nicbo.InvadedLandsEvents.events.duels.sumo;

import me.nicbo.InvadedLandsEvents.events.duels.Duel;
import me.nicbo.InvadedLandsEvents.events.utils.EventTeam;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Sumo extends Duel {
    private List<SumoTeam> teams;

    private int teamSize;

    private Location start1;
    private Location start2;

    private BukkitRunnable minYCheck;
    private final int MIN_Y;

    Sumo(String name, int teamSize) {
        super(name, "sumo", teamSize);
        teams = new ArrayList<>();
        this.teamSize = teamSize;

        this.start1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        this.start2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);
        this.MIN_Y = eventConfig.getInt("int-min-y");
    }

    @Override
    public void init() {
        this.minYCheck = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Player> iterator = fightingPlayers.iterator();

                while (iterator.hasNext()) {
                    Player player = iterator.next();
                    if (player.getLocation().getY() < MIN_Y) {
                        fightingPlayers.remove(player);
                        player.teleport(specLoc);
                    }
                }
            }
        };
    }

    @Override
    public void start() {
        minYCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        for (int i = 0; i < players.size(); i += teamSize) {
            SumoTeam team = new SumoTeam();
            for (int j = 0; j < teamSize; j++) {
                if (i >= players.size()) {
                    // send player message about being kicked
                    leaveEvent(players.get(i + j));
                }
                team.addPlayer(players.get(i + j));
            }
        }
    }

    @Override
    public void over() {
        teams.clear();
    }

    @Override
    public void tpFightingPlayers() {

    }
}
