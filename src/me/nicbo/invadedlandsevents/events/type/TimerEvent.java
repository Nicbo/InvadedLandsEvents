package me.nicbo.invadedlandsevents.events.type;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An event that uses a timer
 *
 * @author Nicbo
 */

public abstract class TimerEvent extends InvadedEvent {
    private final BukkitRunnable eventTimer;

    private int timeLeft;

    protected TimerEvent(InvadedLandsEvents plugin, String eventName, String configName) {
        super(plugin, eventName, configName);
        this.eventTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (--timeLeft <= 0) {
                    winEvent(getTimerEndWinner());
                    this.cancel();
                }
            }
        };

        this.timeLeft = getEventInteger("time-limit-seconds");
    }

    @Override
    protected void start() {
        super.start();
        this.eventTimer.runTaskTimer(plugin, 0, 20);
    }

    @Override
    protected void over() {
        super.over();
        this.eventTimer.cancel();
    }

    /**
     * Returns who should win when timer runs out
     * Override if winner should not be null
     *
     * @return the winner of the event when the time runs out
     */
    public Player getTimerEndWinner() {
        return null;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
