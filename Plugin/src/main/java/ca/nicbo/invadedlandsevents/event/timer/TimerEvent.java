package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.event.InvadedEvent;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

/**
 * Partial implementation of {@link InvadedEvent}.
 * <p>
 * This type of event has a timer that will end the event when it reaches 0.
 *
 * @author Nicbo
 */
public abstract class TimerEvent extends InvadedEvent {
    private final TimerTask timerTask;
    private int timeLeft;

    protected TimerEvent(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description) {
        super(plugin, eventType, hostName, description);
        this.timerTask = new TimerTask();
        this.timeLeft = getEventConfig().getInteger("time-limit");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        timerTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        timerTask.stop();
    }

    // ---------- Getters for Plugin module users ----------

    public Player getCurrentWinner() {
        return null;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    // -----------------------------------------------------

    private class TimerTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 20;

        protected TimerTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            if (--timeLeft == 0) {
                Player winner = getCurrentWinner();
                if (winner != null) {
                    end(new EventEndingContext(winner));
                } else {
                    end(new EventEndingContext());
                }
            }
        }
    }
}
