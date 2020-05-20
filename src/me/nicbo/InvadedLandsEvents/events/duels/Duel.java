package me.nicbo.InvadedLandsEvents.events.duels;

import me.nicbo.InvadedLandsEvents.events.InvadedEvent;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract duel class, all duel events extend this class
 * Code that is used by all the duel events exist here
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-05-05
 */

public abstract class Duel extends InvadedEvent {

    protected Location startLoc1;
    protected Location startLoc2;

    protected List<Player> fightingPlayers;

    protected boolean fighting;
    protected boolean frozen;

    protected BukkitRunnable leaveCheck;
    protected BukkitRunnable playerFreeze;

    protected final String MATCH_START;
    protected final String MATCH_COUNTER;
    protected final String MATCH_STARTED;
    protected final String ELIMINATED;

    public Duel(String eventName, String configName) {
        super(eventName, configName);

        this.startLoc1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        this.startLoc2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);

        this.fightingPlayers = new ArrayList<>();

        this.MATCH_START = getEventMessage("MATCH_START");
        this.MATCH_COUNTER = getEventMessage("MATCH_COUNTER");
        this.MATCH_STARTED = getEventMessage("MATCH_STARTED");
        this.ELIMINATED = getEventMessage("ELIMINATED");
    }

    public abstract void tpFightingPlayers();

    // Use EventLeaveEvent event instead
    protected void initLeaveCheck() {
        this.leaveCheck = new BukkitRunnable() {
            @Override
            public void run() {
                if (fightingPlayers.size() == 1 && fighting) {
                    roundOver(fightingPlayers.get(0));
                    if (frozen)
                        playerFreeze.cancel();
                }
            }
        };
    }

    protected void restartPlayerFreeze() {
        this.playerFreeze = new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (timer == 5) {
                    frozen = true;
                    EventUtils.broadcastEventMessage(MATCH_START.replace("{player1}", fightingPlayers.get(0).getName())
                            .replace("{player2}", fightingPlayers.get(1).getName()));
                }

                EventUtils.broadcastEventMessage(MATCH_COUNTER.replace("{seconds}", String.valueOf(timer--)));
                if (timer <= 0) {
                    EventUtils.broadcastEventMessage(MATCH_STARTED);
                    frozen = false;
                    this.cancel();
                }
            }
        };

        this.playerFreeze.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    protected void newRound() {
        if (players.size() < 2) {
            playerWon(players.get(0));
        } else {
            fightingPlayers.clear();
            addTwoPlayers();
            tpFightingPlayers();
            restartPlayerFreeze();
            fighting = true;
        }
    }

    protected void roundOver(Player loser) {
        fighting = false;
        Player winner = fightingPlayers.get(0);
        EventUtils.broadcastEventMessage(ELIMINATED
                .replace("{loser}", loser.getName())
                .replace("{winner}", winner.getName())
                .replace("{remaining}", String.valueOf(players.size())));
        EventUtils.clear(winner);
        winner.teleport(specLoc);
        newRound();
    }

    protected void addTwoPlayers() {
        fightingPlayers.add(GeneralUtils.getRandom(players));
        Player player2 = GeneralUtils.getRandom(players);

        while (fightingPlayers.contains(player2)) {
            player2 = GeneralUtils.getRandom(players);
        }

        fightingPlayers.add(player2);
    }

    /*
    TODO:
        - Make this class compatible w/ team sumos
     */
}
