package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostLeaveEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigHandler;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * King of the Hill.
 *
 * @author Nicbo
 */
public class KingOfTheHill extends TimerEvent {
    private final Kit kit;

    private final CuboidRegion capRegion;

    private final List<Location> locations;

    private final List<Player> playersInRegion;

    private final Map<Player, Integer> points;
    private final int winPoints;

    private final CapturingAssignmentTask capturingAssignmentTask;
    private final PointsDispatchTask pointsDispatchTask;

    private Player capturing;
    private Player leader;

    public KingOfTheHill(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.KING_OF_THE_HILL, hostName, prepareDescription(plugin.getConfigurationManager().getConfigHandler()));
        this.kit = getEventConfig().getKit("kit");
        this.capRegion = getEventConfig().getRegion("cap-region");
        this.locations = IntStream.range(1, 5)
                .mapToObj(i -> getEventConfig().getLocation("start-" + i))
                .collect(CollectionUtils.toUnmodifiableList());
        this.points = new HashMap<>();
        this.playersInRegion = new ArrayList<>();
        this.winPoints = getEventConfig().getInteger("win-points");
        this.capturingAssignmentTask = new CapturingAssignmentTask();
        this.pointsDispatchTask = new PointsDispatchTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPreStart(EventPreStartEvent event) {
        this.leader = RandomUtils.randomElement(getPlayers());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);
        this.pointsDispatchTask.start(getPlugin());
        this.capturingAssignmentTask.start(getPlugin());

        for (Player player : getPlayers()) {
            preparePlayer(player);
        }

        broadcastMessage(Message.KOTH_START.get());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);
        this.capturingAssignmentTask.stop();
        this.pointsDispatchTask.stop();
    }

    private void preparePlayer(Player player) {
        kit.apply(player);
        player.teleport(getRandomLocation());
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new KingOfTheHillScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }

        if (event.isKillingBlow()) {
            event.doFakeDeath();
            preparePlayer(player);
            if (player.equals(capturing)) {
                lostCapturingPoint(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPostLeave(EventPlayerPostLeaveEvent event) {
        if (isState(EventState.STARTED)) {
            playersInRegion.remove(event.getPlayer());
        }
    }

    // ---------- Getters for Plugin module users ----------

    @Override
    public Player getCurrentWinner() {
        return leader;
    }

    public List<Player> getPlayersInRegion() {
        return Collections.unmodifiableList(playersInRegion);
    }

    public Map<Player, Integer> getPoints() {
        return Collections.unmodifiableMap(points);
    }

    public int getPoints(Player player) {
        return points.getOrDefault(player, 0);
    }

    public Player getCapturing() {
        return capturing;
    }

    public Player getLeader() {
        return leader;
    }

    // -----------------------------------------------------



    private void setRandomCapturing() {
        this.capturing = playersInRegion.isEmpty() ? null : RandomUtils.randomElement(playersInRegion);

        if (capturing != null) {
            broadcastMessage(Message.KOTH_CAPTURING.get().replace("{player}", capturing.getName()));
        }
    }

    private void lostCapturingPoint(Player player) {
        broadcastMessage(Message.KOTH_LOST.get().replace("{player}", player.getName()));
        setRandomCapturing();
    }

    private Location getRandomLocation() {
        return RandomUtils.randomElement(locations);
    }

    private static List<String> prepareDescription(InvadedConfigHandler configHandler) {
        ConfigSection section = configHandler.getConfigSection(EventType.KING_OF_THE_HILL.getConfigName());
        int winPoints = section.getInteger("win-points");
        List<String> description = new ArrayList<>();
        for (String message : ListMessage.KOTH_DESCRIPTION.get()) {
            description.add(message.replace("{points}", String.valueOf(winPoints)));
        }

        return description;
    }

    private class CapturingAssignmentTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public CapturingAssignmentTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            if (capturing == null) { // No one is capturing, attempt to set new capturing player
                setRandomCapturing();
            }

            for (Player player : getPlayers()) { // Loop through each player
                if (capRegion.contains(player)) { // Check if player is in cap region
                    if (!playersInRegion.contains(player)) { // If they aren't already added to the playersInRegion list, add them
                        playersInRegion.add(player);
                    }
                } else { // Player is not in cap region, remove them from list
                    playersInRegion.remove(player);
                }
            }

            if (capturing != null && !playersInRegion.contains(capturing)) { // Player stepped out of cap region
                lostCapturingPoint(capturing);
            }
        }
    }

    private class PointsDispatchTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 20;

        public PointsDispatchTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            if (capturing != null) {
                int newCapturingPoints = CollectionUtils.incrementMap(points, capturing);
                if (newCapturingPoints % 5 == 0) {
                    broadcastMessage(Message.KOTH_CAPTURING_POINTS.get()
                            .replace("{player}", capturing.getName())
                            .replace("{points}", String.valueOf(newCapturingPoints)));
                }

                if (newCapturingPoints >= getPoints(leader)) {
                    leader = capturing;

                    if (newCapturingPoints == winPoints) {
                        end(new EventEndingContext(leader));
                    }
                }
            }
        }
    }

    private class KingOfTheHillScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;
        private final EventScoreboardLine timeRemainingLine;
        private final EventScoreboardLine pointsLine;
        private final EventScoreboardLine leadLine;
        private final EventScoreboardLine capturingLine;

        public KingOfTheHillScoreboard(Player player) {
            super(player, Message.TITLE_KOTH.get(), getConfigName());
            this.playerCountLine = new EventScoreboardLine(11);
            this.spectatorCountLine = new EventScoreboardLine(10);
            this.timeRemainingLine = new EventScoreboardLine(9);
            this.pointsLine = new EventScoreboardLine(8);
            EventScoreboardLine blankOneLine = new EventScoreboardLine(7);
            EventScoreboardLine leadTitleLine = new EventScoreboardLine(6, "&eIn the Lead:");
            this.leadLine = new EventScoreboardLine(5);
            EventScoreboardLine blankTwoLine = new EventScoreboardLine(4);
            EventScoreboardLine capturingTitleLine = new EventScoreboardLine(3, "&eCapturing:");
            this.capturingLine = new EventScoreboardLine(2);
            this.setLines(playerCountLine, spectatorCountLine, timeRemainingLine, pointsLine, blankOneLine, leadTitleLine, leadLine, blankTwoLine, capturingTitleLine, capturingLine);
        }

        @Override
        protected void refresh() {
            Player player = getPlayer();

            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
            timeRemainingLine.setText("&eTime Remaining: &6" + StringUtils.formatSeconds(getTimeLeft()));
            pointsLine.setText("&eYour Points: &6" + getPoints(player));

            ChatColor colour = player.equals(leader) ? ChatColor.GREEN : ChatColor.RED;
            leadLine.setText(colour + leader.getName() + "&7: &6" + getPoints(leader) + "/" + winPoints);
            capturingLine.setText(capturing == null ? "&cNo one." : "&6" + capturing.getName() + " &7(" + getPoints(capturing) + ")");
        }
    }
}
