package ca.nicbo.invadedlandsevents.event.round;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.event.InvadedEvent;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

/**
 * Partial implementation of {@link InvadedEvent}.
 * <p>
 * This type of event has rounds that end based on the provided times. If a player fails to meet the requirements by the
 * end of the round, they will be eliminated.
 *
 * @author Nicbo
 */
public abstract class RoundEvent extends InvadedEvent {
    private final int[] times;

    private final RoundTimerTask roundTimerTask;

    private int timer;
    private int round;

    protected RoundEvent(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description, int[] times) {
        super(plugin, eventType, hostName, description);
        this.times = times;
        this.roundTimerTask = new RoundTimerTask();
        this.timer = times[0];
        this.round = 1;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        onStartRound();
        roundTimerTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        roundTimerTask.stop();
    }

    protected abstract void onStartRound();

    protected abstract void onEndRound();

    // ---------- Getters for Plugin module users ----------

    public int getTimer() {
        return timer;
    }

    public int getRound() {
        return round;
    }

    // -----------------------------------------------------

    private class RoundTimerTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 20;

        public RoundTimerTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            if (--timer <= 0) {
                if (++round < times.length) {
                    timer = times[round - 1];
                } else {
                    timer = times[times.length - 1];
                }

                onEndRound();
                if (isState(EventState.STARTED)) {
                    onStartRound();
                }
            }
        }
    }
}
