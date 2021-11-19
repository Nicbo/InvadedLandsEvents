package ca.nicbo.invadedlandsevents.event.round;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostLeaveEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TNT Tag.
 *
 * @author Nicbo
 */
public class TNTTag extends RoundEvent {
    private static final int[] TIMES = {30};

    private final Set<Player> tagged;

    private final Location startLocation;

    private final Kit kit;
    private final Kit taggedKit;

    public TNTTag(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.TNT_TAG, hostName, ListMessage.TNTTAG_DESCRIPTION.get(), TIMES);
        this.tagged = new HashSet<>();
        this.startLocation = getEventConfig().getLocation("start");
        this.kit = getEventConfig().getKit("kit");
        this.taggedKit = getEventConfig().getKit("tagged-kit");
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new TNTTagScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }

        event.setDamage(0);

        Player damager = event.getDamager();
        if (isTagged(damager) && !isTagged(player)) {
            untagPlayer(damager);
            tagPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPostLeave(EventPlayerPostLeaveEvent event) {
        tagged.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);

        for (Player player : getPlayers()) {
            player.teleport(startLocation);
        }
    }

    @Override
    protected void onStartRound() {
        broadcastMessage(Message.TNTTAG_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        List<Player> players = CollectionUtils.shuffledCopy(getPlayers());

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < players.size() / 2) {
                tagPlayer(player);
            } else {
                untagPlayer(player);
            }
        }
    }

    @Override
    protected void onEndRound() {
        int lost = 0;
        for (Player player : tagged) {
            broadcastMessage(Message.TNTTAG_ELIMINATED.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - ++lost)));
        }
        lose(tagged);
        tagged.clear();
    }

    // ---------- Getters for Plugin module users ----------

    public boolean isTagged(Player player) {
        return tagged.contains(player);
    }

    public Set<Player> getTagged() {
        return Collections.unmodifiableSet(tagged);
    }

    // -----------------------------------------------------

    private void tagPlayer(Player player) {
        tagged.add(player);
        taggedKit.apply(player);
        player.sendMessage(Message.TNTTAG_TAG.get());
        SpigotUtils.clearPotionEffects(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
    }

    private void untagPlayer(Player player) {
        tagged.remove(player);
        kit.apply(player);
        SpigotUtils.clearPotionEffects(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
    }

    private class TNTTagScoreboard extends EventScoreboard {
        private final EventScoreboardLine roundLine;
        private final EventScoreboardLine timerLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        private final EventScoreboardLine warningOneLine;
        private final EventScoreboardLine warningTwoLine;
        private final EventScoreboardLine blankOneLine;
        private final EventScoreboardLine blankTwoLine;

        private boolean showHit;

        public TNTTagScoreboard(Player player) {
            super(player, Message.TITLE_TNTTAG.get(), getConfigName());
            this.warningOneLine = new EventScoreboardLine(9, "&c&lYOU MUST HIT");
            this.warningTwoLine = new EventScoreboardLine(8, "&c&lA TNTLESS PLAYER");
            this.blankOneLine = new EventScoreboardLine(7);

            this.roundLine = new EventScoreboardLine(6);
            this.timerLine = new EventScoreboardLine(5);
            this.blankTwoLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);

            this.setLines(roundLine, timerLine, blankTwoLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            boolean playerTagged = isTagged(getPlayer());

            if (showHit && !playerTagged) {
                showHit = false;
                setLines(roundLine, timerLine, blankTwoLine, playerCountLine, spectatorCountLine);
            } else if (!showHit && playerTagged) {
                showHit = true;
                setLines(warningOneLine, warningTwoLine, blankOneLine, roundLine, timerLine, blankTwoLine, playerCountLine, spectatorCountLine);
            }

            roundLine.setText("&eRound: &6" + getRound());
            timerLine.setText("&eTime Remaining: &6" + getTimer());
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }
    }
}
