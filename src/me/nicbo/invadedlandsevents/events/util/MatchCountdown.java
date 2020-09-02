package me.nicbo.invadedlandsevents.events.util;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/**
 * Counts down for 5 seconds (6 including start message)
 *
 * @author Nicbo
 */

public class MatchCountdown extends BukkitRunnable {
    private final Consumer<String> broadcaster;
    private final Runnable runnable;

    private final Message COUNTER, STARTED;
    private int timer;
    private boolean counting;

    /**
     * Creates instance of a match countdown
     *
     * @param broadcaster the consumer that broadcasts the countdown
     * @param COUNTER     the message to send when counting down
     * @param STARTED     the message to send when the count down ends
     */
    public MatchCountdown(Consumer<String> broadcaster, Message COUNTER, Message STARTED) {
        this(broadcaster, null, COUNTER, STARTED);
    }

    /**
     * Creates instance of a match countdown
     *
     * @param broadcaster the consumer that broadcasts the countdown
     * @param runnable    the runnable that runs when the countdown ends
     * @param COUNTER     the message to send when counting down
     * @param STARTED     the message to send when the count down ends
     */
    public MatchCountdown(Consumer<String> broadcaster, Runnable runnable, Message COUNTER, Message STARTED) {
        this.broadcaster = broadcaster;
        this.runnable = runnable;
        this.COUNTER = COUNTER;
        this.STARTED = STARTED;
        this.timer = 6;
    }

    @Override
    public void run() {
        if (--timer > 0) {
            broadcaster.accept(COUNTER.get().replace("{seconds}", String.valueOf(timer)));
        } else {
            broadcaster.accept(STARTED.get());

            if (runnable != null) {
                runnable.run();
            }

            counting = false;
            this.cancel();
        }
    }

    /**
     * Check if the count down is still counting
     *
     * @return true if the count down is still running
     */
    public boolean isCounting() {
        return counting;
    }

    /**
     * Start the countdown
     *
     * @param plugin the instance of main class
     */
    public void start(InvadedLandsEvents plugin) {
        this.counting = true;
        super.runTaskTimer(plugin, 0, 20);
    }
}
