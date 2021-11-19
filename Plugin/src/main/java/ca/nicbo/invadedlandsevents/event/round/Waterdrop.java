package ca.nicbo.invadedlandsevents.event.round;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostLeaveEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.InvadedEventValidationResult;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.bukkit.Material.REDSTONE_BLOCK;
import static org.bukkit.Material.WATER;

/**
 * Waterdrop.
 *
 * @author Nicbo
 */
public class Waterdrop extends RoundEvent {
    private static final int[] TIMES = {20, 20, 19, 19, 18, 17, 17, 16, 15, 14, 14, 13, 12, 12, 10, 10, 10, 9, 8, 7, 7, 6};
    private static final int START_Y_OFFSET = 2; // How many blocks the player can fall before it starts checking

    private final Material[][] closedCover;
    private final List<Material[][]> easyCovers;
    private final List<Material[][]> mediumCovers;
    private final List<Material[][]> hardCovers;

    private final CuboidRegion waterRegion;
    private final CuboidRegion safeRegion;
    private final Location startLocation;

    private final Kit kit;

    private final Set<Player> jumped;
    private final Set<Player> eliminated;

    private final EliminationTask eliminationTask;

    private Material[][] mainCover;

    public Waterdrop(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.WATERDROP, hostName, ListMessage.WATERDROP_DESCRIPTION.get(), TIMES);
        this.closedCover = getClosedCover();
        this.easyCovers = getEasyCovers();
        this.mediumCovers = getMediumCovers();
        this.hardCovers = getHardCovers();
        this.waterRegion = getEventConfig().getRegion("water-region");
        this.safeRegion = getEventConfig().getRegion("safe-region");
        this.startLocation = getEventConfig().getLocation("start");
        this.kit = getEventConfig().getKit("kit");
        this.eliminated = new HashSet<>();
        this.jumped = new HashSet<>();
        this.eliminationTask = new EliminationTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);
        this.eliminationTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);
        this.eliminationTask.stop();
    }

    @Override
    protected void onStartRound() {
        broadcastMessage(Message.WATERDROP_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        setMainCover();
        buildMainCover();

        for (Player player : getPlayers()) {
            player.teleport(startLocation);
            kit.apply(player);
            eliminated.add(player);
        }
    }

    @Override
    protected void onEndRound() {
        int count = 0;
        for (Player player : eliminated) {
            broadcastMessage(Message.WATERDROP_ELIMINATED.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - ++count)));
        }
        lose(eliminated);

        jumped.clear();
        eliminated.clear();
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new WaterdropScoreboard(player);
    }

    @Override
    public InvadedEventValidationResult validate() {
        if (waterRegion.getLengthX() != 5 || waterRegion.getLengthY() != 1 || waterRegion.getLengthZ() != 5) {
            return new InvadedEventValidationResult(false, "The water-region does not form a 5x1x5 square");
        }

        return new InvadedEventValidationResult(true);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPostLeave(EventPlayerPostLeaveEvent event) {
        if (isState(EventState.STARTED)) {
            eliminated.remove(event.getPlayer());
        }
    }

    // ---------- Getters for Plugin module users ----------

    public Set<Player> getJumped() {
        return Collections.unmodifiableSet(jumped);
    }

    // note that eliminated also contains players who have not jumped yet
    public Set<Player> getEliminated() {
        return Collections.unmodifiableSet(eliminated);
    }

    public Material[][] getMainCover() {
        return mainCover == null ? null : deepCopyCover(mainCover);
    }

    // -----------------------------------------------------

    private void setMainCover() {
        int round = getRound();

        List<Material[][]> coverList;
        if (round <= 8) {
            coverList = easyCovers;
        } else if (round <= 14) {
            coverList = mediumCovers;
        } else if (round <= 20) {
            coverList = hardCovers;
        } else {
            mainCover = getHoleCover();
            return;
        }

        mainCover = mainCover == null ? RandomUtils.randomElement(coverList) : RandomUtils.randomElementNotEqual(coverList, mainCover, Arrays::deepEquals);
    }

    private Material[][] getHoleCover() {
        Material[][] cover = deepCopyCover(closedCover);

        for (int i = 0; i < (RandomUtils.randomBoolean() ? 2 : 3); i++) {
            cover[RandomUtils.randomMinMax(0, 4)][RandomUtils.randomMinMax(0, 4)] = WATER;
        }

        return cover;
    }

    private void buildMainCover() {
        int y = waterRegion.getMinY();
        int minX = waterRegion.getMinX();
        int minZ = waterRegion.getMinZ();
        int maxX = waterRegion.getMaxX();
        int maxZ = waterRegion.getMaxZ();

        World world = waterRegion.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, y, z).setType(mainCover[x - minX][z - minZ]);
            }
        }
    }

    private static Material[][] deepCopyCover(Material[][] cover) {
        Material[][] copy = new Material[cover.length][cover.length]; // All covers are squares
        for (int i = 0; i < cover.length; i++) {
            Material[] mat = new Material[cover.length];
            System.arraycopy(cover[i], 0, mat, 0, cover.length);
            copy[i] = mat;
        }
        return copy;
    }

    private static Material[][] getClosedCover() {
        return new Material[][]{
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        };
    }

    private static List<Material[][]> getEasyCovers() {
        return CollectionUtils.unmodifiableList(new Material[][]{ // Open
                {WATER, WATER, WATER, WATER, WATER},
                {WATER, WATER, WATER, WATER, WATER},
                {WATER, WATER, WATER, WATER, WATER},
                {WATER, WATER, WATER, WATER, WATER},
                {WATER, WATER, WATER, WATER, WATER},
        }, new Material[][]{ // 3 lines
                {WATER, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
        }, new Material[][]{ // 2 lines
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
        }, new Material[][]{ // 3 lines reversed
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, WATER},
        }, new Material[][]{ // 2 lines reversed
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER},
        }, new Material[][]{ // N
                {REDSTONE_BLOCK, WATER, WATER, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, WATER, REDSTONE_BLOCK}
        });
    }

    private static List<Material[][]> getMediumCovers() {
        return CollectionUtils.unmodifiableList(new Material[][]{ // H
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER},
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER},
                {WATER, WATER, WATER, WATER, WATER},
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER},
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER}
        }, new Material[][]{ // -
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // |
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // +
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // Target
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // Square
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // S
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, WATER, WATER, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, WATER, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        });
    }

    private static List<Material[][]> getHardCovers() {
        return CollectionUtils.unmodifiableList(new Material[][]{ // 4 corners
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER}
        }, new Material[][]{ // 2 bottom
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {WATER, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // X
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {WATER, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, WATER}
        }, new Material[][]{ // 4 holes
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK, REDSTONE_BLOCK}
        }, new Material[][]{ // Checkered
                {WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER},
                {REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK},
                {WATER, REDSTONE_BLOCK, WATER, REDSTONE_BLOCK, WATER}
        });
    }

    private class EliminationTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public EliminationTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            for (Player player : getPlayers()) {
                if (startLocation.getBlockY() - player.getLocation().getBlockY() > START_Y_OFFSET && SpigotUtils.isPlayerOnGround(player) && !jumped.contains(player)) {
                    if (safeRegion.contains(player)) { // Player is in safe zone
                        broadcastMessage(Message.WATERDROP_SUCCESS_JUMP.get().replace("{player}", player.getName()));
                        eliminated.remove(player);
                    } else { // Player missed
                        broadcastMessage(Message.WATERDROP_FAIL_JUMP.get().replace("{player}", player.getName()));
                    }
                    jumped.add(player);
                }
            }
        }
    }

    private class WaterdropScoreboard extends EventScoreboard {
        private final EventScoreboardLine roundLine;
        private final EventScoreboardLine timerLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        public WaterdropScoreboard(Player player) {
            super(player, Message.TITLE_WATERDROP.get(), getConfigName());
            this.roundLine = new EventScoreboardLine(6);
            this.timerLine = new EventScoreboardLine(5);
            EventScoreboardLine blankLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);
            this.setLines(roundLine, timerLine, blankLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            roundLine.setText("&eRound: &6" + getRound());
            timerLine.setText("&eTime Remaining: &6" + getTimer());
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }
    }
}
