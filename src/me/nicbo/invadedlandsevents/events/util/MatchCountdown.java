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
     * @param counter     the message to send when counting down
     * @param started     the message to send when the count down ends
     */
    public MatchCountdown(Consumer<String> broadcaster, Message counter, Message started) {
        this(broadcaster, null, counter, started);
    }

    /**
     * Creates instance of a match countdown
     *
     * @param broadcaster the consumer that broadcasts the countdown
     * @param runnable    the runnable that runs when the countdown ends
     * @param counter     the message to send when counting down
     * @param started     the message to send when the count down ends
     */
    public MatchCountdown(Consumer<String> broadcaster, Runnable runnable, Message counter, Message started) {
        this.broadcaster = broadcaster;
        this.runnable = runnable;
        this.COUNTER = counter;
        this.STARTED = started;
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

    public boolean isCounting() {
        return counting;
    }

    public void start(InvadedLandsEvents plugin) {
        this.counting = true;
        super.runTaskTimer(plugin, 0, 20);
    }
}
