package me.nicbo.invadedlandsevents.events.type;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.util.MatchCountdown;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an event that has 2 sets of players dueling
 *
 * @author Nicbo
 */

public abstract class DuelEvent extends InvadedEvent {
    private final Message MATCH_COUNTER;
    private final Message MATCH_STARTED;

    protected final Location startLoc1;
    protected final Location startLoc2;

    private boolean fighting;
    private boolean frozen;

    private MatchCountdown matchCountdown;

    protected final Set<Player> fightingPlayers;

    protected DuelEvent(InvadedLandsEvents plugin, String eventName, String configName, String messageName) {
        super(plugin, eventName, configName);

        this.MATCH_COUNTER = Message.valueOf(messageName + "_MATCH_COUNTER");
        this.MATCH_STARTED = Message.valueOf(messageName + "_MATCH_STARTED");

        this.startLoc1 = getEventLocation("start-1");
        this.startLoc2 = getEventLocation("start-2");

        this.fightingPlayers = new HashSet<>();
    }

    @Override
    protected void start() {
        super.start();
        startRound();
    }

    @Override
    protected void over() {
        super.over();

        this.fighting = false;
        this.frozen = false;

        if (matchCountdown.isCounting()) {
            matchCountdown.cancel();
        }
    }

    private void startRound() {
        fighting = true;

        this.matchCountdown = new MatchCountdown(this::broadcastEventMessage, () -> {
            frozen = false;
        }, MATCH_COUNTER, MATCH_STARTED);

        Collection<Player> players = prepareRound();

        if (players != null) {
            fightingPlayers.addAll(players);
            frozen = true;
            this.matchCountdown.start(plugin);
        }
    }

    protected void endRound() {
        fighting = false;
        fightingPlayers.clear();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (isRunning()) { // In case event was force ended or someone won
                startRound();
            }
        }, 20 * 5);
    }

    /**
     * Prepare players for next round
     *
     * @return the players (null if the round should not start)
     */
    protected abstract Collection<Player> prepareRound();

    public boolean isFighting() {
        return fighting;
    }

    public boolean isFrozen() {
        return frozen;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHitD(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player)) {
                return;
            }

            if (frozen || !fighting || !fightingPlayers.contains(player) ||
                    (event.getDamager() instanceof Player && !fightingPlayers.contains((Player) event.getDamager()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (frozen && fightingPlayers.contains(player)) {
            Location to = event.getTo();
            Location from = event.getFrom();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                player.teleport(from.setDirection(to.getDirection()));
            }
        }
    }
}
