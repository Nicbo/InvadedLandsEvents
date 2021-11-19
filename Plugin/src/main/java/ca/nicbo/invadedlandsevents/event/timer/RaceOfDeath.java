package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Race of Death.
 *
 * @author Nicbo
 */
public class RaceOfDeath extends TimerEvent {
    private final CuboidRegion winRegion;
    private final Location startLoc;
    private final Kit kit;
    private final WinRegionMonitorTask winRegionMonitorTask;

    public RaceOfDeath(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.RACE_OF_DEATH, hostName, ListMessage.ROD_DESCRIPTION.get());
        this.winRegion = getEventConfig().getRegion("win-region");
        this.startLoc = getEventConfig().getLocation("start");
        this.kit = getEventConfig().getKit("kit");
        this.winRegionMonitorTask = new WinRegionMonitorTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false);

        for (Player player : getPlayers()) {
            kit.apply(player);
            player.addPotionEffect(invisibility);
            player.teleport(startLoc);
            player.sendMessage(Message.ROD_START.get());
        }

        this.winRegionMonitorTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);
        this.winRegionMonitorTask.stop();
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new RaceOfDeathScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        event.setCancelled(true);
    }

    private class WinRegionMonitorTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public WinRegionMonitorTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            getPlayers().stream()
                    .filter(winRegion::contains)
                    .findAny()
                    .ifPresent(player -> end(new EventEndingContext(player)));
        }
    }

    private class RaceOfDeathScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;
        private final EventScoreboardLine timeRemainingLine;

        public RaceOfDeathScoreboard(Player player) {
            super(player, Message.TITLE_ROD.get(), getConfigName());
            this.playerCountLine = new EventScoreboardLine(4);
            this.spectatorCountLine = new EventScoreboardLine(3);
            this.timeRemainingLine = new EventScoreboardLine(2);
            this.setLines(playerCountLine, spectatorCountLine, timeRemainingLine);
        }

        @Override
        protected void refresh() {
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
            timeRemainingLine.setText("&eTime Remaining: &6" + StringUtils.formatSeconds(getTimeLeft()));
        }
    }
}
