package me.nicbo.invadedlandsevents.events.type;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An event that is round based
 *
 * @author Nicbo
 */

public abstract class RoundEvent extends InvadedEvent {
    private int timer;
    private int round;

    private final BukkitRunnable roundTimer;

    /**
     * Creates instance of the round event
     *
     * @param plugin     the plugin instance
     * @param configName the events config name
     * @param eventName  the events display name
     * @param times the times for the rounds
     */
    protected RoundEvent(InvadedLandsEvents plugin, String configName, String eventName, int[] times) {
        super(plugin, eventName, configName);
        this.round = 1;
        this.timer = times[0];
        this.roundTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (--timer <= 0) {
                    if (++round >= times.length) {
                        timer = times[times.length - 1]; // Last time in list
                    } else {
                        timer = times[round - 1]; // Next time
                    }

                    eliminatePlayers();
                    if (isRunning()) {
                        newRound();
                    }
                }
            }
        };
    }

    @Override
    protected void start() {
        super.start();
        newRound();
        this.roundTimer.runTaskTimer(plugin, 0, 20);
    }

    @Override
    protected void over() {
        super.over();
        this.roundTimer.cancel();
    }

    /**
     * Starts a new round
     */
    protected abstract void newRound();

    /**
     * Eliminates the players at the end of the round
     */
    protected abstract void eliminatePlayers();


    public int getTimer() {
        return timer;
    }

    public int getRound() {
        return round;
    }
}
