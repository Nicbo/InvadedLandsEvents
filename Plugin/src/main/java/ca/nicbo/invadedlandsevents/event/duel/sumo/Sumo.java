package ca.nicbo.invadedlandsevents.event.duel.sumo;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.event.duel.DuelEvent;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

/**
 * Sumo.
 *
 * @author Nicbo
 */
public abstract class Sumo extends DuelEvent {
    private final int minY;
    private final EliminationTask eliminationTask;

    protected Sumo(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description, int teamSize) {
        super(plugin, eventType, hostName, description, teamSize);
        this.minY = getEventConfig().getInteger("min-y");
        this.eliminationTask = new EliminationTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);
        this.eliminationTask.start(getPlugin());
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (event.isCancelled()) {
            return;
        }

        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);
        this.eliminationTask.stop();
    }

    private class EliminationTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public EliminationTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            // Since you can't tie sumo, don't have an eliminated list, just eliminate the first one found...
            getFightingPlayers().stream()
                    .filter(player -> player.getLocation().getY() < minY)
                    .findAny()
                    .ifPresent(player -> eliminatePlayer(player, false));
        }
    }
}
