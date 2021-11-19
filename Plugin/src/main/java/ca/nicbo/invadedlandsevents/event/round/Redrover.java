package ca.nicbo.invadedlandsevents.event.round;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostLeaveEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.RandomWeightedCollection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Redrover.
 *
 * @author Nicbo
 */
public class Redrover extends RoundEvent {
    private static final int[] TIMES = {20, 19, 19, 18, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10};

    private final RandomWeightedCollection<Supplier<PotionEffect>> potionEffectSuppliers;

    private final CuboidRegion blueRegion;
    private final CuboidRegion redRegion;

    private final Location startLocation;
    private final Location killerStartLocation;

    private final Kit kit;
    private final Kit killerKit;

    private Player killer;
    private Side side;

    public Redrover(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.REDROVER, hostName, ListMessage.REDROVER_DESCRIPTION.get(), TIMES);

        this.potionEffectSuppliers = new RandomWeightedCollection<Supplier<PotionEffect>>()
                .add(() -> null, 50)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 0, true, false), 20)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 1, true, false), 20)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 4, true, false), 10);

        this.startLocation = getEventConfig().getLocation("start");
        this.killerStartLocation = getEventConfig().getLocation("killer-start");
        this.blueRegion = getEventConfig().getRegion("blue-region");
        this.redRegion = getEventConfig().getRegion("red-region");
        this.kit = getEventConfig().getKit("kit");
        this.killerKit = getEventConfig().getKit("killer-kit");
    }

    private void pickNewKiller() {
        killer = RandomUtils.randomElement(getPlayers());
        killer.sendMessage(Message.REDROVER_SELECTED.get());
        killer.teleport(killerStartLocation);
        killerKit.apply(killer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPreStart(EventPreStartEvent event) {
        pickNewKiller();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);

        for (Player player : getPlayers()) {
            if (!player.equals(killer)) {
                player.teleport(startLocation);
                kit.apply(player);
            }
        }
    }

    @Override
    protected void onStartRound() {
        if (getRound() != 1 && getPlayersSize() == 2) { // Player and killer, this allows killer to have a chance to win
            end(new EventEndingContext(CollectionUtils.getOther(getPlayers(), killer)));
            return;
        }

        side = side == null ? Side.RED : Side.getOther(side);

        broadcastMessage(Message.REDROVER_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        broadcastMessage(Message.REDROVER_RUN_TO.get().replace("{side}", side.toString()));

        PotionEffect effect = potionEffectSuppliers.next().get();

        if (effect != null) {
            killer.addPotionEffect(effect);
        }
    }

    @Override
    protected void onEndRound() {
        CuboidRegion currentRegion = side == Side.BLUE ? blueRegion : redRegion;
        List<Player> toLose = new ArrayList<>();

        for (Player player : getPlayers()) {
            if (!currentRegion.contains(player) && !player.equals(killer)) {
                toLose.add(player);
                broadcastMessage(Message.REDROVER_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - toLose.size())));
            }
        }

        lose(toLose);
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new RedroverScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        Player player = event.getPlayer();

        if (event.isCancelled()) {
            return;
        }

        // Player hit was killer or damager was not the killer
        if (player.equals(killer) || !event.getDamager().equals(killer)) {
            event.setDamage(0); // No damage but they still get hit
            return;
        }

        if (event.isKillingBlow()) {
            broadcastMessage(Message.REDROVER_ELIMINATED_BY.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - 1))
                    .replace("{killer}", killer.getName()));

            event.doFakeDeath();
            lose(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPostLeave(EventPlayerPostLeaveEvent event) {
        if (isState(EventState.STARTED) && event.getPlayer().equals(killer)) {
            pickNewKiller();
        }
    }

    // ---------- Getters for Plugin module users ----------

    public Player getKiller() {
        return killer;
    }

    public Side getSide() {
        return side;
    }

    // -----------------------------------------------------

    public enum Side {
        BLUE(ChatColor.BLUE + "Blue"),
        RED(ChatColor.RED + "Red");

        private final String message;

        Side(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return message;
        }

        public static Side getOther(Side side) {
            return side == BLUE ? RED : BLUE;
        }
    }

    private class RedroverScoreboard extends EventScoreboard {
        private final EventScoreboardLine roundLine;
        private final EventScoreboardLine countdownLine;
        private final EventScoreboardLine sideLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        public RedroverScoreboard(Player player) {
            super(player, Message.TITLE_REDROVER.get(), getConfigName());
            this.roundLine = new EventScoreboardLine(7);
            this.countdownLine = new EventScoreboardLine(6);
            this.sideLine = new EventScoreboardLine(5);
            EventScoreboardLine blankLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);
            this.setLines(roundLine, countdownLine, sideLine, blankLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            this.roundLine.setText("&eRound: &6" + getRound());
            this.countdownLine.setText("&eCountdown: &6" + getTimer());
            this.sideLine.setText("&eRun to: " + (side == null ? Side.RED : side));
            this.playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            this.spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }
    }
}
