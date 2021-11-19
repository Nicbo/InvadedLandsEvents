package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostCountdownEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import ca.nicbo.invadedlandsevents.task.event.MatchCountdownTask;
import ca.nicbo.invadedlandsevents.task.world.BlockPlacementTask;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Spleef.
 *
 * @author Nicbo
 * @author StarZorrow
 */
public class Spleef extends TimerEvent {
    private final Location startOne;
    private final Location startTwo;

    private final CuboidRegion snowRegion;

    private final Kit kit;

    private final MatchCountdownTask matchCountdownTask;
    private final EliminationTask eliminationTask;

    public Spleef(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.SPLEEF, hostName, ListMessage.SPLEEF_DESCRIPTION.get());
        this.startOne = getEventConfig().getLocation("start-1");
        this.startTwo = getEventConfig().getLocation("start-2");
        this.snowRegion = getEventConfig().getRegion("snow-region");
        this.kit = getEventConfig().getKit("kit");
        this.matchCountdownTask = new MatchCountdownTask.Builder(this::broadcastMessage)
                .setCounter(Message.SPLEEF_MATCH_COUNTER.get())
                .setStarted(Message.SPLEEF_MATCH_STARTED.get())
                .build();
        this.eliminationTask = new EliminationTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);

        List<Player> players = getPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.teleport(i % 2 == 0 ? startOne : startTwo);
            kit.apply(player);
        }

        this.eliminationTask.start(getPlugin());
        this.matchCountdownTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        super.onEventPostEnd(event);

        if (matchCountdownTask.isRunning()) {
            matchCountdownTask.stop();
        }

        eliminationTask.stop();
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new SpleefScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isPlayerSpectating(player)) {
            event.setCancelled(true);
        } else if (isPlayerPlaying(player)) {
            Block block = event.getBlock();
            if (isBlockBreakable(block)) {
                block.setType(Material.AIR);
                player.getInventory().addItem(CompatibleMaterial.SNOWBALL.createItemStack(4));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostCountdown(EventPostCountdownEvent event) {
        SyncedTask buildSnow = new BlockPlacementTask(snowRegion, Material.SNOW_BLOCK);
        buildSnow.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!isState(EventState.STARTED) || !(shooter instanceof Player) || !isPlayerPlaying((Player) shooter)) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Snowball) {
            Iterator<Block> iterator = new BlockIterator(entity.getWorld(),
                    entity.getLocation().toVector(),
                    entity.getVelocity().normalize(), 0, 4);

            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (isBlockBreakable(block)) {
                    block.setType(Material.AIR);
                    break;
                }
            }
        }
    }

    private boolean isBlockBreakable(Block block) {
        return isState(EventState.STARTED) && !matchCountdownTask.isRunning() &&
                block.getType() == Material.SNOW_BLOCK && snowRegion.contains(block);
    }

    private class EliminationTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public EliminationTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            // Guarantees winner
            getPlayers().stream()
                    .filter(player -> player.getLocation().getY() < snowRegion.getMinY())
                    .findAny()
                    .ifPresent(player -> {
                        broadcastMessage(Message.SPLEEF_ELIMINATED.get()
                                .replace("{player}", player.getName())
                                .replace("{remaining}", String.valueOf(getPlayersSize() - 1)));
                        lose(player);
                    });
        }
    }

    private class SpleefScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;
        private final EventScoreboardLine timeRemainingLine;

        public SpleefScoreboard(Player player) {
            super(player, Message.TITLE_SPLEEF.get(), getConfigName());
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
